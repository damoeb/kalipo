'use strict';

kalipoApp.controller('CreateThreadController', function ($scope, Thread) {

    $scope.create = function () {

        $scope.thread.tags = $scope.thread.tags.split(' ');

        Thread.save($scope.thread,
            function (data) {
                // todo redirect to thread
                noty({text: 'Thread created', type: 'success'});
            });
    };

});
