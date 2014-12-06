'use strict';

kalipoApp.controller('VoteController', function ($scope, resolvedVote, Vote) {

    $scope.votes = resolvedVote;

    $scope.create = function () {
        Vote.save($scope.vote,
            function () {
                $scope.votes = Vote.query();
                $('#saveVoteModal').modal('hide');
                $scope.clear();
            });
    };

    $scope.update = function (id) {
        $scope.vote = Vote.get({id: id});
        $('#saveVoteModal').modal('show');
    };

    $scope.delete = function (id) {
        Vote.delete({id: id},
            function () {
                $scope.votes = Vote.query();
            });
    };

    $scope.clear = function () {
        $scope.vote = {id: null, commentId: null, authorId: null, like: null};
    };
});
