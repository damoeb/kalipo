'use strict';

kalipoApp.controller('ProfileLikesController', ['$scope', '$rootScope', 'Vote', 'Comment',
    function ($scope, $rootScope, Vote, Comment) {

        $scope.likes = [];

        $scope.$page = 0;

        var doFetchVotes = function () {
            console.log('Fetch votes of ' + $rootScope.login);

            Vote.byAuthor({'id': $rootScope.login, 'page': $scope.$page}, function (votes) {

                // todo use comment-lazy directive

                console.log('Got ' + votes.length + ' votes on page ' + $scope.$page);
                _.forEach(votes, function (vote) {
                    Comment.get({id: vote.commentId}, function (comment) {
                        console.log('Resolved comment ' + comment.id);

                        // author chose to hide his name
                        if (_.isEmpty(comment.displayName) || _.isUndefined(comment.displayName)) {
                            comment.displayName = 'Anonymous';
                        }

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
