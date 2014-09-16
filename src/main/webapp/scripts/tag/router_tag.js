'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/tag', {
                templateUrl: 'views/tags.html',
                controller: 'TagController',
                resolve: {
                    resolvedTag: ['Tag', function (Tag) {
                        return Tag.query();
                    }]
                },
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
