'use strict';

kalipoApp.controller('PromoteThreadController', ['$scope', '$routeParams', 'Thread',
    function ($scope, $routeParams, Thread) {

        $scope.threadId = $routeParams.threadId;

}]);