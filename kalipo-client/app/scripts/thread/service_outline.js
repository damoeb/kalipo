'use strict';

kalipoApp.factory('Outline', function (Thread, OutlineConfig) {

    var $doc = $('html, body');

    var $this = this;

    var helper = {

        pushAll: function (pages) {
            var sink = [];
            _.forEach(pages, function (_comments) {
                _.forEach(_comments, function (comment) {
                    sink.push(comment);
                })
            });
            return sink;
        },

        flat: function (sink, nestedComments) {
            _.forEach(nestedComments, function (comment) {
                sink.push(comment);
                helper.flat(sink, comment.replies);
            });
        },

        rootsCount: function (fromIndex, untilIndex) {
            var rootCount = 0;
            _.forEach(comments, function (comment, index) {
                if (fromIndex <= index && comment.level == 0) {
                    rootCount++;
                }
                return index < untilIndex;
            });
            return rootCount;
        }
    };

    var internal = {
        viewport: function () {

            var scrollTop = $(document).scrollTop();

            // find first comment on viewport
            var $all = $('.comment[ng-comment-id]:visible');

            var $firstOnViewport = $($all[0]);
            _.forEach($all, function (comment) {
                var $comment = $(comment);
                if ($comment.offset().top + $comment.height() > scrollTop) {
                    $firstOnViewport = $comment;
                    return false;
                }
            });

            var _windowHeight = $(window).height();
            var $lastOnViewport = $($all[1]);
//            console.log('top limit', scrollTop + _windowHeight);
            _.forEach($all, function (comment) {
                var $comment = $(comment);
                if ($comment.offset().top < scrollTop + _windowHeight) {
//                    console.log('top', $comment.attr('ng-comment-id'), $comment.offset().top);
                    $lastOnViewport = $comment;
                }
            });

            return {
                first: $firstOnViewport.attr('ng-comment-id'),
                last: $lastOnViewport.attr('ng-comment-id'),
                scrollTop: scrollTop
            }
        },

        influence: function (comments) {

            var minInfluence = _.min(comments, function (c) {
                return c.influence;
            }).influence;

            var maxInfluence = _.max(comments, function (c) {
                return c.influence;
            }).influence;

            var iRange = Math.abs(minInfluence) + Math.abs(maxInfluence);

//            console.log('minInfluence', minInfluence, 'maxInfluence', maxInfluence, 'iRange', iRange);

            return {
                max: maxInfluence,
                min: minInfluence,
                range: iRange
            }
        },

        intern: function (influence, comments) {

            // lowest level is 0
            var maxLevel = _.max(comments, function (c) {
                return c.level;
            }).level;

            var domainWidth = (maxLevel * OutlineConfig.level_xOffset + OutlineConfig.bar_width + influence.max * 0.8 * OutlineConfig.bar_influenceBoost);
            var domainHeight = comments.length * (OutlineConfig.bar_height + OutlineConfig.bar_marginBottom) + helper.rootsCount(0, comments.length) * OutlineConfig.yOffsetForRoots;

            return {
                width: domainWidth,
                height: domainHeight
            }
        },

        scale: function (intern, extern) {

            return {
                x: d3.scale.linear()
                    .domain([0, intern.width])
                    .range([0, extern.width]),

                y: d3.scale.linear()
                    .domain([0, intern.height])
                    .range([0, extern.height])
            }
        }
    };

    return {
        draw: function (comments, dimensions) {

            console.log('drawing outline');

            var influence = internal.influence(comments);
            var externDimensions = dimensions;
            var internDimensions = internal.intern(influence, comments);
            var scale = internal.scale(internDimensions, externDimensions);

            // todo fix e
            $this.yScale = scale.y;

            d3.select('#klp-outline').select('g').remove();

            var g = d3.select('#klp-outline')
                .attr('width', externDimensions.width)
                .attr('height', externDimensions.height)
                .append('g');

            var yOffsetTotal = 0;

            g.selectAll('rect')
                .data(comments)
                .enter()
                .append('rect')
                .attr('x', function (d, i) {
                    return scale.x(OutlineConfig.level_xOffset * d.level);
                })
                .attr('y', function (d, i) {
                    if (d.level == 0) {
                        yOffsetTotal += OutlineConfig.yOffsetForRoots;
                    }
                    return scale.y(i * (OutlineConfig.bar_height + OutlineConfig.bar_marginBottom) + yOffsetTotal);
                })
                .attr('width', function (d, i) {
                    // influence can be <0
                    return scale.x(OutlineConfig.bar_width + Math.max(0, d.influence) * OutlineConfig.bar_influenceBoost);
                })
                .attr('height', function (d, i) {
                    return scale.y(OutlineConfig.bar_height);
                })
                .attr('fill', function (d, i) {
                    if (_.isUndefined(d.createdByMod)) {
                        return OutlineConfig.colorInterpolator(Math.abs(d.influence) / influence.range);
                    } else {
                        return OutlineConfig.colorInterpolator(Math.abs(d.influence) / influence.range);
                    }
                })
                .attr('title', function (d, i) {
                    return 'Click to scroll - ' + d.id;
                })
                .on('click', function (d, i) {
                    console.log('scroll to', d.id);
                    var $element = $('#' + d.id);
                    // show element if in hidden branch
                    if (!$element.is(':visible')) {
                        $element.parents('.replies.optionals').removeClass('hidden');
                    }
                    $doc.scrollTop($element.offset().top);
                });
        },

        flattenPages: function (pages) {

            var flat_pages = {};

            _.forEach(pages, function (page) {

                flat_pages[page.id] = [];
                helper.flat(flat_pages[page.id], page.comments);
                flat_pages[page.id] = _.flatten(flat_pages[page.id]);
            });

            return helper.pushAll(flat_pages);
        },

        /**
         * update outline viewport
         * @param comments list of comments
         * @param $outline the dom element
         */
        refreshViewport: function (comments, $outline) {

            var viewport = internal.viewport();

            var firstCommentId = viewport.first;
            var lastCommentId = viewport.last;

//            console.log('viewport from', firstCommentId, 'last', lastCommentId);

            var indexOfFirst = _.findIndex(comments, function (comment) {
                return comment.id == firstCommentId;
            });
            var indexOfLast = _.findIndex(comments, function (comment) {
                return comment.id == lastCommentId;
            });

            var _top = -((OutlineConfig.bar_height + OutlineConfig.bar_marginBottom) * indexOfFirst + OutlineConfig.yOffsetForRoots * helper.rootsCount(0, indexOfFirst));
            var _height = (OutlineConfig.bar_height + OutlineConfig.bar_marginBottom) * (indexOfLast - indexOfFirst) + OutlineConfig.yOffsetForRoots * (helper.rootsCount(indexOfFirst, indexOfLast));

            var $viewportIndicator = $('#outline-viewport-indicator');
            if ($outline.offset().top > viewport.scrollTop || viewport.scrollTop < 200) { // || $element.parent().height() > scrollTop) {
                $outline.css({'position': 'relative', 'top': 0});
                $viewportIndicator.hide();

            } else {

                if ($outline.css('position') == 'relative') {
                    $outline.css({
                        'position': 'fixed',
                        'top': -50
                    });
                }

                $outline.animate({top: $this.yScale(_top)}, '300', 'swing');
                $viewportIndicator.show().animate({height: $this.yScale(_height)}, '200', 'swing');
            }
        }
    }
});
