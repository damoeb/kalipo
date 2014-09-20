'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/vote', {
                templateUrl: 'views/votes.html',
                controller: 'VoteController',
                resolve: {
                    resolvedVote: ['Vote', function (Vote) {
                        return Vote.query();
                    }]
                },
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
