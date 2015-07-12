'use strict';

kalipoApp.controller('ModerateController', function ($scope, $routeParams, $rootScope, Thread, Comment, Report, COMMENT_STATUS, REPORT_STATUS, $q, Notifications) {

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

    $scope.queryBans = function () {
        $q.when(promiseLogin).then(function() {
            // todo implement
        });
    };

    $scope.queryReports = function () {
        $q.when(promiseLogin).then(function() {
            Report.query({status: REPORT_STATUS.PENDING, page: $scope.$page}, handleResponse);
        });
    };

    var finalize = function(entity, message) {
        entity.$finalized = true;
        entity.$reason = message;
        Notifications.info(message);
    }

    $scope.approveComment = function (comment) {
        Comment.approve({id: comment.id}, function () { finalize(comment, 'Approved'); });
    };

    $scope.rejectComment = function (comment) {
        Comment.reject({id: comment.id}, function () { finalize(comment, 'Rejected'); });
    };

    $scope.approveReport = function (report) {
        Report.approve({id: report.id}, function () { finalize(report, 'Approved'); });
    };

    $scope.rejectReport = function (report) {
        Report.reject({id: report.id}, function () { finalize(report, 'Rejected'); });
    };


});
