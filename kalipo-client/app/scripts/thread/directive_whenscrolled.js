/**
 * Created by damoeb on 28.04.15.
 */
angular.module('kalipoApp')
    .directive('whenScrolled', function($rootScope) {
    return {
        restrict: 'A',
        //scope: '=',
        link: function($scope, $element, $attributes) {

            var $document = $(document);

            $rootScope.$on('init-when-scrolled', function () {

                setTimeout(function () {

                    $document.bind('scroll', function () {
                        var scrollTop = $document.scrollTop();
                        var top = $element.offset().top;
                        var height = $element.height();
                        var loadNextPage = scrollTop + top > height;

                        if ($scope.$busy) return;

                        if (loadNextPage) {
                            $scope.$apply($attributes.whenScrolled);
                        }
                    });

                }, 2000);
            });
        }
    }
});
