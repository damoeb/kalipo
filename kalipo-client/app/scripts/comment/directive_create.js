/**
 * Created by damoeb on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('commentCreate', function () {
        return {
            restrict: 'E',
//        scope: {},
            templateUrl: 'views/partial_create.html'
        }
    });
