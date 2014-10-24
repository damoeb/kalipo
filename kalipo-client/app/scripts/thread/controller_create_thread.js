'use strict';

kalipoApp.controller('CreateThreadController', function ($scope, Thread) {

    $scope.create = function () {

        if ($scope.thread.tags) {
            $scope.thread.tags = $scope.thread.tags.split(' ');
        }

        Thread.save($scope.thread,
            function (data) {
                // todo go to promote thread
                noty({text: 'Thread created', type: 'success'});
            });
    };

});
