'use strict';

kalipoApp.controller('DiscussionController', function ($scope, $routeParams, $location, $anchorScroll, $rootScope, Thread, Comment, Report, Discussion, Websocket, Notifications, REPORT_IDS, $compile, $q) {

    var threadId = $routeParams.threadId;
    // todo impl scrolling to commentId
    var commentId = $routeParams.commentId;

    $scope.pages = [];
    $scope.$threadId = threadId;
    $scope.$viewMode = false;
    $scope.thread = {};
    $scope.reportModel = {};
    $scope.$showPending = false;
    $scope.$pendingCount = 0;
    $scope.$reportCount = 0;
    $scope.$hasReports = false;
    $scope.$isLastPage = false;
    $scope.$missedCommentCount = 0;
    $scope.report = {};
    $scope.reportOptions = REPORT_IDS;
    $scope.$busy = false;

    var tree = {};
    var currentPage = 0;
    var promiseTemplates = Discussion.init();

    // -- Initialization -- ----------------------------------------------------------------------------------------

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

        var socket = Websocket.subscribe(function (message) {
            if (message.threadId == threadId) {
                console.log('event', message);
                Comment.get({id: Websocket.getCommentId(message)}, function (comment) {

                    comment['$new'] = true;

                    var $comment = $('<div/>');
                    Discussion.renderComment(comment, $comment, false);

                    var $reference = $('#comment-' + comment.id);
                    //console.log('ref', $reference);

                    if ($reference.length == 0) {
                        // is root comment
                        if (_.isUndefined(comment.parentId)) {
                            $scope.$missedCommentCount += 1;

                            // todo wie twitter: "2 neue Kommentare"
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

                    // todo update pages and refresh outline
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

    $scope.updateThread = function () {

        var re = new RegExp('[, \n\t]+', 'g');

        if (!_.isUndefined($scope.thread.$uriHooks)) {
            $scope.thread.uriHooks = _.compact($scope.thread.$uriHooks.replace(re, ' ').split(' '));
        }
        Thread.update($scope.thread, function () {
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

});
