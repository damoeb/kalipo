'use strict';

kalipoApp.controller('ModerateController', ['$scope', '$routeParams', '$rootScope', 'Thread', 'Comment', 'Report', 'Vote', '$log', '$location', '$anchorScroll',
    function ($scope, $routeParams, $rootScope, Thread, Comment, Report, Vote, $log, $location, $anchorScroll) {

    $scope.approve = function (id) {
        Comment.approve({id: id},
            function () {
                // todo remove
            });
    };

    $scope.reject = function (id) {
        Comment.reject({id: id},
            function () {
                // todo remove
            });
    };

    $scope.delete = function (id) {
        Comment.delete({id: id},
            function () {
                $scope.comments = Comment.query();
            });
    };

    }]);
