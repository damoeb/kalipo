/**
 * Created by damoeb on 28.04.15.
 */
angular.module('kalipoApp')
    .directive('whenScrolled', function($rootScope) {
    return {
        restrict: 'A',
        //scope: '=',
        link: function($scope, $element, $attributes) {

            $scope.busy = false;

            var $document = $(document);

            $rootScope.$on('init-when-scrolled', function () {

                setTimeout(function () {

                    $document.bind('scroll', function () {
                        var scrollTop = $document.scrollTop();
                        var top = $element.offset().top;
                        var height = $element.height();
                        var loadNextPage = scrollTop + top > height;

                        if ($scope.busy) return;

                        //console.log('whenScrolled', scrollTop + top, height, loadNextPage);

                        if (loadNextPage) {
                            $scope.busy = true;
                            $scope.$apply($attributes.whenScrolled);

                            setTimeout(function () {
                                $scope.busy = false;
                                $scope.$apply();
                            }, 4000);
                        }
                    });

                }, 2000);
            });
        }
    }
});
