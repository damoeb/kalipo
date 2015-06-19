'use strict';

kalipoApp.controller('ProfileController', ['$scope', '$routeParams',
    function ($scope, $routeParams) {
        $scope.tab = $routeParams.tabId;
        console.log('tab', $scope.tab);
    }]);
