'use strict';

kalipoApp.controller('ThreadController',
    function ($scope, $routeParams, Thread, Comment, Notifications, THREAD_STATUS) {

        var threadId = $routeParams.threadId;

        $scope.$threadId = threadId;
        $scope.threadStatusList = THREAD_STATUS;
        $scope.draft = {};

        Thread.get({id: threadId}, function (thread) {
            $scope.$pendingCount = thread.pendingCount;
            $scope.thread = thread;
        });

        // --

        $scope.showReplyModal = function (commentId, quote) {

            console.log('reply modal', commentId);

            $('#createCommentModal').modal();
            $scope.draft.threadId = threadId;

            //if(_.isUndefined(quote) || quote.length==0) {
            //    $scope.draft.body = '';
            //} else {
            //    $scope.draft.body = '>' + quote.replace(/\n/g, '>\n');
            //}
            $scope.draft.parentId = commentId;
        };

        $scope.submitComment = function () {

//            console.log('submitting comment');
            // todo support anon flag in view
            $scope.draft.anonymous = false;

            Comment.save($scope.draft,
                function () {
                    Notifications.info('Saved');
                    $('#createCommentModal').modal('hide');
                    $scope.draft = {};
                });
        };

        // --

        $scope.markSpamComment = function (commentId) {
            Notifications.info('Spam ' + commentId);
            Comment.spam({
                id: commentId
            });
        };

        $scope.deleteComment = function (commentId) {
            Notifications.info('Delete ' + commentId);
            Comment.delete({
                id: commentId
            });
        };

        $scope.deleteThread = function (thread) {
            Notifications.info('Delete thread ' + thread.id);
            Thread.delete({
                id: thread.id
            });
        };

        $scope.deleteCommentAndBanUser = function (commentId) {
            Notifications.info('Delete + Ban ' + commentId);
            Comment.deleteAndBan({
                id: commentId
            });
        };
    });
