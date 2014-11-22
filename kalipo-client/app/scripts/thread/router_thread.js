'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/thread/create', {
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
            .when('/thread/:threadId/share', {
                templateUrl: 'views/thread-view.html',
                controller: 'ViewThreadController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
            .when('/thread/:threadId/edit', {
                templateUrl: 'views/thread-edit.html',
                controller: 'EditThreadController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
            .when('/thread/:threadId/:commentId', {
                templateUrl: 'views/thread-view.html',
                controller: 'ViewThreadController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
