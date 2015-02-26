/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('collection', function ($compile, $templateCache, $http, $rootScope, Vote, Comment) {
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

                // modal to create comments
                $http.get('views/partial_reply.html', {cache: true}).success(function (tmpl_reply) {
                    $element.append($compile(tmpl_reply)($scope));
                });

                $http.get('views/partial_comment.html', {cache: true}).success(function (tmpl_comment) {
                    $http.get('views/partial_menu.html', {cache: true}).success(function (tmpl_menu) {

                        var comp_comment = _.template(tmpl_comment);
                        var comp_menu = _.template(tmpl_menu);
                        var comp_toggle_concealed = _.template('<div class="toggle-optionals" style="margin-left: <%- comment.level * 15 %>px; <% if(comment.level>1) { %>border-left: 1px dashed #ececec;<% } %>"><a href="javascript:void(0)" ng-click="toggleOptionals(\'<%- comment.id %>\')"><strong><% if(comment.$hasObligatoryReplies) {%> <%- comment.$concealedRepliesCount %><% } else { %><%- comment.$repliesCount %><% } %></strong> <% if(comment.$concealedRepliesCount==1) { %>reply<% } else { %>replies<% } %></a> <span class="glyphicon glyphicon-chevron-down"></span></div>');

                        var $thread = $('<div></div>');

                        var __render = function (comment, $sink, concealed){

                            var $comment = $(comp_comment({
                                comment: comment,
                                page: $scope.page,
                                fnRenderMenu: comp_menu
                            })).appendTo($sink);

                            var $replies = $('<div></div>', {class: 'replies'}).appendTo($comment);

                            // obligatory replies
                            _.forEach(comment.replies, function (reply) {
                                if (reply.$obligatory || !concealed) {
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
                                    if(!reply.$obligatory) {
                                        __render(reply, $hidden_replies, true);
                                    }
                                });
                            }
                        };

                        _.forEach($scope.collection, function (comment) {
                            __render(comment, $thread, consealed);
                        });

                        $element.append($compile($thread.contents())($scope));

                    });
                });

                $scope.toggleOptionals = function (commentId) {

                    $('#comment-' + commentId + ' > .replies.optionals').toggleClass('hidden');
                    $rootScope.$broadcast('event:discussion-changed');

                };

                $scope.showReplyModal = function (commentId, displayName, threadId, quote) {
                    $('#createCommentModal').modal();
                    $scope.displayName = displayName;
                    $scope.draft.threadId = threadId;
                    $scope.draft.body = '>' + quote.replace(/\n/g, '>\n');
                    $scope.draft.parentId = commentId;
                };

                $scope.toggleReportForm = function (commentId) {
                    console.log('report', commentId);
                    // todo show popover
                };

                $scope.submitComment = function () {

                    console.log('submit comment', $scope.draft);
                    // todo support anon flag in view
                    $scope.draft.anonymous = false;

                    Comment.save($scope.draft,
                        function () {
                            // todo notification
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

                    Vote.save(vote, function (updated) {
//                noty({text: 'Liked', type: 'success'});
                    });
                };

                $scope.dislike = function (commentId) {
                    console.log('dislike', commentId);
                    commentId.dislikes++;

                    var vote = {like: false, commentId: commentId};

                    Vote.save(vote, function (updated) {
//                noty({text: 'Disliked', type: 'success'});
                    });
                };

            }
        }
    });
