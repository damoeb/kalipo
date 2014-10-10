'use strict';

kalipoApp.controller('ReputationController', function ($scope, resolvedReputation, Reputation) {

    $scope.reputations = resolvedReputation;

    $scope.create = function () {
        Reputation.save($scope.reputation,
            function () {
                $scope.reputations = Reputation.query();
                $('#saveReputationModal').modal('hide');
            });
    };

    $scope.update = function (id) {
        $scope.reputation = Reputation.get({id: id});
        $('#saveReputationModal').modal('show');
    };
});
