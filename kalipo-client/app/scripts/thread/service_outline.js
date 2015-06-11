'use strict';

kalipoApp.factory('Outline', function (Thread, OutlineConfig) {

    var $doc = $('html, body');

    var $this = this;

    var helper = {

        pushAll: function (sink, pages) {
            _.forEach(pages, function (_comments) {
                _.forEach(_comments, function (comment) {
                    sink.push(comment);
                })
            })
        },

        flat: function (sink, deepComments) {
            _.forEach(deepComments, function (comment) {
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
            var $all = $('.comment:visible');

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

        extern: function (comments) {

            return {
                width: $this.$element.width(),
                height: comments.length * (OutlineConfig.bar_height + OutlineConfig.bar_marginBottom)
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

            console.log('minInfluence', minInfluence, 'maxInfluence', maxInfluence, 'iRange', iRange);

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
            console.log('domain-width', domainWidth);

            var domainHeight = comments.length * (OutlineConfig.bar_height + OutlineConfig.bar_marginBottom) + helper.rootsCount(0, comments.length) * OutlineConfig.yOffsetForRoots;
            console.log('domain-height', domainHeight);

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
        },

        draw: function (comments) {

            console.log('drawing');

            var influence = internal.influence(comments);
            var extern = internal.extern(comments);
            var intern = internal.intern(influence, comments);
            var scale = internal.scale(intern, extern);

            // todo fix e
            $this.yScale = scale.y;

            d3.select('#klp-outline').select('g').remove();

            var g = d3.select('#klp-outline')
                .attr('width', extern.width)
                .attr('height', extern.height)
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
                    console.log('goto', d.id);
                    // todo check if comment is there, otherwise try to load it
                    $doc.animate({
                        scrollTop: $('#' + d.id).offset().top
                    }, 500);
                });
        },

        paginate: function (comments) {
            var paginated = {};
            var grouped = _.groupBy(comments, function (comment, index) {
                return parseInt(index / OutlineConfig.commentsOnPage);
            });
            _.forEach(grouped, function (_comments, page) {
                paginated[page] = _.flatten(_comments);
            });

            return paginated;
        }

    };

    return {
        // todo rm this
        assign: function ($element) {
            $this.$element = $element;
        },

        fetchOutline: function (threadId, onSuccess) {

            Thread.outline({id: threadId}, function (comments) {

                onSuccess(comments);

            });
        },

        prepareAndDraw: function (pages, comments, onSuccess) {

            console.log('prepare drawing');

            var paginated = internal.paginate(comments);
            _.forEach(pages, function (page) {

                paginated[page.id] = [];
                helper.flat(paginated[page.id], page.comments);
                paginated[page.id] = _.flatten(paginated[page.id]);

            });

            // updated pages to flat comments
            comments = [];

            helper.pushAll(comments, paginated);

            internal.draw(comments);

            onSuccess(comments);
        },

        scroll: function (comments) {

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
            var $outline = $this.$element.parent();
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
