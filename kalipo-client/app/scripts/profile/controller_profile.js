'use strict';

kalipoApp.controller('ProfileController', function ($rootScope, $scope, $q, Comment) {

    var promiseLogin = function() {
        var defer = $q.defer();
        if (_.isUndefined($rootScope.login)) {
            console.log('wait');
            $scope.$on('event:auth-authorized', function() {
                defer.resolve();
            });
        } else {
            defer.resolve();
        }
        return defer.promise;
    };

    $scope.reset = function () {
        $scope.$page = 0;
        $scope.$pageCount = 0;
        $scope.$lastPage = true;
        $scope.$firstPage = true;
    };

    $scope.nextPage = function (fetchName) {
        if ($scope.$page + 1 < $scope.$pageCount) {
            $scope.$page++;
            // todo remove eval
            eval('$scope.'+fetchName+'()');
        }
    };

    $scope.previousPage = function (fetchName) {
        if ($scope.$page > 0) {
            $scope.$page--;
            eval('$scope.'+fetchName+'()');
        }
    };

    $scope.fetchComments = function () {
        $q.when(promiseLogin).then(function() {
            Comment.get({'userId': $rootScope.login, 'page': $scope.$page}, function (response) {
                $scope.$pageCount = response.totalPages;
                $scope.$lastPage = response.lastPage;
                $scope.$firstPage = response.firstPage;
                $scope.entities = response.content;
            })
        });
    };

    $scope.fetchIgnoredUsers = function () {
        $q.when(promiseLogin).then(function() {
            // todo implement
        });
    };
});
