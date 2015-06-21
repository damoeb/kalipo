/**
 * Created by damoeb on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('discussion', function ($compile, $templateCache, $http, $rootScope, Vote, Comment, Report, Notifications, COMMENT_SETTINGS, Discussion, $q) {
        return {
            restrict: 'E',
            replace: true,
            template: '',
            link: function ($scope, $element, $attrs) {

                $scope.draft = {};
                $scope.$isMod = true;

                var promise = Discussion.init();

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

                $q.when(promise).then(function () {
                    var $thread = $('<div></div>');

                    _.forEach($scope.page.comments, function (comment) {
                        Discussion.renderComment(comment, $thread, false);
                    });

                    $element.append($compile($thread.contents())($scope));

                    // 'show more' treatment
                    $element.find('.comment').each(function () {
                        __showMore($(this));
                    });
                });

                $scope.toggleOptionals = function (commentId) {

                    $('#comment-' + commentId + ' > .replies.optionals').toggleClass('hidden');
                    $rootScope.$broadcast('refresh-outline-viewport');
                };

                // --

                $scope.ignoreAuthorOf = function (commentId, displayName) {
                    // todo implement
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
                    console.log('refresh-outline-viewport -> ...');
                    $('#comment-' + commentId).toggleClass('hiddenreplies');
                    $rootScope.$broadcast('refresh-outline-viewport');
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
