/**
 * Created by markus on 16.12.14.
 */
angular.module('kalipoApp')
    .directive('commentMenu', function ($compile, $templateCache, $http) {
    return {
        restrict: 'E', //E = element, A = attribute, C = class, M = comment
//        scope: {},
//        template: '<div ng-click="replaceMe()">I am the menu</div>',
        templateUrl: 'scripts/comment/partial_menu.html',
        replace: true,
//        transclude: true,
        controller: function ($scope, $element) {

            // todo replace by true/false
            $scope.toggleReplyForm = function (comment) {
                comment.$reply = true;
                comment.$report = false;
            };

            $scope.toggleReportForm = function (comment) {
                comment.$reply = false;
                comment.$report = true;
            };
        }
    }
});
