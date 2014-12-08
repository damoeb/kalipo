'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/discuss', {
                templateUrl: 'views/thread-create.html',
                controller: 'CreateThreadController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
            .when('/thread/:threadId', {
                templateUrl: 'views/thread-view.html',
                controller: 'ViewThreadController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
