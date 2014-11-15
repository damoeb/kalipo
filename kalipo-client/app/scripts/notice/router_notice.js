'use strict';

kalipoApp.config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
    $routeProvider
        .when('/notices/my', {
            templateUrl: 'views/notices.html',
            controller: 'NoticeController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
});
