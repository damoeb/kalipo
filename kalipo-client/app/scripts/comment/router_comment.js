'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
            $routeProvider
                .when('/comment', {
                    templateUrl: 'views/comments.html',
                    controller: 'CommentController',
                    resolve:{
                        resolvedComment: ['Comment', function (Comment) {
                            return Comment.query();
                        }]
                    },
                    access: {
                        authorizedRoles: [USER_ROLES.all]
                    }
                })
        });
