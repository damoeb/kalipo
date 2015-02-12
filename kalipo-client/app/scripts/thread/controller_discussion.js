'use strict';

kalipoApp.controller('DiscussionController', ['$scope', '$routeParams', '$location', '$anchorScroll', '$rootScope', 'Thread', 'Comment', 'Report',
    function ($scope, $routeParams, $location, $anchorScroll, $rootScope, Thread, Comment, Report) {

        var threadId = $routeParams.threadId;
        var commentId = $routeParams.commentId;

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

        $scope.pages = [];

        var tree = {};

        var $this = this;

        $this.groupedByIdMaster = {};

        var currentPage = -1;

        $scope.loadMore = function () {
            console.log("load more");
            __fetchComments();
        };

        $scope.scrollTo = function (id) {
            console.log('scroll to comment', id);
            var old = $location.hash();
            $location.hash(id);
            $anchorScroll();
            //reset to old to keep any additional routing logic from kicking in
            $location.hash(old);
        };

        var __fetchComments = function () {

            currentPage = currentPage + 1;

            var start = new Date().getTime();

            Thread.discussion({id: threadId, page: currentPage}, function (pageData) {

                $scope.$isLastPage = pageData.lastPage;

                var end = new Date().getTime();
                console.log('Fetch time: ' + (end - start));

                start = new Date().getTime();

                var comments = __postFetchComments(pageData.content, currentPage);

                var roots = __sort(__mergeWithTree(tree, comments));
                __shape(roots);

                var page = {
                    id: currentPage,
                    comments: roots
                };
                $scope.pages.push(page);

                end = new Date().getTime();
                console.log('Execution time: ' + (end - start));

                console.log('event:fetched-page -> ...');
                $rootScope.$broadcast('event:fetched-page', page);

                //console.log('scroll to ', comments[0].id);
                //$scope.scrollTo(comments[0].id);
                $scope.scrollTo('page-' + currentPage);
                //$location.hash(currentPage);
            });
        };

        __fetchComments();

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

        var __sort = function (comments) {
            return _.sortBy(comments, function (comment) {
                return -1 * comment.$score
            })
        };

        var __postFetchComments = function (comments, currentPage) {

            return _.forEach(comments, function (comment, index) {
                comment.replies = {
                    $all: [],
                    verbose: [],
                    furthermore: []
                };

                comment.$index = currentPage * 200 + index;
                comment.$commentCount = 1;

                if (comment.hidden) {
                    comment.text = 'Content hidden';
                    comment.dislikes = 0;
                    comment.likes = 0;
                }

                if (comment.status == 'DELETED') {
                    comment.displayName = 'Deleted';
                    comment.text = 'Content deleted';
                    comment.dislikes = 0;
                    comment.likes = 0;
                }

                if(_.isUndefined(comment.likes)) {
                    comment.likes = 0;
                }
                if(_.isUndefined(comment.dislikes)) {
                    comment.dislikes = 0;
                }

                comment.$pending = comment.status == 'PENDING';

                // todo minimize negative-only comments, hell-banned subthreads

                comment.$minimized = comment.dislikes > 3 && comment.dislikes > comment.likes;
                comment.$score = comment.influence / comment.createdDate;

                // author chose to hide his name
                if (_.isEmpty(comment.displayName) || _.isUndefined(comment.displayName)) {
                    comment.displayName = 'Anonymous';
                }

                var total = comment.likes + comment.dislikes;
                comment.$likes = comment.likes / total * 100;
                comment.$dislikes = comment.dislikes / total * 100;

            });
        };

        var __shape = function (comments) {
            console.log('shape');
            __shapeRc(comments, 1);
        };

        var __shapeRc = function (comments, level) {

            _.forEach(comments, function (comment) {

                comment.$repliesCount = 0;
                var replies = comment.replies.$all;
                var verbose = comment.replies.verbose;
                var furthermore = comment.replies.furthermore;

                __shapeRc(replies, level +1);

                _.forEach(replies, function(reply, index) {

                    // get reply count
                    comment.$repliesCount += 1; // reply itself
                    comment.$repliesCount += reply.$repliesCount; // its replies

                    var isHidden = index >= 1 && reply.$repliesCount == 0 && reply.$little;

                    // todo && older than n views && not owner of comment
                    if( isHidden ) {
                        console.log('dropping', reply.id);
                        furthermore.push(reply.id);

                    } else {
                        verbose.push(reply);
                    }
                });

                // todo can still be controversial
                comment.$little = (comment.likes > 1 || comment.dislikes > 1) && (comment.likes - comment.dislikes) < -1;

                comment.replies.verbose = __sort(comment.replies.verbose);

                //delete comment.replies.$all;

            });
        };

        var __mergeWithTree = function (tree, comments) {

            var roots = [];

            _.forEach(comments, function (comment) {

                tree[comment.id] = comment;

                if (comment.level == 0 || _.isUndefined(comment.parentId)) {
                    roots.push(comment);

                } else {

                    var parent = tree[comment.parentId];
                    if (_.isUndefined(parent)) {
                        console.log('cannot find parent of', comment.parentId);
                    } else {
                        var replies = parent.replies;

                        replies.$all.push(comment);
                    }
                }
            });

            return roots;
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

        $scope.threadEventSocket = atmosphere;
        $scope.threadEventSubSocket;
        $scope.threadEventTransport = 'websocket';

        $scope.threadEventRequest = {
            url: 'websocket/live/channel',
            contentType: "application/json",
            transport: $scope.threadEventTransport,
            trackMessageLength: true,
            reconnectInterval: 5000,
            enableXDR: true,
            timeout: 60000
        };

        $scope.threadEventRequest.onOpen = function (response) {
            $scope.threadEventTransport = response.transport;
            $scope.threadEventRequest.uuid = response.request.uuid;
        };

        $scope.threadEventRequest.onMessage = function (response) {
            var message = atmosphere.util.parseJSON(response.responseBody);

            console.log('event', message);

            if (message.threadId == threadId) {

            }
        };

        $scope.threadEventSubSocket = $scope.threadEventSocket.subscribe($scope.threadEventRequest);

    }]);
