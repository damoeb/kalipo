'use strict';

kalipoApp.controller('ThreadController', ['$scope', '$routeParams', 'Thread',
    function ($scope, $routeParams, Thread) {

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

    }]);
