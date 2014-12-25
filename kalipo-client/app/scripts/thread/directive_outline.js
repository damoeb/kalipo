/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('threadOutline', function ($compile, $routeParams, Thread) {
        return {
            restrict: 'E',
            scope: {},
            template: '<svg id="klp-outline"></svg>',
            replace: true,
            link: function ($scope, $element, $attributes) {

                var threadId = $routeParams.threadId;
                console.log('threadId', threadId);

                var interpolateColor = d3.scale.linear()
                    .domain([0,1])
                    .interpolate(d3.interpolateRgb)
                    .range(['#d3d3d3', '#0000ff']); // lightgray - blue

                var conf = {
                    height: 7,
                    yspace: 3
                };

                Thread.outline({id: threadId}, function (comments) {

                    //$(document).ready(function() {
                    //    //Cache the Window object
                    //
                    //    $(window).scroll(function() {
                    //
                    //        var scrollTop = $(this).scrollTop();
                    //
                    //        // find first comment on viewport
                    //        //var first = _.first($('.comment'), function(comment) {
                    //        //    console.log($(comment), $(comment).offset().top, scrollTop, $(comment).offset().top > scrollTop);
                    //        //    return $(comment).offset().top < scrollTop;
                    //        //});
                    //
                    //        var $all = $('.comment');
                    //        var $first;
                    //        for(var i=0; i<$all.length; i++) {
                    //            var $comment = $($all[i]);
                    //            if($comment.offset().top > scrollTop) {
                    //                $first = $comment;
                    //                break;
                    //            }
                    //        }
                    //
                    //        var index = parseInt($first.attr('ng-data-index'));
                    //        console.log('first', index);
                    //
                    //        var outline = $element.parent();
                    //        if(index == 0) {
                    //            outline.css({'position':'relative', 'top':0});
                    //        } else {
                    //            outline.css({'position':'fixed', 'top': -(conf.height + conf.yspace) * index + 100 + 'px'});
                    //        }
                    //
                    //    });
                    //});


                    var outWidth = $element.width();
                    var outHeight = Math.max($(window).height(), comments.length * 6);

                    var yScale = d3.scale.linear()
                        .domain([0, comments.length * (conf.height + conf.yspace)])
                        .range([0, outHeight]);

                    var xScale = d3.scale.linear()
                        .domain([0, 150])
                        .range([0, outWidth]);

                    var minI = _.min(comments, function(c) { return c.influence; }).influence;
                    var maxI = _.max(comments, function(c) { return c.influence; }).influence;
                    var iRange = Math.abs(minI) + Math.abs(maxI);

                    console.log('minI',minI, 'maxI',maxI, 'iRange',iRange);

                    d3.select('#klp-outline')
                        .attr('width', outWidth)
                        .attr('height', outHeight)
                        .append('g').selectAll('rect')
                        .data(comments)
                        .enter()
                        .append('rect')
                        .attr('x', function (d, i) {
                            return xScale(7 * d.level);
                        })
                        .attr('y', function (d, i) {
                            return yScale(i * 10);
                        })
                        .attr('width', function (d, i) {
                            return xScale(15 + d.influence * 5);
                        })
                        .attr('height', function (d, i) {
                            return yScale(conf.height);
                        })
                        .attr('fill', function (d, i) {
                            return interpolateColor(Math.abs(d.influence) / iRange);
                        })
                        .attr('title', function (d, i) {
                            return 'Click to scroll - ' + d.id;
                        })
                        .on('click', function(d, i) {
                            console.log('go to', d.id);
                        });
                });
            }
        }
    });