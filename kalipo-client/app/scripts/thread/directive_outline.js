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
                    height: 7,
                    yspace: 3,
                    yRootOffset: 8, // if a level=0 comment occurs
                    commentsOnPage: 200,
                    elHeight: 10,
                    xLevelOffset: 5,
                    width: 15,
                    inflBoost: 5
                };

                var __lvl0CommentCount = function (comments) {
                    return _.filter(comments, function (c) {
                        return c.level == 0;
                    }).length;
                };

                var lastScrollTop = 0;
                var $this = this;

                var __scroll = function () {

                    var scrollTop = $(this).scrollTop();

                    if (scrollTop == lastScrollTop) {
                        //console.log('skip scroll');
                        return;
                    }

                    lastScrollTop = scrollTop;

                    // find first comment on viewport
                    var $all = $('.comment');


                    var $firstOnViewport = $($all[0]);
                    _.forEach($all, function (comment) {
                        var $comment = $(comment);
                        if ($comment.offset().top > scrollTop) {
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

                    var __rootsUntilIndex = function (fromIndex, untilIndex) {
                        var rootCount = 0;
                        _.forEach(comments, function (comment, index) {
                            if (fromIndex<=index && comment.level == 0) {
                                rootCount++;
                            }
                            return index <= untilIndex;
                        });
                        return rootCount;
                    };

                    var _top = -((conf.height + conf.yspace) * indexOfFirst + conf.yRootOffset * __rootsUntilIndex(0, indexOfFirst));
                    var _height = conf.yspace * (indexOfLast - indexOfFirst +1) + conf.yRootOffset * (__rootsUntilIndex(indexOfFirst, indexOfLast) +1);

                    $('#outline-viewport').animate({height: _height}, '200', 'swing');

                    var $outline = $element.parent();
                    if (indexOfFirst == 0) { // || $element.parent().height() > scrollTop) {
                        $outline.css({'position': 'relative', 'top': 0});
                    } else {

                        $outline.css({
                            'position': 'fixed'
                        }).animate({top: $this.yScale(_top)}, '500', 'swing');
                    }
                };

                var __init = function () {
                    var outHeight = $this.comments.length * conf.elHeight;
                    var domainHeight = $this.comments.length * (conf.height + conf.yspace) + __lvl0CommentCount($this.comments) * conf.yRootOffset;
                    console.log('domain-height', domainHeight);

                    $this.yScale = d3.scale.linear()
                        .domain([0, domainHeight])
                        .range([0, outHeight]);
                };

                var __draw = function () {

                    console.log('drawing');

                    var outWidth = $element.width();
                    var outHeight = $this.comments.length * conf.elHeight;

                    var minI = _.min($this.comments, function (c) {
                        return c.influence;
                    }).influence;
                    var maxI = _.max($this.comments, function (c) {
                        return c.influence;
                    }).influence;
                    var maxLevel = _.max($this.comments, function (c) {
                        return c.level;
                    }).level;

                    var domainWidth = (maxLevel * conf.xLevelOffset + conf.width + maxI * conf.inflBoost);
                    console.log('domain-width', domainWidth);

                    var xScale = d3.scale.linear()
                        .domain([0, domainWidth])
                        .range([0, outWidth]);

                    var iRange = Math.abs(minI) + Math.abs(maxI);

                    console.log('minI', minI, 'maxI', maxI, 'iRange', iRange);

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
                            return xScale(conf.xLevelOffset * d.level);
                        })
                        .attr('y', function (d, i) {
                            if (d.level == 0) {
                                yOffsetTotal += conf.yRootOffset;
                            }
                            return $this.yScale(i * conf.elHeight + yOffsetTotal);
                        })
                        .attr('width', function (d, i) {
                            // influence can be <0
                            return xScale(conf.width + Math.max(0, d.influence) * conf.inflBoost);
                        })
                        .attr('height', function (d, i) {
                            return $this.yScale(conf.height);
                        })
                        .attr('fill', function (d, i) {
//                            if (d.level == 0) {
//                                return interpolateRootColor(Math.abs(d.influence) / iRange);
//                            } else {
                                return interpolateColor(Math.abs(d.influence) / iRange);
//                            }
                        })
                        .attr('title', function (d, i) {
                            return 'Click to scroll - ' + d.id;
//                                return d.influence;
                        })
                        .on('click', function (d, i) {
                            console.log('go to', d.id);
                            // todo fix this
                            //$(document).animate({
                            //    scrollTop: $('#54c4c6b4c830ed79f392b012').offset().top
                            //    //scrollTop: $('#' + d.id).offset().top
                            //}, 2000);
                        });
                };

                Thread.outline({id: threadId}, function (comments) {

                    $this.comments = comments;

                    $(document).ready(function () {
                        //Cache the Window object

                        var $this = $(this);

                        // scroll end listener
                        $this.scroll(function () {
                            if ($this.data('scrollTimeout')) {
                                clearTimeout($this.data('scrollTimeout'));
                            }
                            $this.data('scrollTimeout', setTimeout(function () {
                                __scroll()
                            }, 200));
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

                            _.forEach(paginated[page.id], function(comment) {
                                paginated[page.id].push(comment);
                            });

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
                            //console.log(comment.id, comment.level);
                            __flat(sink, comment.replies.verbose);
                        });
                    };

                });
            }
        }
    });
