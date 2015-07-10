'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/discuss', {
                templateUrl: 'views/create-discussion.html',
                controller: 'CreateThreadController',
                access: {
                    authorizedRoles: [USER_ROLES.user]
                }
            })
            .when('/v/:threadId', {
                templateUrl: 'views/discussion.html',
                controller: 'DiscussionController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
