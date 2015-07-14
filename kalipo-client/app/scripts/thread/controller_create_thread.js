'use strict';

kalipoApp.controller('CreateThreadController', function ($scope, $location, Thread) {

    $scope.thread = {};

    $scope.create = function () {

        Thread.save($scope.thread,
            function (data) {
                $location.path('/comments/' + data.id);
            });
    };

});
