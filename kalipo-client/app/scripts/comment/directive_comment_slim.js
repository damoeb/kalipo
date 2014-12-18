/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('commentSlim', function () {
        return {
            restrict: 'E',
            replace: true,
            //scope: {
            //    'comment': '=comment'
            //},
            templateUrl: 'scripts/comment/partial_comment_slim.html',
            link: function ($scope, $element, $attributes) {

                var model = $attributes.ngModel;
                if (_.isUndefined($scope.$eval(model))) {
                    $scope.$watch(model, function (comment) {
                        if (!_.isUndefined(comment)) {
                            $scope.comment = comment;

                            // deregister
                            $scope.$watch(model, function () {
                            });
                        }
                    });
                }
            }
        }
    });
