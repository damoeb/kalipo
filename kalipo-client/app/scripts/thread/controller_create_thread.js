'use strict';

kalipoApp.controller('CreateThreadController', function ($scope, $location, Thread) {

    $scope.thread = {};

    $scope.create = function () {

        $scope.thread.modIds = _.compact($scope.thread.$mods.split(' ,;'));
        $scope.thread.uriHooks = _.compact($scope.thread.$hooks.split(' ,;'));

        Thread.save($scope.thread,
            function (data) {
                $location.path('/thread/' + data.id + '/share');
            });
    };

});
