'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/moderate', {
                templateUrl: 'views/moderate-pending.html',
                controller: 'PendingController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
            .when('/moderate/pending', {
                templateUrl: 'views/moderate-pending.html',
                controller: 'PendingController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
            .when('/moderate/reports', {
                templateUrl: 'views/moderate-reports.html',
                controller: 'PendingController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
