'use strict';

kalipoApp.config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
    $routeProvider
        .when('/dashboard/:userId', {
            templateUrl: 'views/dashboard.html',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/dashboard/:userId/notifications', {
            templateUrl: 'views/my-notices.html',
            controller: 'NoticeController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/likes', {
            templateUrl: 'views/my-likes.html',
            controller: 'LikesController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/achievements', {
            templateUrl: 'views/my-achievements.html',
            controller: 'AchievementsController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
});
