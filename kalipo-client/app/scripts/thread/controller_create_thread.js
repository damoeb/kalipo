'use strict';

kalipoApp.controller('CreateThreadController', function ($scope, $location, Thread) {

    $scope.thread = {};

    $scope.create = function () {

        var re = new RegExp('[, \n\t]+', 'g');

        if ($scope.thread.$modIds) {
            $scope.thread.modIds = _.compact($scope.thread.$modIds.replace(re, ' ').split(' '));
        }

        if ($scope.thread.$uriHooks) {
            $scope.thread.uriHooks = _.compact($scope.thread.$uriHooks.replace(re, ' ').split(' '));
        }

        Thread.save($scope.thread,
            function (data) {
                $location.path('/v/' + data.id);
            });
    };

});
