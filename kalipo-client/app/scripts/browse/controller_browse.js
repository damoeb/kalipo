'use strict';

kalipoApp.controller('BrowseController', function ($scope, Thread, Comment) {

    Thread.query(function (threads) {
        $scope.threads = threads;

        _.forEach(threads, function (thread) {
            Comment.get({id: thread.leadCommentId}, function (comment) {
                thread.leadComment = comment;
            });
        });
    });

});
