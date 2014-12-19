/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('commentReply', function ($compile, $templateCache) {
        return {
            restrict: 'E',
            replace: false,
            //scope: {},
//            templateUrl: 'scripts/comment/partial_reply.html',
            link: function ($scope, $element, $attributes) {

                var condition = $attributes.ngRenderIf;

                $scope.$watch(condition, function (render) {

                    if (render) {
                        var commentId = $scope.$eval($attributes.ngCommentId);
                        console.log('render', commentId);

                        //var html = $templateCache.get('scripts/comment/partial_reply.html');
                        //    console.log(html);
                        var html = '<div>I should not be read</div>';
                        var e = $compile(html)($scope);
                        $element.replaceWith(e);
                    } else {

                    }
                });
            }
        }
    });
