'use strict';

kalipoApp.controller('ThreadController', ['$scope', '$routeParams', 'Thread', 'Comment', 'Notifications',
    function ($scope, $routeParams, Thread, Comment, Notifications) {

        var threadId = $routeParams.threadId;

        $scope.$threadId = threadId;

        Thread.get({id: threadId}, function (thread) {
//            thread.uglyDucklingSurvivalEndDate = null;

            //thread.$kLine = thread.kLine.join(', ');
            thread.$uriHooks = thread.uriHooks.join('\n');
            thread.$modIds = thread.modIds.join(' ').trim();

            $scope.$pendingCount = thread.pendingCount;
            $scope.$reportCount = thread.reportedCount;

            $scope.thread = thread;
        });

        $scope.draft = {};

        // --

        $scope.showReplyModal = function (commentId, quote) {

            console.log('reply modal', commentId);

            $('#createCommentModal').modal();
            $scope.draft.threadId = threadId;
            if(_.isUndefined(quote) || quote.length==0) {
                $scope.draft.body = '';
            } else {
                $scope.draft.b3ody = '>' + quote.replace(/\n/g, '>\n');
            }
            $scope.draft.parentId = commentId;
        };

        $scope.submitComment = function () {

            console.log('submit comment', $scope.draft);
            // todo support anon flag in view
            $scope.draft.anonymous = false;

            Comment.save($scope.draft,
                function () {
                    Notifications.info('Comment saved');
                    $('#createCommentModal').modal('hide');
                });
        };

        // --

        $scope.markSpamComment = function(commentId) {
            Notifications.info('Spam '+commentId);
            // todo impl backend
        };

        $scope.deleteComment = function(commentId) {
            Notifications.info('Delete '+commentId);
            // todo impl backend
        };

        $scope.deleteCommentAndBlacklistUser = function(commentId) {
            Notifications.info('Delete + Blacklist '+commentId);
            // todo impl backend
        };
    }]);
