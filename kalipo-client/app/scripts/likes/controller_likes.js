'use strict';

kalipoApp.controller('LikesController', ['$scope', '$rootScope', 'Vote', 'Comment',
    function ($scope, $rootScope, Vote, Comment) {

        $scope.likes = [];

        $scope.$page = 0;

        var doFetchLikes = function () {
            console.log('Fetch likes of ' + $rootScope.login);

            Vote.likes({'id': $rootScope.login, 'page': $scope.$page}, function (votes) {

                console.log('Got ' + votes.length + ' votes');
                _.forEach(votes, function (vote) {
                    Comment.get({id: vote.commentId}, function (comment) {

                        //console.log('Comment '+comment,id);
                        comment.createdVoteDate = vote.createdDate;
                        comment.voteAuthorId = vote.authorId;
                        $scope.likes.push(comment);
                    })
                });
            })
        };

        $scope.fetchLikes = function () {

            if (typeof($rootScope.login) == 'undefined') {
                console.log('wait');
                $scope.$on('event:auth-authorized', doFetchLikes)
            } else {
                console.log($rootScope.login);
                doFetchLikes();
            }
        };

    }]);
