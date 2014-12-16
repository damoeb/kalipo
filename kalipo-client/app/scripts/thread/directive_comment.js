/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('comment', function () {
    return {
        restrict: 'E',
//        scope: {},
        templateUrl: 'scripts/thread/partial_comment.html'
    }
});