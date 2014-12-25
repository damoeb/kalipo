/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('commentReply', function ($compile, $templateCache, $http) {
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
                        console.log('reply', commentId);

                        //var html = $http.get('scripts/comment/partial_reply.html', {cache: true});

                        //var html = '<div>I should not be read</div>';
                        //var html = $templateCache.get('scripts/comment/partial_reply.html');
                        $http.get('scripts/comment/partial_reply.html', {cache:true}).success(function(html) {
                            var e = $compile(html)($scope);
                            $element.replaceWith(e);
                        });

                    } else {
                        $element.empty();
                    }
                });
            }
        }
    });
