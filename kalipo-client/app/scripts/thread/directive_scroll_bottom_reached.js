/**
 * Created by damoeb on 28.04.15.
 */
angular.module('kalipoApp')
    .directive('ngScrollBottomReached', function($rootScope) {
    return {
        restrict: 'A',
        //scope: '=',
        link: function($scope, $element, $attributes) {

            // todo define scrollpane as a parameter
            var $document = $(document);

            $document.ready(function () {

                console.log('initialize');

                $document.bind('scroll', function () {
                    var scrollTop = $document.scrollTop();
                    var top = $element.offset().top;
                    var height = $element.height();
                    var isBottomReached = scrollTop + top > height;

                    if ($scope.$busy) return;

                    if (isBottomReached) {
                        $scope.$apply($attributes.ngScrollBottomReached);
                    }
                });
            });
        }
    }
});
