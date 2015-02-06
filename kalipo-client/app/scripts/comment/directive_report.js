/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('commentReport', function ($compile, $templateCache, $http) {
        return {
            restrict: 'E',
            replace: false,
            //scope: {},
            link: function ($scope, $element, $attributes) {

                var condition = $attributes.ngRenderIf;

                $scope.$watch(condition, function (render) {

                    if (render) {
                        console.log('report', $scope.comment.id);
                        $http.get('views/partial_report.html', {cache: true}).success(function (html) {
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
