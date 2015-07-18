'use strict';

kalipoApp.controller('DiscussionController', function ($scope, $routeParams, $location, $anchorScroll, $rootScope, Thread, Comment, Report, Discussion, Websocket, Notifications, REPORT_IDS, $compile, $q, THREAD_STATUS, Vote) {

    var threadId = $routeParams.threadId;
    // todo impl scrolling to commentId
    var commentId = $routeParams.commentId;

    $scope.pages = [];
    $scope.$threadId = threadId;
    $scope.reportModel = {};
    $scope.$showPending = false;
    $scope.$pendingCount = 0;
    $scope.$hasReports = false;
    $scope.$isLastPage = false;
    $scope.report = {};
    $scope.reportOptions = REPORT_IDS;
    $scope.$busy = false;
    $scope.threadStatusList = THREAD_STATUS;
    $scope.draft = {
        threadId: threadId
    };
    $scope.visitorCount = 0;

    var tree = {};
    var currentPage = 0;
    var promiseTemplates = Discussion.init();

    // -- Initialization -- ----------------------------------------------------------------------------------------

    $scope.thread = Thread.get({id: threadId});

    var onFetchedPage = function (result) {
        $scope.pages.push(result.page);
        $scope.$isLastPage = result.isLastPage;
        $scope.$isEmptyDiscussion = result.totalElements == 0;
        $scope.$busy = false;

        if (result.numberOfElements > 0) {
            $rootScope.$broadcast('fetched-page', $scope.pages);
        }
    };

    var firstFetch = function () {
        var promiseFetch = Discussion.fetch(threadId, 0, tree);
        $q.when(promiseFetch).then(function(response) {
            onFetchedPage(response);
            $rootScope.$broadcast('init-when-scrolled');
        });
    };

    firstFetch();

    // -- Socket -- ------------------------------------------------------------------------------------------------

    $q.when(promiseTemplates).then(function () {

        var socket = Websocket.subscribe(threadId, function (message) {
//            console.log('event', message);

            if(message.type == 'STATS') {
                $scope.visitorCount = message.data;
                $scope.$apply();
            } else {
                Comment.get({id: Websocket.getCommentId(message)}, function (comment) {

                    comment['$new'] = true;

                    var $comment = $('<div/>');
                    Discussion.renderComment(comment, $comment, false);

                    var $reference = $('#comment-' + comment.id);
                    //console.log('ref', $reference);

                    if ($reference.length == 0) {
                        // is root comment
                        if (_.isUndefined(comment.parentId)) {
                            $('discussion').prepend($compile($comment.contents())($scope));

                        } else {
                            var $parent = $('#comment-' + comment.parentId + '> .replies');
                            //console.log('append to', $parent);
                            $parent.prepend($compile($comment.contents())($scope));
                        }
                    } else {
                        // replace
                        var $container = $reference.children('.comment:first-child');
                        //console.log('replace', $container);
                        var $replaceBy = $comment.children('.comment-wrapper').children('.comment');
                        //console.log('replace by', $replaceBy);
                        $container.empty().append($compile($replaceBy.contents())($scope));
                    }
                });
            }
        });

        $scope.$on("$destroy", function () {
            console.log('unsubscribe');
            Websocket.unsubscribe(socket);
        });
    });

    // -- Scope Functions -- ---------------------------------------------------------------------------------------

    var loadMore = function () {
        if (!$scope.$isLastPage) {
            $scope.$busy = true;
            console.log("load more");

            currentPage = currentPage + 1;

            $q.when(Discussion.fetch(threadId, currentPage, tree)).then(onFetchedPage);
        }
    };

    $scope.loadMore = function () {
        loadMore();
    };

    $scope.scrollTo = function (id) {
        console.log('scroll to comment', id);
        var old = $location.hash();
        $location.hash(id);
        $anchorScroll();
        //reset to old to keep any additional routing logic from kicking in
        $location.hash(old);
    };

    $scope.updateThread = function (thread) {

        Thread.save(thread, function () {
            Notifications.info('Updated');
        });
    };

    $scope.submitReport = function () {

        console.log('submit report');

        $scope.report.reason = $scope.report.reason.id;

        Report.save($scope.report,
            function () {
                $('#reportCommentModal').modal('hide');
                Notifications.info('Report submitted');
                $scope.report.reason = null;
            });
    };

    $scope.showReplyModal = function (commentId) {

        $('#createCommentModal').modal();
        $scope.draft.threadId = threadId;
        $scope.draft.parentId = commentId;
    };

    $scope.submitComment = function () {

        // todo support anon flag in view
        $scope.draft.anonymous = false;

        Comment.save($scope.draft,
            function () {
                Notifications.info('Saved');
                $('#createCommentModal').modal('hide');
                $scope.draft = {};
            });
    };

    // --

    $scope.markSpamComment = function (commentId) {
        Notifications.info('Spam ' + commentId);
        Comment.spam({
            id: commentId
        });
    };

    $scope.deleteComment = function (commentId) {
        Notifications.info('Delete ' + commentId);
        Comment.delete({
            id: commentId
        });
    };

    $scope.deleteThread = function (thread) {
        Notifications.info('Delete thread ' + thread.id);
        Thread.delete({
            id: thread.id
        });
    };

    $scope.deleteCommentAndBanUser = function (commentId) {
        Notifications.info('Delete + Ban ' + commentId);
        Comment.deleteAndBan({
            id: commentId
        });
    };

    $scope.like = function (commentId) {
        commentId.likes++;

        var vote = {like: true, commentId: commentId};

        Vote.save(vote, function (id) {
            Notifications.info('Mmh');
        });
    };

    $scope.dislike = function (commentId) {
        commentId.dislikes++;

        var vote = {like: false, commentId: commentId};

        Vote.save(vote, function (id) {
            Notifications.info('Nah');
        });
    };

    $scope.toggleOptionals = function (commentId) {

        $('#comment-' + commentId + ' > .replies.optionals').toggleClass('hidden');
        $rootScope.$broadcast('refresh-outline-viewport');
    };

    $scope.ignoreAuthorOf = function (commentId) {
        console.log('ignoreAuthorOf', commentId);
        Account.ignoreAuthor({commentId: commentId});
    };

    $scope.showReportModal = function (commentId, displayName) {
        console.log('report modal', commentId);

        $('#reportCommentModal').modal();

        $scope.displayName = displayName;
        $scope.report.commentId = commentId;
    };

    $scope.verbose = function (commentId) {
        $('#comment-' + commentId).removeClass('oneline');
    };

    $scope.toggleReplies = function (commentId) {
        console.log('refresh-outline-viewport -> ...');
        $('#comment-' + commentId).toggleClass('hiddenreplies');
        $rootScope.$broadcast('refresh-outline-viewport');
    };
});
