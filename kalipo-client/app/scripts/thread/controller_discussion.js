'use strict';

kalipoApp.controller('DiscussionController', ['$scope', '$routeParams', '$location', '$anchorScroll', '$rootScope', 'Thread', 'Comment', 'Report', 'Discussion', 'Websocket', 'Notifications', 'REPORT_IDS',
    function ($scope, $routeParams, $location, $anchorScroll, $rootScope, Thread, Comment, Report, Discussion, Websocket, Notifications, REPORT_IDS) {

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

        var tree = {};
        var currentPage = 0;


        // -- Initialization -- ----------------------------------------------------------------------------------------

        var onFetchedPage = function (result) {
            $scope.pages.push(result.page);
            $scope.$isLastPage = result.isLastPage;
            $scope.$isEmptyDiscussion = result.totalElements==0;

            if(result.numberOfElements > 0) {
                console.log('event:fetched-page -> ...');
                $rootScope.$broadcast('event:fetched-page');
            }
        };

        var firstFetch = function() {
            Discussion.fetch(threadId, 0, tree, function(result) {
                onFetchedPage(result);

                setTimeout(function() {
                    console.log('event:fetched-first-page -> ...');
                    $rootScope.$broadcast('event:fetched-first-page');
                }, 2000);
            });
        };

        firstFetch();

        // -- Socket -- ------------------------------------------------------------------------------------------------

        var socket = Websocket.subscribe(function (message) {
            if (message.threadId == threadId) {
                console.log('event', message);
                $scope.$missedCommentCount += 1;
                Comment.get({id: Websocket.getCommentId(message)}, function (comment) {
                    $rootScope.$broadcast('event:comment', comment, message.type);
                    // todo update tree if possible
                });
            }
        });

        $scope.$on("$destroy", function () {
            console.log('unsubscribe');
            Websocket.unsubscribe(socket);
        });


        // -- Scope Functions -- ---------------------------------------------------------------------------------------

        var loadMore = function () {
            if(!$scope.$isLastPage) {
                console.log("load more");

                currentPage = currentPage + 1;

                Discussion.fetch(threadId, currentPage, tree, onFetchedPage);
            }

        };

        $scope.reload = function() {
            currentPage = 0;
            $scope.$missedCommentCount = 0;
            $scope.pages = [];

            firstFetch();
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

            if (!_.isUndefined($scope.thread.$modIds)) {
                $scope.thread.modIds = _.compact($scope.thread.$modIds.replace(re, ' ').split(' '));
            }
            //if (!_.isUndefined($scope.thread.$kLine)) {
            //    $scope.thread.kLine = _.compact($scope.thread.$kLine.replace(re, ' ').split(' '));
            //}
            if (!_.isUndefined($scope.thread.$uriHooks)) {
                $scope.thread.uriHooks = _.compact($scope.thread.$uriHooks.replace(re, ' ').split(' '));
            }
            Thread.update($scope.thread, function() {
                Notifications.info('Updated');
            });
        };

        $scope.submitReport = function () {

            console.log('submit report');

            $scope.report.reason = $scope.report.reason.id;

            Report.save($scope.report,
                function () {
                    Notifications.info('Report saved...');
                    $scope.report.reason = null;
                });
        };

    }]);
