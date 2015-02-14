'use strict';

kalipoApp.factory('Outline', function (Thread, OutlineConfig) {

    var $doc = $('html, body');

    var $this = this;

    var helper = {

        __pushAll: function (sink, pages) {
            _.forEach(pages, function (_comments) {
                _.forEach(_comments, function (comment) {
                    sink.push(comment);
                })
            })
        },

        __flat: function (sink, deepComments) {
            _.forEach(deepComments, function (comment) {
                sink.push(comment);
                helper.__flat(sink, comment.replies.verbose);
            });
        },

        __rootsCount: function (fromIndex, untilIndex) {
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
        __viewport: function () {

            var scrollTop = $(document).scrollTop();

            // find first comment on viewport
            var $all = $('.comment');

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
            _.forEach($all, function (comment) {
                var $comment = $(comment);
                if ($comment.offset().top + $comment.height() < scrollTop + _windowHeight) {
                    $lastOnViewport = $comment;
                }
            });

            return {
                first: $firstOnViewport.attr('ng-comment-id'),
                last: $lastOnViewport.attr('ng-comment-id'),
                scrollTop: scrollTop
            }
        },

        __extern: function (comments) {

            return {
                width: $this.$element.width() * 1.2,
                height: comments.length * (OutlineConfig.bar_height + OutlineConfig.bar_marginBottom)
            }
        },

        __influence: function (comments) {

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

        __intern: function (influence, comments) {

            // lowest level is 0
            var maxLevel = _.max(comments, function (c) {
                return c.level;
            }).level;

            var domainWidth = (maxLevel * OutlineConfig.level_xOffset + OutlineConfig.bar_width + influence.max * 0.8 * OutlineConfig.bar_influenceBoost);
            console.log('domain-width', domainWidth);

            var domainHeight = comments.length * (OutlineConfig.bar_height + OutlineConfig.bar_marginBottom) + helper.__rootsCount(0, comments.length) * OutlineConfig.yOffsetForRoots;
            console.log('domain-height', domainHeight);

            return {
                width: domainWidth,
                height: domainHeight
            }
        },

        __scale: function (intern, extern) {

            return {
                x: d3.scale.linear()
                    .domain([0, intern.width])
                    .range([0, extern.width]),

                y: d3.scale.linear()
                    .domain([0, intern.height])
                    .range([0, extern.height])
            }
        },

        __draw: function (comments) {

            console.log('drawing');

            var influence = internal.__influence(comments);
            var extern = internal.__extern(comments);
            var intern = internal.__intern(influence, comments);
            var scale = internal.__scale(intern, extern);

            // todo fix
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
                    return OutlineConfig.colorInterpolator(Math.abs(d.influence) / influence.range);
                })
                .attr('title', function (d, i) {
                    return 'Click to scroll - ' + d.id;
                })
                .on('click', function (d, i) {
                    console.log('go to', d.id);
                    // todo check if comment is there, otherwise try to load it
                    $doc.animate({
                        scrollTop: $('#' + d.id).offset().top
                    }, 500);
                });
        },

        __paginate: function (comments) {
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

            var paginated = internal.__paginate(comments);
            _.forEach(pages, function (page) {

                paginated[page.id] = [];
                helper.__flat(paginated[page.id], page.comments);
                paginated[page.id] = _.flatten(paginated[page.id]);

            });

            // updated pages to flat comments
            comments = [];

            helper.__pushAll(comments, paginated);

            internal.__draw(comments);

            onSuccess(comments);
        },

        scroll: function (comments) {

            var viewport = internal.__viewport();

            var firstCommentId = viewport.first;
            var lastCommentId = viewport.last;

            console.log('viewport from', firstCommentId, 'last', lastCommentId);

            var indexOfFirst = _.findIndex(comments, function (comment) {
                return comment.id == firstCommentId;
            });
            var indexOfLast = _.findIndex(comments, function (comment) {
                return comment.id == lastCommentId;
            });

            var _top = -((OutlineConfig.bar_height + OutlineConfig.bar_marginBottom) * indexOfFirst + OutlineConfig.yOffsetForRoots * helper.__rootsCount(0, indexOfFirst));
            var _height = (OutlineConfig.bar_height + OutlineConfig.bar_marginBottom) * (indexOfLast - indexOfFirst) + OutlineConfig.yOffsetForRoots * (helper.__rootsCount(indexOfFirst, indexOfLast));

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
