'use strict';

kalipoApp.controller('NoticeController', function ($scope, resolvedNotice, Notice) {

        $scope.notices = resolvedNotice;

        $scope.create = function () {
            Notice.save($scope.notice,
                function () {
                    $scope.notices = Notice.query();
                    $('#saveNoticeModal').modal('hide');
                    $scope.clear();
                });
        };

        $scope.update = function (id) {
            $scope.notice = Notice.get({id: id});
            $('#saveNoticeModal').modal('show');
        };

        $scope.delete = function (id) {
            Notice.delete({id: id},
                function () {
                    $scope.notices = Notice.query();
                });
        };

        $scope.clear = function () {
            $scope.notice = {id: null};
        };
    });
