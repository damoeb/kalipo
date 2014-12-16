/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('likes', function () {
    return {
        restrict: 'E',
//        scope: {},
        templateUrl: 'scripts/thread/partial_likes.html'
    }
});