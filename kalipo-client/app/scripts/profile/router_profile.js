'use strict';

kalipoApp.config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
    $routeProvider
        .when('/profile', {
            templateUrl: 'views/profile.html',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/profile/:tabId', {
            templateUrl: 'views/profile.html',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        });
});
