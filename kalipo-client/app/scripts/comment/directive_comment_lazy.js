/**
 * Created by damoeb.
 */
angular.module('kalipoApp')
    .directive('commentLazy', function (Comment) {
        return {
            restrict: 'E',
            templateUrl: 'views/template_comment_slim.html',
            link: function ($scope, $element, $attributes) {

                var commentId = $scope.$eval($attributes.ngCommentId);
                $scope.comment = Comment.get({id: commentId});
            }
        }
    });
