/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('commentCreate', function () {
        return {
            restrict: 'E',
//        scope: {},
            templateUrl: 'scripts/comment/partial_create.html'
        }
    });
