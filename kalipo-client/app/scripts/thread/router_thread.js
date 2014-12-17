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
                templateUrl: 'views/discussion.html',
                controller: 'DiscussionController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
