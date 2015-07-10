'use strict';

kalipoApp.controller('ModerateController', function ($scope, $routeParams, $rootScope, Thread, Comment, COMMENT_STATUS, $q) {

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

    $scope.nextPage = function (fetchMethodName) {
        if ($scope.$page + 1 < $scope.$pageCount) {
            $scope.$page++;
            // todo remove eval
            eval('$scope.' + fetchMethodName + '()');
        }
    };

    $scope.previousPage = function (fetchMethodName) {
        if ($scope.$page > 0) {
            $scope.$page--;
            eval('$scope.' + fetchMethodName + '()');
        }
    };

    var handleResponse = function (response) {
         $scope.$pageCount = response.totalPages;
         $scope.$lastPage = response.lastPage;
         $scope.$firstPage = response.firstPage;
         $scope.entities = response.content;
     };

    $scope.queryPending = function () {
        $q.when(promiseLogin).then(function() {
            Comment.query({status: COMMENT_STATUS.PENDING, page: $scope.$page}, handleResponse);
        });
    };

    $scope.queryReports = function () {
        $q.when(promiseLogin).then(function() {
            Comment.query({reported: true, page: $scope.$page}, handleResponse);
        });
    };

    $scope.approveComment = function (id) {
        Comment.approve({id: id},
            function () {
                // todo remove comment
            });
    };

    $scope.rejectComment = function (id) {
        Comment.reject({id: id},
            function () {
                // todo remove comment
            });
    };

});
