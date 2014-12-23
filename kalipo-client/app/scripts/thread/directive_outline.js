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
                    .range(["#d3d3d3", "#0000ff"]); // lightgray - blue

                Thread.outline({id: threadId}, function (comments) {

                    var outWidth = 300;
                    var outHeight = $(window).height() * 4;

                    var yScale = d3.scale.linear()
                        .domain([0, 2400])
                        .range([0, outHeight]);

                    var xScale = d3.scale.linear()
                        .domain([0, 150])
                        .range([0, outWidth]);

                    var minI = _.min(comments, function(c) { return c.influence; }).influence;
                    var maxI = _.max(comments, function(c) { return c.influence; }).influence;
                    var iRange = Math.abs(minI) + Math.abs(maxI);

                    console.log('minI',minI, 'maxI',maxI, 'iRange',iRange);

                    d3.select("#klp-outline")
                        .attr("width", outWidth)
                        .attr("height", outHeight)
                        .append("g").selectAll("rect")
                        .data(comments)
                        .enter()
                        .append("rect")
                        .attr("x", function (d, i) {
                            return xScale(7 * d.level);
                        })
                        .attr("y", function (d, i) {
                            return yScale(i * 7);
                        })
                        .attr("width", function (d, i) {
                            return xScale(15 + d.influence * 5);
                        })
                        .attr("height", function (d, i) {
                            return yScale(5);
                        })
                        .attr("fill", function (d, i) {
                            return interpolateColor(Math.abs(d.influence) / iRange);
                        })
                        .attr("title", function (d, i) {
                            return 'Click to scroll - ' + d.id;
                        })
                        .on('mouseover', function(d, i) {
                            d3.select(this).attr("fill", 'black');
                        })
                        .on('mouseout', function(d, i) {
                            d3.select(this).attr("fill", interpolateColor(Math.abs(d.influence) / iRange));
                        });
                });
            }
        }
    });
