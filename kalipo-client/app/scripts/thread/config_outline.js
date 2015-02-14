'use strict';

kalipoApp.constant('OutlineConfig', {
    bar_height: 7,
    bar_marginBottom: 3,
    yOffsetForRoots: 8, // if a level=0 comment occurs
    commentsOnPage: 200,
    level_xOffset: 5,
    bar_width: 15,
    bar_influenceBoost: 5,
    colorInterpolator: d3.scale.linear()
        .domain([0, 1])
        .interpolate(d3.interpolateRgb)
        .range(['#cccccc', '#0000ff']) // lightgray - blue
});
