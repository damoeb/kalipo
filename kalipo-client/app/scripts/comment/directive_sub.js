/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('subcomment', function ($compile, $templateCache, $http) {
        return {
            restrict: 'E',
            //replace: false,
            scope: {
                collection: '=',
                index: '='
            },
            link: function ($scope, $element, $attributes) {
                $scope.comment = $scope.collection[$scope.index];
                console.log('subcomment', $scope.collection.length, $scope.index);
            }
        }
    });
