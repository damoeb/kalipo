'use strict';

kalipoApp.controller('ThreadController', ['$scope', '$routeParams', 'Thread', 'Comment', 'Notifications',
    function ($scope, $routeParams, Thread, Comment, Notifications) {

        var threadId = $routeParams.threadId;

        $scope.$threadId = threadId;

        Thread.get({id: threadId}, function (thread) {
//            thread.uglyDucklingSurvivalEndDate = null;

            thread.$kLine = thread.kLine.join(', ');
            thread.$uriHooks = thread.uriHooks.join('\n');
            thread.$modIds = thread.modIds.join(' ').trim();

            $scope.$pendingCount = thread.pendingCount;
            $scope.$reportCount = thread.reportedCount;

            $scope.thread = thread;
        });

        $scope.draft = {};

        // todo merge with showReplyModal
        $scope.submitFirstComment = function () {

            // todo support anon flag in view
            $scope.draft.anonymous = false;
            $scope.draft.threadId = threadId;

            console.log('submit first comment', $scope.draft);

            Comment.save($scope.draft,
                function () {
                    Notifications.info('Comment saved');
                });
        };

    }]);
