'use strict';

kalipoApp.controller('DiscussionController', ['$scope', '$routeParams', '$location', '$anchorScroll', '$rootScope', 'Thread', 'Comment', 'Report', 'Vote',
    function ($scope, $routeParams, $location, $anchorScroll, $rootScope, Thread, Comment, Report, Vote) {

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

                var roots = __mergeWithTree(tree, comments);
                __classifyByInfluence(roots);

                $scope.pages.push({
                    id: currentPage,
                    comments: roots
                });

                //__startLiveUpdates();

                end = new Date().getTime();
                console.log('Execution time: ' + (end - start));

                //console.log('scroll to ', comments[0].id);
                //$scope.scrollTo(comments[0].id);
                $scope.scrollTo('page-' + currentPage);
                //$location.hash(currentPage);
            });
        };

        __fetchComments();

        //var isTyping = false;
        //var stoppedTypingTimer = 0;
        //$scope.onTyping = function () {
        //    if (!isTyping) {
        //        console.log('started typing');
        //        isTyping = true;
        //        $rootScope.liveRequest.sendMessage(isTyping, threadId);
        //    }
        //
        //    if (stoppedTypingTimer) {
        //        clearInterval(stoppedTypingTimer);
        //    }
        //    stoppedTypingTimer = setTimeout(function () {
        //        isTyping = false;
        //        console.log('stopped typing');
        //        $rootScope.liveRequest.sendMessage(isTyping, threadId);
        //    }, 10000);
        //};

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

        var __sortByInfluence = function (comments) {
            return _.sortBy(comments, function (comment) {
                return -comment.influence
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

                comment.$imploded = false;
                comment.$minimized = comment.dislikes > 3 && comment.dislikes > comment.likes;
                comment.$score = Math.max(comment.likes - comment.dislikes, 1) / comment.createdDate;

                // author chose to hide his name
                if (_.isEmpty(comment.displayName) || _.isUndefined(comment.displayName)) {
                    comment.displayName = 'Anonymous';
                }

                var total = comment.likes + comment.dislikes;
                comment.$likes = comment.likes / total * 100;
                comment.$dislikes = comment.dislikes / total * 100;

            });
        };

        var __classifyByInfluence = function (comments) {
            console.log('classify');
            __classifyByInfluenceRc(comments, 1);
        };

        var __classifyByInfluenceRc = function (comments, level) {
            _.forEach(comments, function (comment) {

                comment.$repliesCount = 0;
                var replies = comment.replies.$all;
                var verbose = comment.replies.verbose;
                var furthermore = comment.replies.furthermore;

                __classifyByInfluenceRc(replies, level +1);

                _.forEach(replies, function(reply, index) {

                    // get reply count
                    comment.$repliesCount += 1; // reply itself
                    comment.$repliesCount += reply.$repliesCount; // its replies

                    if (reply.influence <= 0 || index > 4) { // todo && older than n views && not owner of comment

                        if(replies.length < 3 || level > 1) {
                            reply.$little = true;
                            verbose.push(reply);
                        } else {
                            furthermore.push(reply.id);
                        }

                    } else {
                        verbose.push(reply);
                    }
                });

                // sort by score
                comment.replies.verbose = __sortByInfluence(comment.replies.verbose);

                delete comment.replies.$all;

            });
        };

        var __mergeWithTree = function (tree, comments) {

            var roots = [];

            _.forEach(comments, function (comment) {

                tree[comment.id] = comment;

                if (comment.level == 0 || _.isUndefined(comment.parentId)) {
                    roots.push(comment);

                } else {

                    var replies = tree[comment.parentId].replies;

                    replies.$all.push(comment);
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

        //$scope.typing = [];
        //
        //$scope.threadEventSocket = atmosphere;
        //$scope.threadEventSubSocket;
        //$scope.threadEventTransport = 'websocket';
        //
        //$scope.threadEventRequest = { url: 'websocket/live/channel',
        //    contentType: "application/json",
        //    transport: $scope.threadEventTransport,
        //    trackMessageLength: true,
        //    reconnectInterval: 5000,
        //    enableXDR: true,
        //    timeout: 60000 };
        //
        //$scope.threadEventRequest.onOpen = function (response) {
        //    $scope.threadEventTransport = response.transport;
        //    $scope.threadEventRequest.uuid = response.request.uuid;
        //};
        //
        //$scope.threadEventRequest.onMessage = function (response) {
        //    var message = response.responseBody;
        //    var event = atmosphere.util.parseJSON(message);
        //
        //    if (event.threadId == threadId) {
        //        if (event.typing) {
        //            $scope.typing.push(event.userLogin);
        //            $scope.typing = _.sortBy(_.compact($scope.typing), 'user');
        //        } else {
        //            $scope.typing = _.remove($scope.typing, function (userLogin) {
        //                return userLogin != event.userLogin;
        //            })
        //        }
        //    }
        //
        //    $scope.$apply();
        //};
        //
        //$scope.threadEventSubSocket = $scope.threadEventSocket.subscribe($scope.threadEventRequest);

    }]);
