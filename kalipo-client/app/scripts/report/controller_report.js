'use strict';


kalipoApp.controller('ReportController', ['$scope', '$routeParams', '$rootScope', 'Report', 'Comment',
    function ($scope, $routeParams, $rootScope, Report, Comment) {
        $scope.reportedComments = [];

        Report.pendingInThread({thread: $routeParams.threadId}, function (reports) {
            $scope.reports = reports;

            var byCommentId = _.groupBy(reports, function (report) {
                return report.commentId;
            });

            // todo fix
            $scope.$parent.$reportCount = byCommentId.length;

            _.each(byCommentId, function (reports, commentId) {
                Comment.get({id: commentId}, function (comment) {
                    comment.rreports = reports;
                    $scope.reportedComments.push(comment);
                });
            });
        });

        $scope.approveReports = function (id) {
            Report.approve({id: id},
                function () {
                    // todo remove
                });
        };

        $scope.rejectReports = function (id) {
            Report.reject({id: id},
                function () {
                    // todo remove
                });
        };

    }]);
