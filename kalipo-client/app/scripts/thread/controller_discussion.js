'use strict';

kalipoApp.controller('DiscussionController', function ($scope, $sce, $routeParams, $location, $anchorScroll, AuthenticationSharedService, $rootScope, Thread, Comment, Report, Discussion, Websocket, Notifications, REPORT_IDS, $compile, $q, THREAD_STATUS, Vote, DISCUSSION_TYPES, $route) {

    var threadId = $routeParams.threadId;
    // todo impl scrolling to commentId
    var commentId = $location.hash();
    console.log('requesting commentId', commentId);

    $scope.discussionTypes = DISCUSSION_TYPES;
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
    var promiseAuth = function() {
        var deferred = $q.defer();

        if($rootScope.authenticated) {
            console.log('already authenticated');
            deferred.resolve();
        } else {
            $rootScope.$on('event:auth-loginConfirmed', function() {
                $('#loginModal').modal('hide');
                deferred.resolve();
            });

            var modal = $('#loginModal').modal();
        }

        return deferred.promise;
    };

    // -- Initialization -- ----------------------------------------------------------------------------------------

    $scope.thread = Thread.get({id: threadId});

    var onFetchedPage = function (result) {
        $scope.pages.push(result.page);
        $scope.$isLastPage = result.isLastPage;
        $scope.$isEmptyDiscussion = result.totalElements == 0;
        $scope.$busy = false;

        if (result.numberOfElements > 0) {
            $rootScope.$broadcast('redraw-outline', $scope.pages);
        }
    };

    // prevent routing of comment hashes
    var lastRoute = $route.current;
    var lastPath = $location.path();
    $scope.$on('$locationChangeSuccess', function(event) {
        // Want to prevent re-loading when going from same thread
        if (lastPath && $location.path() == lastPath) {
            $route.current = lastRoute;
        }
    });

    // outline uses window size
    $(window).unbind('resize').resize(function() {
        console.log('redraw');
        $rootScope.$broadcast('redraw-outline', $scope.pages);
    });

    $('.comment').on('mouseover' function() {
        var cid = $(this).attr('ng-comment-id');
//        $('.comment-in-outline-' + cid).addClass('')
    }).on('mouseover' function() {
        var cid = $(this).attr('ng-comment-id');
//        $('.comment-in-outline-' + cid).removeClass('')
    });

    var firstFetch = function () {
        var promiseFetch = Discussion.fetch(threadId, 0, tree);
        $q.when(promiseFetch).then(function(response) {
            onFetchedPage(response);

            if(commentId) {
                $(document).ready(function() {
                    Discussion.scrollTo(commentId);
                });
            }

            $rootScope.$broadcast('initialize-when-scrolled-listener');
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

    // -- Non-Auth Scope Functions -- ----------------------------------------------------------------------------------
    $scope.renderHtml = function (htmlCode) {
        return $sce.trustAsHtml(htmlCode);
    };


    var loadMore = function () {
        if (!$scope.$isLastPage) {
            $scope.$busy = true;

            currentPage = currentPage + 1;
            console.log('next page', currentPage);

            $q.when(Discussion.fetch(threadId, currentPage, tree)).then(onFetchedPage);
        }
    };

    $scope.loadMore = function () {
        loadMore();
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

    $scope.toggleOptionals = function (commentId) {

        $('#comment-' + commentId + ' > .replies.optionals').toggleClass('hidden');
        $rootScope.$broadcast('outline-viewport-changed');
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
        console.log('outline-viewport-changed -> ...');
        $('#comment-' + commentId).toggleClass('hiddenreplies');
        $rootScope.$broadcast('outline-viewport-changed');
    };


    // -- Auth Scope Functions -- --------------------------------------------------------------------------------------

    $scope.updateThread = function (thread) {
        promiseAuth().then(function() {
            Thread.save(thread, function () {
                Notifications.info('Updated');
            });
        });
    };

    $scope.submitComment = function () {

        $('#createCommentModal').modal('hide');

        promiseAuth().then(function() {

            $scope.draft.anonymous = false;

            Comment.save($scope.draft,
                function () {
                    Notifications.info('Saved');
                    $scope.draft = {};
                });
        });
    };

    $scope.markSpamComment = function (commentId) {
        promiseAuth().then(function() {
            Notifications.info('Spam ' + commentId);
            Comment.spam({
                id: commentId
            });
        });
    };

    $scope.deleteComment = function (commentId) {
        promiseAuth().then(function() {
            Notifications.info('Delete ' + commentId);
            Comment.delete({
                id: commentId
            });
        });
    };

    $scope.deleteThread = function (thread) {
        promiseAuth().then(function() {
            Notifications.info('Delete thread ' + thread.id);
            Thread.delete({
                id: thread.id
            });
        });
    };

    $scope.deleteCommentAndBanUser = function (commentId) {
        promiseAuth().then(function() {
            Notifications.info('Delete + Ban ' + commentId);
            Comment.deleteAndBan({
                id: commentId
            });
        });
    };

    $scope.like = function (commentId) {
        promiseAuth().then(function() {
            var vote = {like: true, commentId: commentId};

            Vote.save(vote, function (id) {
                Notifications.info('Mmh');
            });
        });
    };

    $scope.dislike = function (commentId) {
        promiseAuth().then(function() {
            var vote = {like: false, commentId: commentId};

            Vote.save(vote, function (id) {
                Notifications.info('Nah');
            });
        });
    };

    $scope.ignoreAuthorOf = function (commentId) {
        promiseAuth().then(function() {
            console.log('ignoreAuthorOf', commentId);
            Account.ignoreAuthor({commentId: commentId});
        });
    };
});
