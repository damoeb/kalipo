'use strict';

kalipoApp.factory('Outline', function (Thread) {

    var internal = {};

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

    return {}
});
