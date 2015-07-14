'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/discuss', {
                templateUrl: 'views/discussion-create.html',
                controller: 'CreateThreadController',
                access: {
                    authorizedRoles: [USER_ROLES.user]
                }
            })
            .when('/comments/:threadId', {
                templateUrl: 'views/discussion.html',
                controller: 'DiscussionController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
            .when('/comments/:threadId/edit', {
                templateUrl: 'views/discussion-edit.html',
                controller: 'DiscussionController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })

    });
