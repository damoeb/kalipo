'use strict';

kalipoApp.controller('ViewThreadController', ['$scope', '$routeParams', '$rootScope', 'Thread', 'Comment', 'Report', 'Vote', '$log', '$location', '$anchorScroll',
    function ($scope, $routeParams, $rootScope, Thread, Comment, Report, Vote, $log, $location, $anchorScroll) {
        if ($location.path().endsWith('share')) {
            $scope.more = 'social';
        } else {
            $scope.more = null;
        }

        $scope.textCommentHere = 'Add a comment...';

        $scope.draft = {};
        $scope.thread = {};
        $scope.reportModel = {};
        $scope.$doComment = false;

        var threadId = $routeParams.threadId;
        var commentId = $routeParams.commentId;

        Thread.get({id: threadId}, function (thread) {
            $scope.thread = thread;
        });

        Thread.getComments({id: threadId}, function (comments) {

            $scope.comments = [];
            $scope.pending = [];

            var groups = _.groupBy(_.sortBy(comments, function(comment){return -comment.createdDate}), function(comment) {
                return comment.status == 'PENDING' ? 'PENDING' : 'OTHERS';
            });

            $scope.comments = _hierarchical(groups.OTHERS);
            $scope.pending = groups.PENDING;

//            todo enable scrollTo
//            if (commentId) {
//                noty({text: 'Go to comment ' + commentId});
//                $scope.scrollTo(commentId);
//            }
        });

        $scope.toggleShareComponent = function () {
            if ($scope.more == null) {
                $scope.more = 'social';
            } else {
                $scope.more = null;
            }
        };

        $scope.create = function () {

            $scope.draft.threadId = threadId;

            Comment.save($scope.draft,
                function () {
                    $scope.clear();
                });
        };

        var _hierarchical = function (comments) {

            var map = _.groupBy(comments, function(comment) {
                comment.children = [];
                comment.$report = false;
                comment.$commentCount = 1;

                if (comment.hidden) {
                    comment.text = 'Content hidden';
                    comment.dislikes = 0;
                    comment.likes = 0;
                }

                if (comment.status == 'DELETED') {
                    comment.authorId = 'Deleted';
                    comment.text = 'Content deleted';
                    comment.dislikes = 0;
                    comment.likes = 0;
                }

                // todo minimize negative-only comments, hell-banned subthreads

                comment.$maximized = !(comment.dislikes > 5 && comment.dislikes > comment.likes);

                var total = comment.likes + comment.dislikes;
                comment.$likes = comment.likes / total * 100;
                comment.$dislikes = comment.dislikes / total * 100;

                return comment.id;
            });

            return _.filter(comments, function(comment) {
                if (comment.parentId == null) {
                    return true;
                } else {
                    var parent = map[comment.parentId][0];
                    parent.children.push(comment);

                    // push commentCount to parents
                    var child = comment;
                    while(child.parentId) {
                        parent = map[child.parentId][0];
                        parent.$commentCount += 1;
                        child = parent;
                    }
                }
            });
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