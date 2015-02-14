'use strict';

kalipoApp.controller('DiscussionController', ['$scope', '$routeParams', '$location', '$anchorScroll', '$rootScope', 'Thread', 'Comment', 'Report', 'Discussion', 'Websocket',
    function ($scope, $routeParams, $location, $anchorScroll, $rootScope, Thread, Comment, Report, Discussion, Websocket) {

        var threadId = $routeParams.threadId;
        // todo impl scrolling to commentId
        var commentId = $routeParams.commentId;

        $scope.pages = [];
        $scope.$threadId = threadId;
        $scope.$viewMode = false;
        $scope.draft = {};
        $scope.thread = {};
        $scope.reportModel = {};
        $scope.$showPending = false;
        $scope.$pendingCount = 0;
        $scope.$reportCount = 0;
        $scope.$hasReports = false;
        $scope.$isLastPage = true;

        var tree = {};
        var currentPage = 0;

        var onFetchedPage = function (result) {
            $scope.pages.push(result.page);
            $scope.$isLastPage = result.isLastPage;

            console.log('event:fetched-page -> ...');
            $rootScope.$broadcast('event:fetched-page');
        };

        Discussion.fetchPage(threadId, 0, tree, onFetchedPage);

        // -------------------------------------------------------------------------------------------------------------

        $scope.loadMore = function () {
            console.log("load more");

            currentPage = currentPage + 1;

            Discussion.fetchPage(threadId, currentPage, tree, onFetchedPage);

            $scope.scrollTo('page-' + currentPage);
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
            if (!_.isUndefined($scope.thread.$kLine)) {
                $scope.thread.kLine = _.compact($scope.thread.$kLine.replace(re, ' ').split(' '));
            }
            if (!_.isUndefined($scope.thread.$uriHooks)) {
                $scope.thread.uriHooks = _.compact($scope.thread.$uriHooks.replace(re, ' ').split(' '));
            }
            Thread.update($scope.thread, function() {
                // done
            });
        };

        $scope.submit = function () {

            $scope.draft.threadId = threadId;
            // todo support anon flag in view
            $scope.draft.anonymous = false;

            Comment.save($scope.draft,
                function () {
                    $scope.clear();
                });
        };

        $scope.report = function (comment) {
            comment.report = false;

            $scope.reportModel.commentId = comment.id;

            Report.save($scope.reportModel,
                function () {
                    $scope.report.reason = null;
                });
        };

        $scope.clear = function () {
            $scope.draft = {id: null, text: null};
        };

        // --

        var socket = Websocket.subscribe(function (message) {
            console.log('event', message);
            if (message.threadId == threadId) {

            }
        });

        $scope.$on("$destroy", function () {
            console.log('unsubscribe');
            Websocket.unsubscribe(socket);
        });

    }]);
