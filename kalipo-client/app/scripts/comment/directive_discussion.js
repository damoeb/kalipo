/**
 * Created by damoeb on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('discussion', function ($compile, $templateCache, $http, $rootScope, Vote, Comment, Report, Notifications, COMMENT_SETTINGS, Discussion, $q, Account) {
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

                    _.forEach($scope.fragment, function (comment) {
                        Discussion.renderComment(comment, $thread, false);
                    });

                    $element.append($compile($thread.contents())($scope));

                    // 'show more' treatment
                    $element.find('.comment').each(function () {
                        __showMore($(this));
                    });
                });
            }
        }
    });
