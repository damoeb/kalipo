/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('threadOutline', function ($compile, $routeParams, $rootScope, Thread) {
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

                var interpolateColor = d3.scale.linear()
                    .domain([0,1])
                    .interpolate(d3.interpolateRgb)
                    .range(['#cccccc', '#0000ff']); // lightgray - blue

                //var interpolateHighlightColor = d3.scale.linear()
                //    .domain([0, 1])
                //    .interpolate(d3.interpolateRgb)
                //    .range(['#ffd800', '#ff0000']); // orange - red

//                var interpolateRootColor = d3.scale.linear()
//                    .domain([0, 1])
//                    .interpolate(d3.interpolateRgb)
//                    .range(['#d3d3d3', '#000000']); // lightgray - black

                var conf = {
                    bar_height: 7,
                    bar_marginBottom: 3,
                    yOffsetForRoots: 8, // if a level=0 comment occurs
                    commentsOnPage: 200,
                    level_xOffset: 5,
                    bar_width: 15,
                    bar_influenceBoost: 5
                };

                var __lvl0CommentCount = function (comments) {
                    return _.filter(comments, function (c) {
                        return c.level == 0;
                    }).length;
                };

                var $this = this;

                var $viewport = $('#outline-viewport');

                var __scroll = function () {

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

                    var firstCommentId = $firstOnViewport.attr('ng-comment-id');
                    var lastCommentId = $lastOnViewport.attr('ng-comment-id');

                    console.log('viewport from', firstCommentId, 'last', lastCommentId);

                    var indexOfFirst = _.findIndex($this.comments, function (comment) {
                        return comment.id == firstCommentId;
                    });
                    var indexOfLast = _.findIndex($this.comments, function (comment) {
                        return comment.id == lastCommentId;
                    });

                    var __rootsCount = function (fromIndex, untilIndex) {
                        var rootCount = 0;
                        _.forEach(comments, function (comment, index) {
                            if (fromIndex<=index && comment.level == 0) {
                                rootCount++;
                            }
                            return index < untilIndex;
                        });
                        return rootCount;
                    };

                    var _top = -((conf.bar_height + conf.bar_marginBottom) * indexOfFirst + conf.yOffsetForRoots * __rootsCount(0, indexOfFirst));
                    var _height = (conf.bar_height + conf.bar_marginBottom) * (indexOfLast - indexOfFirst) + conf.yOffsetForRoots * (__rootsCount(indexOfFirst, indexOfLast));

                    var $outline = $element.parent();
                    if ($element.parent().offset().top > scrollTop || scrollTop < 200) { // || $element.parent().height() > scrollTop) {
                        $outline.css({'position': 'relative', 'top': 0});
                        $viewport.hide();

                    } else {

                        if ($outline.css('position') == 'relative') {
                            $outline.css({
                                'position': 'fixed',
                                'top': -50
                            });
                        }

                        $outline.animate({top: $this.yScale(_top)}, '300', 'swing');

                        $viewport.show().animate({height: $this.yScale(_height)}, '200', 'swing');
                    }
                };

                $rootScope.$on('event:discussion-changed', function() {
                    console.log('-> event:discussion-changed');
                    __scroll();
                });

                var __init = function () {
                    var outHeight = $this.comments.length * (conf.bar_height + conf.bar_marginBottom);
                    var domainHeight = $this.comments.length * (conf.bar_height + conf.bar_marginBottom) + __lvl0CommentCount($this.comments) * conf.yOffsetForRoots;
                    console.log('domain-height', domainHeight);

                    $this.yScale = d3.scale.linear()
                        .domain([0, domainHeight])
                        .range([0, outHeight]);
                };

                var $doc = $('html, body');

                var __draw = function () {

                    console.log('drawing');

                    var outWidth = $element.width() - 30;
                    var outHeight = $this.comments.length * (conf.bar_height + conf.bar_marginBottom);

                    var minInfluence = _.min($this.comments, function (c) {
                        return c.influence;
                    }).influence;

                    var maxInfluence = _.max($this.comments, function (c) {
                        return c.influence;
                    }).influence;

                    // lowest level is 0
                    var maxLevel = _.max($this.comments, function (c) {
                        return c.level;
                    }).level;

                    var domainWidth = (maxLevel * conf.level_xOffset + conf.bar_width + maxInfluence * 0.8 * conf.bar_influenceBoost);
                    console.log('domain-width', domainWidth);

                    var xScale = d3.scale.linear()
                        .domain([0, domainWidth])
                        .range([0, outWidth]);

                    var iRange = Math.abs(minInfluence) + Math.abs(maxInfluence);

                    console.log('minInfluence', minInfluence, 'maxInfluence', maxInfluence, 'iRange', iRange);

                    d3.select('#klp-outline').select('g').remove();

                    var g = d3.select('#klp-outline')
                        .attr('width', outWidth)
                        .attr('height', outHeight)
                        .append('g');

                    var yOffsetTotal = 0;

                    g.selectAll('rect')
                        .data($this.comments)
                        .enter()
                        .append('rect')
                        .attr('x', function (d, i) {
                            return xScale(conf.level_xOffset * d.level);
                        })
                        .attr('y', function (d, i) {
                            if (d.level == 0) {
                                yOffsetTotal += conf.yOffsetForRoots;
                            }
                            return $this.yScale(i * (conf.bar_height + conf.bar_marginBottom) + yOffsetTotal);
                        })
                        .attr('width', function (d, i) {
                            // influence can be <0
                            return xScale(conf.bar_width + Math.max(0, d.influence) * conf.bar_influenceBoost);
                        })
                        .attr('height', function (d, i) {
                            return $this.yScale(conf.bar_height);
                        })
                        .attr('fill', function (d, i) {
                            return interpolateColor(Math.abs(d.influence) / iRange);
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
                        return parseInt(index / conf.commentsOnPage);
                    });
                    _.forEach(grouped, function(_comments, page) {
                        paginated[page] = _.flatten(_comments);
                    });

                    var __postFetchedPage = function () {
                        console.log('prepare drawing');
                        _.forEach($scope.pages, function(page){

                            paginated[page.id] = [];
                            __flat(paginated[page.id], page.comments);
                            paginated[page.id] = _.flatten(paginated[page.id]);

                            // refill comments
                            $this.comments = [];

                            __pushAll($this.comments, paginated);
                        });

                        __init();
                        __draw();
                    };

                    var timeoutId = setTimeout(__postFetchedPage, 1000);

                    $rootScope.$on('event:fetched-page', function() {

                        clearTimeout(timeoutId);
                        console.log('-> event:fetched-page', $scope.pages);
                        __postFetchedPage();
                    });

                    var __pushAll = function(sink, pages) {
                        _.forEach(pages, function(_comments, page) {
                            _.forEach(_comments, function(comment) {
                                sink.push(comment);
                            })
                        })
                    };

                    var __flat = function(sink, deepComments) {
                        _.forEach(deepComments, function(comment) {
                            sink.push(comment);
                            __flat(sink, comment.replies.verbose);
                        });
                    };

                });
            }
        }
    });
