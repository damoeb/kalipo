'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
            $routeProvider
                .when('/thread', {
                    templateUrl: 'views/threads.html',
                    controller: 'ThreadController',
                    resolve:{
                        resolvedThread: ['Thread', function (Thread) {
                            return Thread.query();
                        }]
                    },
                    access: {
                        authorizedRoles: [USER_ROLES.all]
                    }
                })
        });
