'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/thread', {
                templateUrl: 'views/threads.html',
                controller: 'ThreadController',
                resolve: {
                    resolvedThread: ['Thread', function (Thread) {
                        return Thread.query();
                    }]
                },
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
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
            .when('/thread/:threadId/:commentId', {
                templateUrl: 'views/thread-view.html',
                controller: 'ViewThreadController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
