'use strict';

kalipoApp.controller('NoticeController', function ($rootScope, $scope, Session, Notice) {

    // todo $rootScope.login is undefined on first load
    $scope.fetch = function () {

        var __doFetchNotices = function () {
            Notice.query({userId: $rootScope.login, page: 0}, function (notices) {
                $scope.notices = notices;
            });
        };

        if (typeof($rootScope.login) == 'undefined') {
            console.log('wait');
            $scope.$on('event:auth-authorized', __doFetchNotices);
        } else {
            console.log($rootScope.login);
            __doFetchNotices();
        }
    };

    $scope.hasUnseen = function () {
        Notice.hasUnseen({userId: Session.login}, function (response) {
            $scope.hasUnseenNotices = response.hasUnseen;
        });
    };

    $scope.seenUntilNow = function () {
        Notice.seenUntilNow({userId: Session.login});
    };

});
