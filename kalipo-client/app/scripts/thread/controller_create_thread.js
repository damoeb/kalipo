'use strict';

kalipoApp.controller('CreateThreadController', function ($scope, $location, Thread, DISCUSSION_TYPES) {

    $scope.thread = {};
    $scope.discussionTypes = DISCUSSION_TYPES;

    $scope.create = function () {

        Thread.save($scope.thread,
            function (data) {
                $location.path('/comments/' + data.id);
            });
    };

});
