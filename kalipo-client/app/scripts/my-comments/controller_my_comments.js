'use strict';

kalipoApp.controller('MyCommentsController', ['$scope', '$rootScope', 'Vote', 'Comment',
    function ($scope, $rootScope, Comment) {

        $scope.comments = [];

        $scope.$page = 0;

        var doFetchComments = function () {
            console.log('Fetch my comments of ' + $rootScope.login);

            Comment.byAuthor({'id': $rootScope.login, 'page': $scope.$page}, function (comments) {

                console.log('Got ' + comments.length + ' comments on page ' + $scope.$page);

                $scope.comments = comments;
            })
        };

        $scope.fetchVotes = function () {

            if (typeof($rootScope.login) == 'undefined') {
                console.log('wait');
                $scope.$on('event:auth-authorized', doFetchComments)
            } else {
                console.log($rootScope.login);
                doFetchComments();
            }
        };

    }]);
