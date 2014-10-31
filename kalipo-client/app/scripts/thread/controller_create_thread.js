'use strict';

kalipoApp.controller('CreateThreadController', function ($scope, $location, Thread) {

    $scope.thread = {};

    $scope.create = function () {

        if ($scope.thread.tags) {
            $scope.thread.tags = $scope.thread.tags.split(' ');
        }

        Thread.save($scope.thread,
            function (data) {
                $location.path('/thread/' + data.id + '/share');
            });
    };

});
