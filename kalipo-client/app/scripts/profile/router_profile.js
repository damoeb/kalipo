'use strict';

kalipoApp.config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
    $routeProvider
        .when('/profile', {
            templateUrl: 'views/profile-comments.html',
            controller: 'ProfileController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/profile/comments', {
            templateUrl: 'views/profile-comments.html',
            controller: 'ProfileController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/profile/notifications', {
            templateUrl: 'views/profile-notices.html',
            controller: 'ProfileNoticeController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/profile/likes', {
            templateUrl: 'views/profile-likes.html',
            controller: 'ProfileLikesController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/profile/achievements', {
            templateUrl: 'views/profile-achievements.html',
            controller: 'ProfileController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/profile/ignored-users', {
            templateUrl: 'views/profile-ignored-users.html',
            controller: 'ProfileController',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        ;
});
