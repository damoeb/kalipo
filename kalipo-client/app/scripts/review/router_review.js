'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/review', {
                templateUrl: 'views/review.html',
                controller: 'ReviewController',
                resolve: {
                    resolvedComment: ['Comment', function (Comment) {
                        return Comment.reviewList({userId: 'admin'});
                    }]
                },
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
