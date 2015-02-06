/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('replies', function ($compile) {
    return {
        restrict: 'E',
        replace: false,
        scope: {
            comment: '='
        },
        templateUrl: 'views/partial_comment.html',
        link: function ($scope, $element, $attrs) {
            if (angular.isArray($scope.comment.replies.verbose)) {
                $element.append("<collection collection='comment.replies.verbose'></collection>");
                $compile($element.contents())($scope)
            }
            var furthermore = $scope.comment.replies.furthermore;
            if (angular.isArray(furthermore) && furthermore.length > 0) {
                $compile('<div><a href="javascript:void(0)">{{comment.replies.furthermore.length}} more comments</a></div>')($scope)
            }
        }
    }
});
