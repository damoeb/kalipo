/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('collection', function ($compile) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                collection: '='
            },
            template: '<div><replies ng-repeat="comment in collection" comment="comment"></replies></div>'
        }
    });
