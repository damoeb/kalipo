/**
 * Created by damoeb on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('discussion', function ($compile, $templateCache, $http, $rootScope, Vote, Comment, Report, Notifications, COMMENT_SETTINGS) {
        return {
            restrict: 'E',
            replace: true,
            //scope: {
            //    collection: '=',
            //    page: '='
            //    // todo parent scope (thread) should be inherited
            //},
            template: '',
            link: function ($scope, $element, $attrs) {

                $scope.draft = {};
                $scope.$isMod = true;

                /**
                 * will wrap excessive long comments with a container and a "show-more" toggle link
                 * @param $commentWrapper
                 * @private
                 */
                var __showMore = function ($commentWrapper) {
                    var $comment = $commentWrapper.find('.body');
                    var lineCount = $comment.height() / COMMENT_SETTINGS.lineHeight;
                    //console.log('lineCount', lineCount, 'append?', lineCount > COMMENT_SETTINGS.criticalLineCount);

                    if (lineCount > COMMENT_SETTINGS.criticalLineCount) {

                        var $wrapper = $('<div/>', {class: 'show-more-content', html: $comment.html()});

                        $comment.empty().append($wrapper);

                        var n = lineCount - COMMENT_SETTINGS.criticalLineCount;

                        var $str_show_more = $('<span/>', {class: 'more', text: 'Show ' + parseInt(n) + ' more lines'});
                        var $str_show_less = $('<span/>', {class: 'less', text: 'Hide ' + parseInt(n) + ' lines'});
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

                        _.forEach($scope.page.comments, function (comment) {
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
