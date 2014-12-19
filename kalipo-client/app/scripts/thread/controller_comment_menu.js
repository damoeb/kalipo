'use strict';

kalipoApp.controller('CommentMenuController', ['$scope', '$routeParams', 'Comment', 'Vote',
    function ($scope, $routeParams, Comment, Vote) {

        $scope.like = function (comment) {
            comment.likes++;

            var vote = {like: true, commentId: comment.id};

            Vote.save(vote, function (updated) {
//                noty({text: 'Liked', type: 'success'});
            });
        };

        $scope.dislike = function (comment) {
            comment.dislikes++;

            var vote = {like: false, commentId: comment.id};

            Vote.save(vote, function (updated) {
//                noty({text: 'Disliked', type: 'success'});
            });
        };

    }]);
