'use strict';

//'Comment', 'Report', 'Vote',

kalipoApp.controller('ViewThreadController', ['$scope', '$routeParams', 'Thread', '$log', '$location', '$anchorScroll',
    function ($scope, $routeParams, Thread, $log, $location, $anchorScroll) {

        $scope.draft = {};
        $scope.thread = {};
        $scope.reportModel = {};
        $scope.pendingCount = 0;
        $scope.commentCount = 0;
        $scope.reportCount = 0;

        var threadId = $routeParams.id;

        Thread.get({id: threadId}, function (thread) {
            $scope.thread = thread;
        });

        Thread.getComments({id: threadId}, function (comments) {
            $scope.comments = comments;
        });


        $scope.create = function () {

            $scope.draft.threadId = threadId;

            Comment.save($scope.draft,
                function () {
                    $scope.clear();

                    $scope.refresh();
                });
        };

        $scope.tree = function (comments) {

            var map = {};
            var roots = [];

            for (var i in comments) {
                var comment = comments[i];
                map[comment.id] = comment;
                comment.subcomments = [];
                comment.report = false;
                comment.maximized = true;
            }

            $.each(comments, function (index, comment) {
                if (comment.level == 0) {
                    roots.push(comment);
                } else {
                    var _parent = map[comment.parentId];
                    _parent.subcomments.push(comment);
                }
            });

            return roots;

        };

        $scope.toggleReplyForm = function (comment) {

            $scope.draft.title = comment.title;
            comment.reply = !comment.reply;
            comment.report = false;
        };

        $scope.toggleReportForm = function (comment) {
            comment.reply = false;
            comment.report = !comment.report;
        };

        $scope.report = function (comment) {
            comment.report = false;

            $scope.reportModel.commentId = comment.id;

            Report.save($scope.reportModel,
                function () {
                    $scope.report.reason = null;
                });
        };

        $scope.like = function (comment) {
            comment.likes++;

            var vote = {like: true, commentId: comment.id};

            Vote.save(vote, function (updated) {
                $log.log(updated);
            });
        };

        $scope.cancelRelyTo = function () {
            $scope.draft.parentId = null;
        };

        $scope.dislike = function (comment) {
            var vote = {like: false, commentId: comment.id};

            Vote.save(vote, function (updated) {
                $log.log(updated);
            });
        };

//        $scope.delete = function (id) {
//            Comment.delete({id: id},
//                function () {
//                    $scope.comments = Comment.query();
//                });
//        };

        $scope.clear = function () {
            $scope.draft = {id: null, text: null};
        };

        $scope.scrollTo = function (id) {
            var old = $location.hash();
            $location.hash(id);
            $anchorScroll();
            //reset to old to keep any additional routing logic from kicking in
            $location.hash(old);
        };

    }]);