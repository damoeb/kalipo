'use strict';

kalipoApp.config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
    $routeProvider
        .when('/profile', {
            templateUrl: 'views/profile-tab-comments.html',
            controller: 'ProfileController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/profile/comments', {
            templateUrl: 'views/profile-tab-comments.html',
            controller: 'ProfileController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/profile/notifications', {
            templateUrl: 'views/profile-tab-notices.html',
            controller: 'ProfileNoticeController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/profile/likes', {
            templateUrl: 'views/profile-tab-likes.html',
            controller: 'ProfileLikesController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/profile/achievements', {
            templateUrl: 'views/profile-tab-achievements.html',
            controller: 'ProfileController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        ;
});
