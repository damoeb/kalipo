'use strict';

kalipoApp.controller('BrowseController', function ($scope, Thread, Comment) {

    Thread.query(function (threads) {
        $scope.threads = threads;
    });

});
