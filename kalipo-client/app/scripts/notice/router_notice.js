'use strict';

kalipoApp.config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
    $routeProvider
        .when('/notifications', {
            templateUrl: 'views/my-notices.html',
            controller: 'NoticeController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
});
