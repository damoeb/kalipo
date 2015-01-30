/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('commentReply', function ($compile, $templateCache, $http) {
        return {
            restrict: 'E',
            replace: false,
            scope: {
                commentId: '='
            },
            link: function ($scope, $element, $attributes) {

                var condition = $attributes.ngRenderIf;

                $scope.$watch(condition, function (render) {

                    if (render) {
                        console.log('reply', $scope.commentId);
                        $http.get('scripts/comment/partial_reply.html', {cache:true}).success(function(html) {
                            var e = $compile(html)($scope);
                            $element.append(e);
                        });

                    } else {
                        $element.empty();
                    }
                });
            }
        }
    });
