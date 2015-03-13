'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/achievements', {
                templateUrl: 'views/my-achievements.html',
                controller: 'AchievementsController',
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
