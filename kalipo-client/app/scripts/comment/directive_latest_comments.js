/**
 * Created by damoeb on 11.06.15.
 */
angular.module('kalipoApp')
    .directive('latestComments', function ($compile, $routeParams, $templateCache, $http, $rootScope, Vote, Thread, Comment, Report, Notifications, REPORT_IDS, COMMENT_SETTINGS) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                //    collection: '=',
                //    page: '='
                //    // todo parent scope (thread) should be inherited
            },
            template: '',
            link: function ($scope, $element, $attrs) {

                var threadId = $routeParams.threadId;

                $http.get('views/partial_ticker_comment.html', {cache: true}).success(function (tmpl_ticket_comment) {

                    var comp_ticket_comment = _.template(tmpl_ticket_comment);

                    var latest_comments = [];

                    var render = function () {
                        var $wrapper = $('<div/>');
                        _.forEachRight(latest_comments, function (comment) {
                            $wrapper.append(comp_ticket_comment({comment: comment}));
                        });
                        $element.empty().append($compile($wrapper.contents())($scope));
                    };

                    Thread.latest({id: threadId}, function (page) {
                        latest_comments = page.content;
                        render();
                    });

                    // listener for new comments
                    $rootScope.$on('event:comment', function (event, comment) {
                        console.log('new', comment);
                        // drop oldest (first), append new comment
                        latest_comments = _.drop(latest_comments);
                        latest_comments.push(comment);
                        render();
                    });

                });
            }
        }
    });
