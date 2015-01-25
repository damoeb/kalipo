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

                var ngModel = $attributes.ngModel;
                var model = $scope.$eval(ngModel);
                if (_.isUndefined(model)) {
                    $scope.$watch(ngModel, function (comment) {
                        if (!_.isUndefined(comment)) {
                            $scope.comment = comment;

                            // deregister
                            $scope.$watch(ngModel, function () {
                            });
                        }
                    });
                } else {
                    $scope.comment = model;
                }
            }
        }
    });
