/**
 * Created by damoeb on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('collection', function ($compile, $templateCache, $http, $rootScope, Vote, Comment, Report, Notifications, REPORT_IDS, COMMENT_SETTINGS) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                collection: '=',
                page: '='
            },
            template: '',
            link: function ($scope, $element, $attrs) {

                $scope.draft = {};
                $scope.report = {};

                $scope.reportOptions = REPORT_IDS;

                // modals
                // .. to CREATE a comment
                $http.get('views/modal_reply.html', {cache: true}).success(function (tmpl_reply) {
                    $element.append($compile(tmpl_reply)($scope));
                });
                // .. to REPORT a comment
                $http.get('views/modal_report.html', {cache: true}).success(function (tmpl_report) {
                    $element.append($compile(tmpl_report)($scope));
                });

                /**
                 * will wrap excessive long comments with a container and a "show-more" toggle link
                 * @param $commentWrapper
                 * @private
                 */
                var __showMore = function ($commentWrapper) {
                    var $comment = $commentWrapper.find('.body');
                    var lineCount = $comment.height() / COMMENT_SETTINGS.lineHeight;
                    console.log('lineCount', lineCount, 'append?', lineCount > COMMENT_SETTINGS.criticalLineCount);

                    if (lineCount > COMMENT_SETTINGS.criticalLineCount) {

                        var $wrapper = $('<div/>', {class: 'show-more-content', html: $comment.html()});

                        $comment.empty().append($wrapper);

                        var n = lineCount - COMMENT_SETTINGS.criticalLineCount;

                        var $str_show_more = $('<span/>', {class: 'more', text: 'Show ' + n + ' more lines'});
                        var $str_show_less = $('<span/>', {class: 'less', text: 'Hide ' + n + ' lines'});
                        var $fn = $('<a/>', {href: 'javascript:void(0)'}).append($str_show_more).append($str_show_less);

                        $fn.click(function () {
                            $comment.toggleClass('tiny')
                        });

                        $comment.addClass('tiny');
                        $comment.append($fn);
                    }
                };

                $http.get('views/partial_comment.html', {cache: true}).success(function (tmpl_comment) {
                    $http.get('views/partial_menu.html', {cache: true}).success(function (tmpl_menu) {

                        var comp_comment = _.template(tmpl_comment);
                        var comp_menu = _.template(tmpl_menu);
                        var comp_toggle_concealed = _.template('<div class="toggle-optionals" style="margin-left: <%- comment.level * 15 %>px; <% if(comment.level>1) { %>border-left: 1px dashed #ececec;<% } %>"><a href="javascript:void(0)" ng-click="toggleOptionals(\'<%- comment.id %>\')"><strong><% if(comment.$hasObligatoryReplies) {%> <%- comment.$concealedRepliesCount %><% } else { %><%- comment.$repliesCount %><% } %></strong> <% if(comment.$hasObligatoryReplies && comment.$concealedRepliesCount==1 || comment.$repliesCount==1) { %>reply<% } else { %>replies<% } %></a> <span class="glyphicon glyphicon-chevron-down"></span></div>');

                        var $thread = $('<div></div>');

                        var __render = function (comment, $sink, concealed) {

                            var $comment = $(comp_comment({
                                comment: comment,
                                page: $scope.page,
                                fnRenderMenu: comp_menu
                            })).appendTo($sink);

                            var $replies = $('<div></div>', {class: 'replies'}).appendTo($comment);

                            // obligatory replies
                            _.forEach(comment.replies, function (reply) {
                                if (reply.$obligatory || concealed) {
                                    __render(reply, $replies, concealed);
                                }
                            });

                            if (comment.$concealedRepliesCount > 0 && !concealed) {

                                $comment.append(comp_toggle_concealed({
                                    comment: comment
                                }));

                                var $hidden_replies = $('<div></div>', {class: 'replies optionals hidden'}).appendTo($comment);

                                // obligatory replies
                                _.forEach(comment.replies, function (reply) {
                                    if (!reply.$obligatory) {
                                        __render(reply, $hidden_replies, true);
                                    }
                                });
                            }
                        };

                        _.forEach($scope.collection, function (comment) {
                            __render(comment, $thread, false);
                        });

                        $element.append($compile($thread.contents())($scope));

                        // 'show more' treatment
                        $element.find('.comment').each(function () {
                            __showMore($(this));
                        });

                    });
                });

                $scope.toggleOptionals = function (commentId) {

                    $('#comment-' + commentId + ' > .replies.optionals').toggleClass('hidden');
                    $rootScope.$broadcast('event:discussion-changed');

                };

                // --

                $scope.showReplyModal = function (commentId, displayName, threadId, quote) {

                    console.log('reply modal', commentId);

                    $('#createCommentModal').modal();
                    $scope.displayName = displayName;
                    $scope.draft.threadId = threadId;
                    $scope.draft.body = '>' + quote.replace(/\n/g, '>\n');
                    $scope.draft.parentId = commentId;
                };

                $scope.submitComment = function () {

                    console.log('submit comment', $scope.draft);
                    // todo support anon flag in view
                    $scope.draft.anonymous = false;

                    Comment.save($scope.draft,
                        function () {
                            Notifications.info('Comment saved');
                        });
                };


                // --

                $scope.showReportModal = function (commentId, displayName) {
                    console.log('report modal', commentId);

                    $('#reportCommentModal').modal();

                    $scope.displayName = displayName;
                    $scope.report.commentId = commentId;
                };

                $scope.submitReport = function () {

                    console.log('submit report');

                    Report.save($scope.report,
                        function () {
                            Notifications.info('Report saved...');
                            $scope.report.reason = null;
                        });
                };

                $scope.verbose = function (commentId) {
                    $('#comment-' + commentId).removeClass('oneline');
                };

                $scope.toggleReplies = function (commentId) {
                    console.log('event:discussion-changed -> ...');
                    $('#comment-' + commentId).toggleClass('hiddenreplies');
                    $rootScope.$broadcast('event:discussion-changed');
                };

                $scope.like = function (commentId) {
                    console.log('like', commentId);
                    commentId.likes++;

                    var vote = {like: true, commentId: commentId};

                    Vote.save(vote, function (id) {
                        console.log('Liked', id);
                        Notifications.info('Liked');
                    });
                };

                $scope.dislike = function (commentId) {
                    console.log('dislike', commentId);
                    commentId.dislikes++;

                    var vote = {like: false, commentId: commentId};

                    Vote.save(vote, function (id) {
                        console.log('Disliked', id);
                        Notifications.info('Disliked');
                    });
                };

            }
        }
    });
