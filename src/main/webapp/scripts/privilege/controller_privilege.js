'use strict';

kalipoApp.controller('PrivilegeController', function ($scope, resolvedPrivilege, Privilege) {

    $scope.privileges = resolvedPrivilege;

    $scope.create = function () {
        Privilege.save($scope.privilege,
            function () {
                $scope.privileges = Privilege.query();
                $('#savePrivilegeModal').modal('hide');
                $scope.clear();
            });
    };

    $scope.update = function (id) {
        $scope.privilege = Privilege.get({id: id});
        $('#savePrivilegeModal').modal('show');
    };

    $scope.delete = function (id) {
        Privilege.delete({id: id},
            function () {
                $scope.privileges = Privilege.query();
            });
    };

    $scope.clear = function () {
        $scope.privilege = {id: null, sampleTextAttribute: null, sampleDateAttribute: null};
    };
});
