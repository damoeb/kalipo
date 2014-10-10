'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/reputation', {
                templateUrl: 'views/reputations.html',
                controller: 'ReputationController',
                resolve: {
                    resolvedReputation: ['Reputation', function (Reputation) {
                        return Reputation.query();
                    }]
                },
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
