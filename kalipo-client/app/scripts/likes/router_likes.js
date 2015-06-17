'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/likes', {
                templateUrl: 'views/my-likes.html',
                controller: 'LikesController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
