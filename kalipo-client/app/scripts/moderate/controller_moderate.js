'use strict';

kalipoApp.controller('ModerateController', ['$scope', '$routeParams',
    function ($scope, $routeParams) {

        var currentTab = $routeParams.tab;
        $scope.$threadId = $routeParams.threadId

        var tabs = ['pending', 'reports'];

        for (var i = 0; i < tabs.length; i++) {
            if (tabs[i] == currentTab) {
                $scope.$tab = tabs[i];
            }
        }
        if (_.isEmpty($scope.$tab)) {
            $scope.$tab = 'pending';
        }

    }]);
