'use strict';

kalipoApp.config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
    $routeProvider
        .when('/notices', {
            templateUrl: 'views/notices.html',
            controller: 'NoticeController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
});
