'use strict';

kalipoApp.factory('Outline', function (Thread) {

    var internal = {};

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
                __flat(sink, comment.replies.verbose);
            })
        }
    };

    return {}
});
