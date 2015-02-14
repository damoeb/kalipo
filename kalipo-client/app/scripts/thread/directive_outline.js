/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('threadOutline', function ($compile, $routeParams, $rootScope, Thread, OutlineConfig) {
        return {
            restrict: 'E',
            scope: {
                pages: '='
            },
            template: '<svg id="klp-outline"></svg>',
            replace: true,
            link: function ($scope, $element, $attributes) {

                var threadId = $routeParams.threadId;
                console.log('threadId', threadId);


                var $this = this;

                var $viewportIndicator = $('#outline-viewport-indicator');

                var helper = {
                    __pushAll: function (sink, pages) {
                        _.forEach(pages, function (_comments, page) {
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
                    }

                };

                var __viewport = function () {

                    var scrollTop = $(this).scrollTop();

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
                };

                var __rootsCount = function (fromIndex, untilIndex) {
                    var rootCount = 0;
                    _.forEach(comments, function (comment, index) {
                        if (fromIndex <= index && comment.level == 0) {
                            rootCount++;
                        }
                        return index < untilIndex;
                    });
                    return rootCount;
                };

                var __scroll = function () {

                    var viewport = __viewport();

                    var firstCommentId = viewport.first;
                    var lastCommentId = viewport.last;

                    console.log('viewport from', firstCommentId, 'last', lastCommentId);

                    var indexOfFirst = _.findIndex($this.comments, function (comment) {
                        return comment.id == firstCommentId;
                    });
                    var indexOfLast = _.findIndex($this.comments, function (comment) {
                        return comment.id == lastCommentId;
                    });

                    var _top = -((OutlineConfig.bar_height + OutlineConfig.bar_marginBottom) * indexOfFirst + OutlineConfig.yOffsetForRoots * __rootsCount(0, indexOfFirst));
                    var _height = (OutlineConfig.bar_height + OutlineConfig.bar_marginBottom) * (indexOfLast - indexOfFirst) + OutlineConfig.yOffsetForRoots * (__rootsCount(indexOfFirst, indexOfLast));

                    var $outline = $element.parent();
                    if ($element.parent().offset().top > viewport.scrollTop || viewport.scrollTop < 200) { // || $element.parent().height() > scrollTop) {
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
                };

                $rootScope.$on('event:discussion-changed', function() {
                    console.log('-> event:discussion-changed');
                    __scroll();
                });

                var $doc = $('html, body');

                var __extern = function () {

                    return {
                        width: $element.width() * 1.2,
                        height: $this.comments.length * (OutlineConfig.bar_height + OutlineConfig.bar_marginBottom)
                    }
                };

                var __influence = function () {

                    var minInfluence = _.min($this.comments, function (c) {
                        return c.influence;
                    }).influence;

                    var maxInfluence = _.max($this.comments, function (c) {
                        return c.influence;
                    }).influence;

                    var iRange = Math.abs(minInfluence) + Math.abs(maxInfluence);

                    console.log('minInfluence', minInfluence, 'maxInfluence', maxInfluence, 'iRange', iRange);

                    return {
                        max: maxInfluence,
                        min: minInfluence,
                        range: iRange
                    }
                };

                var __intern = function (influence) {

                    // lowest level is 0
                    var maxLevel = _.max($this.comments, function (c) {
                        return c.level;
                    }).level;

                    var domainWidth = (maxLevel * OutlineConfig.level_xOffset + OutlineConfig.bar_width + influence.max * 0.8 * OutlineConfig.bar_influenceBoost);
                    console.log('domain-width', domainWidth);

                    var domainHeight = $this.comments.length * (OutlineConfig.bar_height + OutlineConfig.bar_marginBottom) + __rootsCount(0, $this.comments.length) * OutlineConfig.yOffsetForRoots;
                    console.log('domain-height', domainHeight);

                    return {
                        width: domainWidth,
                        height: domainHeight
                    }
                };

                var __scale = function (intern, extern) {

                    return {
                        x: d3.scale.linear()
                            .domain([0, intern.width])
                            .range([0, extern.width]),

                        y: d3.scale.linear()
                            .domain([0, intern.height])
                            .range([0, extern.height])
                    }
                };

                var __draw = function () {

                    console.log('drawing');

                    var influence = __influence();
                    var extern = __extern();
                    var intern = __intern(influence);
                    var scale = __scale(intern, extern);

                    // todo fix
                    $this.yScale = scale.y;

                    d3.select('#klp-outline').select('g').remove();

                    var g = d3.select('#klp-outline')
                        .attr('width', extern.width)
                        .attr('height', extern.height)
                        .append('g');

                    var yOffsetTotal = 0;

                    g.selectAll('rect')
                        .data($this.comments)
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
                };

                Thread.outline({id: threadId}, function (comments) {

                    $this.comments = comments;

                    $(document).ready(function () {

                        var $this = $(this);

                        // attach scroll end listener
                        $this.scroll(function () {
                            if ($this.data('scrollTimeout')) {
                                clearTimeout($this.data('scrollTimeout'));
                            }
                            $this.data('scrollTimeout', setTimeout(function () {
                                __scroll()
                            }, 50));
                        });

                        // remove scroll listener
                        $scope.$on('$destroy', function () {
                            $this.off('scroll');
                        });

                    });

                    var paginated = {};
                    var grouped = _.groupBy($this.comments, function(comment, index) {
                        return parseInt(index / OutlineConfig.commentsOnPage);
                    });
                    _.forEach(grouped, function(_comments, page) {
                        paginated[page] = _.flatten(_comments);
                    });

                    var __postFetchedPage = function () {
                        console.log('prepare drawing');
                        _.forEach($scope.pages, function(page){

                            paginated[page.id] = [];
                            helper.__flat(paginated[page.id], page.comments);
                            paginated[page.id] = _.flatten(paginated[page.id]);

                            // refill comments
                            $this.comments = [];

                            helper.__pushAll($this.comments, paginated);
                        });

                        __draw();
                    };

                    var timeoutId = setTimeout(__postFetchedPage, 1000);

                    $rootScope.$on('event:fetched-page', function() {

                        clearTimeout(timeoutId);
                        console.log('-> event:fetched-page', $scope.pages);
                        __postFetchedPage();
                    });

                });
            }
        }
    });
