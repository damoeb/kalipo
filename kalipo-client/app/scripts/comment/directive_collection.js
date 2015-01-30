/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('collection', function ($compile) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                collection: '='
            },
            //template: '<div><replies ng-repeat="comment in collection" comment="comment"></replies></div>',
            template: '',
            link: function ($scope, $element, $attrs) {

                $scope.comment = {
                    replies: {
                        verbose: $scope.collection
                    }
                };

                var tmpl_reply = _.template('<subcomment collection="comment.replies.verbose" index="<%= index %>"> <div><%= comment.id %> vs {{comment.id}}</div> </subcomment>');

                var $thread = $('<ul></ul>');

                var _render = function(comment, $sink, index) {

                    var $comment = $(tmpl_reply({comment:comment, index:index})).appendTo($sink);

                    if(_.isArray(comment.replies.verbose)) {
                        var $replies = $('<ul></ul>').appendTo($comment);

                        _.forEach(comment.replies.verbose, function(reply, index) {
                            _render(reply, $replies, index);
                        });
                    }
                };

                _.forEach($scope.collection, function(comment, index) {
                    _render(comment, $thread, index);
                });

                //var rendered = _.template(t_collection, {variable: 'data'})({collection: $scope.collection});

                //$element.append($thread);
                $element.append($compile($thread.contents())($scope));

                //$compile('<div><a href="javascript:void(0)">{{comment.replies.furthermore.length}} more comments</a></div>')($scope);
            }
        }
    });
