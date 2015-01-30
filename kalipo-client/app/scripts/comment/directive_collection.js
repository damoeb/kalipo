/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('collection', function ($compile, $templateCache, $http, Vote) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                collection: '=',
                page: '='
            },
            template: '',
            link: function ($scope, $element, $attrs) {

                $http.get('scripts/comment/partial_comment.html', {cache: true}).success(function (tmpl_comment) {
                    $http.get('scripts/comment/partial_menu.html', {cache: true}).success(function (tmpl_menu) {

                        var compiled_comment = _.template(tmpl_comment);
                        var compiled_menu = _.template(tmpl_menu);
                        var compiled_more = _.template('<a href="javascript:void(0)">Load <strong><%= count %></strong> <% if(count==1) { %>comment<% } else { %>comments<% } %></a>');

                        var $thread = $('<div></div>');

                        var index = 0;

                        var __render = function (comment, $sink) {

                            var $comment = $(compiled_comment({
                                comment: comment,
                                page: $scope.page,
                                fnRenderMenu: compiled_menu
                            })).appendTo($sink);

                            if (_.isArray(comment.replies.verbose)) {
                                var $replies = $('<div></div>', {class: 'replies'}).appendTo($comment);

                                _.forEach(comment.replies.verbose, function (reply) {
                                    __render(reply, $replies);
                                });
                            }

                            if (comment.replies.furthermore.length > 0) {
                                $comment.append(compiled_more({count: comment.replies.furthermore.length}));
                            }

                        };

                        _.forEach($scope.collection, function (comment) {
                            __render(comment, $thread);
                        });

                        //var rendered = _.template(t_collection, {variable: 'data'})({collection: $scope.collection});

                        //$element.append($thread);
                        $element.append($compile($thread.contents())($scope));

                    });
                });


                $scope.toggleReplyForm = function (commentId) {
                    console.log('reply', commentId);
                    $scope.$reply = commentId;
                    //comment.$report = false;
                };

                $scope.toggleReportForm = function (commentId) {
                    console.log('report', commentId)
                    //comment.$reply = false;
                    $scope.$report = commentId;
                };

                $scope.unLittle = function (commentId) {
                    $('#comment-' + commentId).removeClass('little');
                };

                $scope.toggle = function (commentId) {
                    $('#comment-' + commentId).toggleClass('minimized');
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
