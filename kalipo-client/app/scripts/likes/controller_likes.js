'use strict';

kalipoApp.controller('LikesController', ['$scope', '$rootScope', 'Vote', 'Comment',
    function ($scope, $rootScope, Vote, Comment) {

        $scope.likes = [];

        $scope.$page = 0;

        var doFetchVotes = function () {
            console.log('Fetch votes of ' + $rootScope.login);

            Vote.byAuthor({'id': $rootScope.login, 'page': $scope.$page}, function (votes) {

                console.log('Got ' + votes.length + ' votes on page ' + $scope.$page);
                _.forEach(votes, function (vote) {
                    Comment.get({id: vote.commentId}, function (comment) {
                        console.log('Resolved comment ' + comment.id);
                        comment.$vote = vote;
                        $scope.likes.push(comment);
                    })
                });
            })
        };

        $scope.fetchVotes = function () {

            if (typeof($rootScope.login) == 'undefined') {
                console.log('wait');
                $scope.$on('event:auth-authorized', doFetchVotes)
            } else {
                console.log($rootScope.login);
                doFetchVotes();
            }
        };

    }]);
