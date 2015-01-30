/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('collection', function ($compile, $templateCache, $http) {
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

                        var $thread = $('<div></div>');

                        var _render = function (comment, $sink, index) {

                            var $comment = $(compiled_comment({
                                comment: comment,
                                $index: index,
                                page: $scope.page,
                                fnRenderMenu: compiled_menu
                            })).appendTo($sink);

                            if (_.isArray(comment.replies.verbose)) {
                                var $replies = $('<div></div>').appendTo($sink);

                                _.forEach(comment.replies.verbose, function (reply, index) {
                                    _render(reply, $replies, index);
                                });
                            }
                        };

                        _.forEach($scope.collection, function (comment, index) {
                            _render(comment, $thread, index);
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

            }
        }
    });
