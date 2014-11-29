'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/moderate/:threadId', {
                templateUrl: 'views/moderate.html',
                controller: 'ModerateController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
