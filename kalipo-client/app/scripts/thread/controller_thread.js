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

        $scope.submitFirstComment = function () {

            console.log('submit first comment', $scope.draft);
            // todo support anon flag in view
            $scope.draft.anonymous = false;

            Comment.save($scope.draft,
                function () {
                    Notifications.info('Comment saved');
                });
        };

    }]);
