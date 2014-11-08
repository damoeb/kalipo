'use strict';

kalipoApp.controller('NoticeController', function ($scope, Session, Notice) {

    // todo $rootScope.login is undefined on first load
    Notice.query({id: Session.login, opt2: 0}, function (notices) {
        $scope.notices = notices;
    });

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
