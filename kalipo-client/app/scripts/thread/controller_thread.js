'use strict';

kalipoApp.controller('ThreadController', function ($scope, resolvedThread, Thread) {

        $scope.threads = resolvedThread;

        $scope.create = function () {
            Thread.save($scope.thread,
                function () {
                    $scope.threads = Thread.query();
                    $('#saveThreadModal').modal('hide');
                    $scope.clear();
                });
        };

        $scope.update = function (id) {
            $scope.thread = Thread.get({id: id});
            $('#saveThreadModal').modal('show');
        };

        $scope.delete = function (id) {
            Thread.delete({id: id},
                function () {
                    $scope.threads = Thread.query();
                });
        };

        $scope.clear = function () {
            $scope.thread = {id: null, uriHook: null, title: null, status: null};
        };
    });
