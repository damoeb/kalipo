'use strict';

kalipoApp.controller('PendingController', ['$scope', '$routeParams', '$rootScope', 'Thread', 'Comment',
    function ($scope, $routeParams, $rootScope, Thread, Comment) {

        Comment.pendingInThread({thread: $routeParams.threadId}, function (comments) {
            $scope.pending = comments;
        });

        $scope.approveComment = function (id) {
            Comment.approve({id: id},
                function () {
                    // todo remove
                });
        };

        $scope.rejectComment = function (id) {
            Comment.reject({id: id},
                function () {
                    // todo remove
                });
        };

    }]);
