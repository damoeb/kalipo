/**
 * Created by damoeb on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('threadOutline', function ($compile, $routeParams, $rootScope, Thread, Outline) {
        return {
            restrict: 'E',
            scope: {
                pages: '='
            },
            template: '<svg id="klp-outline"></svg>',
            replace: true,
            link: function ($scope, $element, $attributes) {

                var threadId = $routeParams.threadId;
                console.log('threadId', threadId);

                var $this = this;

                Outline.assign($element);

                var onScrollEnd = function (callback) {
                    $(document).ready(function () {

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

                    });
                };

                $rootScope.$on('event:discussion-changed', function () {
                    console.log('-> event:discussion-changed');
                    Outline.scroll($this.comments);
                });

                onScrollEnd(function () {
                    Outline.scroll($this.comments)
                });

                var done_firstPage = false;
                var done_outline = false;

                $rootScope.$on('event:fetched-page', function () {

                    console.log('-> event:fetched-page');
                    done_firstPage = true;

                    if (done_outline) {
                        Outline.prepareAndDraw($scope.pages, $this.comments, function (newComments) {
                            $this.comments = newComments;
                        });
                    }
                });

                Outline.fetchOutline(threadId, function (comments) {
                    $this.comments = comments;

                    done_outline = true;

                    if (done_firstPage) {
                        Outline.prepareAndDraw($scope.pages, comments, function (newComments) {
                            $this.comments = newComments;
                        });
                    }

                });

            }
        }
    });
