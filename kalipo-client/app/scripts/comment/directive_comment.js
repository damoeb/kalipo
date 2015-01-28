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
        templateUrl: 'scripts/comment/partial_comment.html',
        link: function ($scope, $element, $attrs) {
            console.log('parent', $scope.comment.id, 'comment', $scope.comment.replies.verbose);
            if (angular.isArray($scope.comment.replies.verbose)) {
                $element.append("<collection collection='comment.replies.verbose'></collection>");
                $compile($element.contents())($scope)
            }
        }
    }
});
