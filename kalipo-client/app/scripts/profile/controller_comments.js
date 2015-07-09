'use strict';

kalipoApp.controller('ProfileCommentsController', function ($scope, $rootScope, Comment) {

    $scope.$page = 0;
    $scope.$pageCount = 0;
    $scope.$lastPage = true;
    $scope.$firstPage = true;

    $scope.nextPage = function () {
        if ($scope.$page + 1 < $scope.$pageCount) {
            $scope.$page++;
            $scope.fetch();
        }
    };

    $scope.previousPage = function () {
        if ($scope.$page > 0) {
            $scope.$page--;
            $scope.fetch();
        }
    };

    $scope.fetch = function () {
        __fetch(function(response) {
            $scope.$pageCount = response.totalPages;
            $scope.$lastPage = response.lastPage;
            $scope.$firstPage = response.firstPage;
            $scope.entities = response.content;
        });
    };

    var __fetch = function (onSuccess) {

        var __doFetch = function () {
            Comment.get({'userId': $rootScope.login, 'page': $scope.$page}, function (response) {
                if(_.isFunction(onSuccess)) {
                    onSuccess(response);
                }
            })
        };

        // todo you have to be logged in to see this page
        if (typeof($rootScope.login) == 'undefined') {
            console.log('wait');
            $scope.$on('event:auth-authorized', __doFetch);
        } else {
            __doFetch();
        }
    };


    $scope.fetch();

});
