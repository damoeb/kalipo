'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/reputation', {
                templateUrl: 'views/reputations.html',
                controller: 'ReputationController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
