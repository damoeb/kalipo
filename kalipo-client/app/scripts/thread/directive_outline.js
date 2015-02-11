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

                var interpolateHighlightColor = d3.scale.linear()
                    .domain([0, 1])
                    .interpolate(d3.interpolateRgb)
                    .range(['#ffd800', '#ff0000']); // orange - red

                //var interpolateRootColor = d3.scale.linear()
                //    .domain([0, 1])
                //    .interpolate(d3.interpolateRgb)
                //    .range(['#d3d3d3', '#000000']); // lightgray - black

                var conf = {
                    height: 7,
                    yspace: 3
                };

                Thread.outline({id: threadId}, function (comments) {

                    $(document).ready(function () {
                        //Cache the Window object

                        var $this = $(this);

                        // scroll end listener
                        $this.scroll(function(){
                            if ($this.data('scrollTimeout')) {
                                clearTimeout($this.data('scrollTimeout'));
                            }
                            $this.data('scrollTimeout', setTimeout(__scroll, 200));
                        });


                        var lastScrollTop = 0;

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
                            for (var i = 0; i < $all.length; i++) {
                                var $comment = $($all[i]);
                                if ($comment.offset().top > scrollTop) {
                                    $firstOnViewport = $comment;
                                    break;
                                }
                            }

                            var indexOfFirst = parseInt($firstOnViewport.attr('ng-data-index'));
                            var $outline = $element.parent();

                            console.log('scroll to', indexOfFirst);

                            if (indexOfFirst == 0 || $element.parent().height() > scrollTop) {
                                $outline.css({'position': 'relative', 'top': 0});
                            } else {

                                var _top = -(conf.height + conf.yspace) * indexOfFirst + 100;
                                $outline.css({
                                    'position': 'fixed'
                                    //'top': _top + 'px'
                                }).animate({top: _top}, '500', 'swing', function() {
                                    console.log('done scrolling')
                                });
                            }

                        };
                    });

                    var __draw = function () {

                        console.log('drawing');

                        var yRootOffset = 8; // if a level=0 comment occurs
                        var elHeight = 10;
                        var outWidth = $element.width();
                        var __lvl0CommentCount = _.filter(comments, function (c) {
                            return c.level == 0;
                        }).length;
                        var outHeight = comments.length * elHeight;

                        var domainHeight = comments.length * (conf.height + conf.yspace) + __lvl0CommentCount * yRootOffset;
                        console.log('domain-height', domainHeight);

                        var yScale = d3.scale.linear()
                            .domain([0, domainHeight])
                            .range([0, outHeight]);

                        var minI = _.min(comments, function (c) {
                            return c.influence;
                        }).influence;
                        var maxI = _.max(comments, function (c) {
                            return c.influence;
                        }).influence;
                        var maxLevel = _.max(comments, function (c) {
                            return c.level;
                        }).level;
                        var xLevelOffset = 5;
                        var width = 15;
                        var inflBoost = 5;

                        var domainWidth = (maxLevel * xLevelOffset + width + maxI * inflBoost);
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
                            .data(comments)
                            .enter()
                            .append('rect')
                            .attr('x', function (d, i) {
                                return xScale(xLevelOffset * d.level);
                            })
                            .attr('y', function (d, i) {
                                if (d.level == 0) {
                                    yOffsetTotal += yRootOffset;
                                }
                                return yScale(i * elHeight + yOffsetTotal);
                            })
                            .attr('width', function (d, i) {
                                // influence can be <0
                                return xScale(width + Math.max(0, d.influence) * inflBoost);
                            })
                            .attr('height', function (d, i) {
                                return yScale(conf.height);
                            })
                            .attr('fill', function (d, i) {
                                //if (d.level == 0) {
                                //    return interpolateRootColor(Math.abs(d.influence) / iRange);
                                //} else {
                                return interpolateColor(Math.abs(d.influence) / iRange);
                                //}
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

                    var paginated = {};
                    var grouped = _.groupBy(comments, function(comment, index) {
                        return parseInt(index / 200);
                    });
                    _.forEach(grouped, function(comments, page) {
                        paginated[page] = _.flatten(comments);
                    });

                    //console.log('paginated', paginated);

                    var __postFetchedPage = function() {
                        console.log('prepare drawing');
                        _.forEach($scope.pages, function(page){

                            paginated[page.id] = [];
                            __flat(paginated[page.id], page.comments);
                            paginated[page.id] = _.flatten(paginated[page.id]);

                            _.forEach(paginated[page.id], function(comment) {
                                paginated[page.id].push(comment);
                            });

                            // refill comments
                            comments = [];

                            __pushAll(comments, paginated);
                        });

                        __draw();
                    };

                    var timeoutId = setTimeout(__postFetchedPage, 1000);

                    $rootScope.$on('event:fetched-page', function() {

                        clearTimeout(timeoutId);
                        console.log('-> event:fetched-page', $scope.pages);
                        __postFetchedPage();
                    });

                    var __pushAll = function(sink, pages) {
                        _.forEach(pages, function(comments, page) {
                            _.forEach(comments, function(comment) {
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
