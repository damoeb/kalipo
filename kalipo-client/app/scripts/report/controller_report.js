'use strict';


kalipoApp.controller('ReportController', ['$scope', '$routeParams', '$rootScope', 'Report', 'Comment', 'Notifications', 'REPORT_IDS',
    function ($scope, $routeParams, $rootScope, Report, Comment, Notifications, REPORT_IDS) {
        $scope.reportedComments = [];

        var byCommentId = {};

        Report.pendingInThread({thread: $routeParams.threadId}, function (reports) {
            $scope.reports = reports;

            // todo resolve reportId (also used in )

            byCommentId = _.groupBy(reports, function (report) {

                console.log('report', report);

                report.reason = _.result(_.find(REPORT_IDS, function (item) {
                    return item.id == report.reasonId;
                }), 'name');

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

        $scope.approveAllReports = function (commentId) {

            _.forEach(byCommentId[commentId], function (report) {
                console.log('approve report', report.id);
                Report.approve({id: report.id},
                    function () {
                        Notifications.info('Approved report of ' + report.authorId);
                    });
            });
        };

        $scope.rejectAllReports = function (commentId) {
            _.forEach(byCommentId[commentId], function (report) {
                console.log('reject report', report.id);
                Report.reject({id: report.id},
                    function () {
                        Notifications.info('Rejected report of ' + report.authorId);
                    });
            });
        };

    }]);
