'use strict';

kalipoApp.controller('ThreadController', ['$scope', '$routeParams', 'Thread', '$location', '$anchorScroll',
    function ($scope, $routeParams, Thread, $location, $anchorScroll) {

        var threadId = $routeParams.threadId;

        $scope.$threadId = threadId;

        // todo load on demand?
        Thread.get({id: threadId}, function (thread) {
//            thread.uglyDucklingSurvivalEndDate = null;

            thread.$kLine = thread.kLine.join(', ');
            thread.$uriHooks = thread.uriHooks.join('\n');
            thread.$modIds = thread.modIds.join(' ').trim();

            $scope.$pendingCount = thread.pendingCount;
            $scope.$reportCount = thread.reportedCount;

            $scope.thread = thread;
        });

        $scope.scrollTo = function (id) {
            var old = $location.hash();
            $location.hash(id);
            $anchorScroll();
            //reset to old to keep any additional routing logic from kicking in
            $location.hash(old);
        };

    }]);
