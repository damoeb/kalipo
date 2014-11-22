'use strict';

kalipoApp.controller('EditThreadController', ['$scope', '$routeParams', '$location', 'Thread',
    function ($scope, $routeParams, $location, Thread) {

        Thread, get({id: $routeParams.threadId})

        $scope.thread = {};

        $scope.save = function () {

            Thread.save($scope.thread,
                function (data) {
//                $location.path('/thread/' + data.id + '/share');
                });
        };
    }]);