'use strict';

kalipoApp.controller('ReportController', function ($scope, resolvedReport, Report, Comment) {

    var reports = resolvedReport;

    $scope.comments = [];

    var byCommentId = _.groupBy(reports, function (report) {
        return report.commentId;
    });

    _.each(byCommentId, function (reports, commentId) {
        Comment.get({id: commentId}, function (comment) {
            comment.rreports = reports;
            $scope.comments.push(comment);
        });
    });

    $scope.clear = function () {
        $scope.report = {id: null, commentId: null, threadId: null, reason: null, status: null};
    };
});
