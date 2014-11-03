'use strict';

kalipoApp.controller('ViewThreadController', ['$scope', '$routeParams', 'Thread', 'Comment', 'Report', 'Vote', '$log', '$location', '$anchorScroll',
    function ($scope, $routeParams, Thread, Comment, Report, Vote, $log, $location, $anchorScroll) {

        if ($location.path().endsWith('share')) {
            $scope.share = 'social';
        } else {
            $scope.share = null;
        }

        $scope.draft = {};
        $scope.thread = {};
        $scope.reportModel = {};
        $scope.pendingCount = 0;
        $scope.commentCount = 0;
        $scope.reportCount = 0;
        $scope.doComment = false;

        var threadId = $routeParams.threadId;
        var commentId = $routeParams.commentId;

        Thread.get({id: threadId}, function (thread) {
            $scope.thread = thread;
        });

        Thread.getComments({id: threadId}, function (comments) {
            $scope.comments = _cluster(comments);

            if (commentId) {
                noty({text: 'Go to comment ' + commentId});
                $scope.scrollTo(commentId);
            }
        });

        $scope.toggleShareComponent = function () {
            if ($scope.share == null) {
                $scope.share = 'social';
            } else {
                $scope.share = null;
            }
        };

        $scope.create = function () {

            $scope.draft.threadId = threadId;

            Comment.save($scope.draft,
                function () {
                    $scope.clear();
                });
        };

        var _cluster = function (comments) {

            var map = {};
            var roots = [];

            for (var i in comments) {
                var comment = comments[i];
                map[comment.id] = comment;
                comment.subcomments = [];
                comment.$report = false;

                // todo minimize negative-only comments, hell-banned subthreads

                comment.$maximized = true;

                var total = comment.likes + comment.dislikes;
                comment.$likes = comment.likes / total * 100;
                comment.$dislikes = comment.dislikes / total * 100;
            }

            $.each(comments, function (index, comment) {
                if (comment.parentId == null) {
                    roots.push(comment);
                } else {
                    var _parent = map[comment.parentId];
                    _parent.subcomments.push(comment);
                }
            });

            var get$commentCount = function (index, comment) {

                comment.$commentCount = 1;

                for(var i=0; i < comment.subcomments.length; i++) {
                    var subcomment = comment.subcomments[i];
                    comment.$commentCount += get$commentCount(0, subcomment);
                }

                console.log(comment.$commentCount);

                return comment.$commentCount;
            };

            $.each(roots, get$commentCount);

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

            var vote = {isLike: true, commentId: comment.id};

            Vote.save(vote, function (updated) {
//                noty({text: 'Liked', type: 'success'});
            });
        };

        $scope.cancelRelyTo = function () {
            $scope.draft.parentId = null;
        };

        $scope.dislike = function (comment) {
            comment.dislikes++;

            var vote = {isLike: false, commentId: comment.id};

            Vote.save(vote, function (updated) {
//                noty({text: 'Disliked', type: 'success'});
            });
        };

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