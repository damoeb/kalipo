'use strict';

kalipoApp.controller('NoticeController', function ($scope, Session, Notice) {

    // todo $rootScope.login is undefined on first load
    $scope.fetch = function () {
        Notice.query({userId: Session.login, page: 0}, function (notices) {
            $scope.notices = notices;
        });
    };

    $scope.hasUnseen = function () {
        Notice.hasUnseen({userId: Session.login}, function (response) {
            $scope.hasUnseenNotices = response.hasUnseen;
        });
    };

    $scope.seenUntilNow = function () {
        Notice.seenUntilNow({userId: Session.login});
    };

    $scope.clear = function () {
        $scope.notice = {id: null};
    };
});
