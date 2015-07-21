/**
 * Created by damoeb on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('threadOutline', function ($compile, $routeParams, $rootScope, Thread, Outline, OutlineConfig) {
        return {
            restrict: 'E',
            scope: {
                pages: '='
            },
            template: '<svg id="klp-outline"></svg>',
            replace: true,
            link: function ($scope, $element, $attributes) {

                var $this = this;
                $this.comments = [];

                var onScrollEnd = function (callback) {

                    var $this = $(this);

                    // attach scroll end listener
                    var scrollTimeout;

                    $this.scroll(function () {
                        if (scrollTimeout) {
                            clearTimeout(scrollTimeout);
                        }
                        scrollTimeout = setTimeout(function () {
                            callback();
                        }, 50);
                    });

                    // remove scroll listener
                    $scope.$on('$destroy', function () {
                        $this.off('scroll');
                    });
                };

                var refreshViewport = function () {
                    Outline.refreshViewport($this.comments, $element.parent())
                };

                $rootScope.$on('refresh-outline-viewport', refreshViewport);
                onScrollEnd(refreshViewport);

                $rootScope.$on('event:render-outline', function (event, tree) {

                    $this.comments = Outline.flattenTree(tree);

                    var dimensions = {
                        width: $element.parent().width(),
                        height: $this.comments.length * (OutlineConfig.bar_height + OutlineConfig.bar_marginBottom)
                    };

                    Outline.draw($this.comments, dimensions);
                });
            }
        }
    });
