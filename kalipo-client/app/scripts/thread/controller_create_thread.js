'use strict';

kalipoApp.controller('CreateThreadController', function ($scope, Thread) {

    $scope.create = function () {
        Thread.save($scope.thread,
            function (data) {
                // todo redirect to thread
                noty({text: 'Thread created', type: 'success'});
            });
    };

});
