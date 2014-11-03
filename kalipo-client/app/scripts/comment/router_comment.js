'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
            $routeProvider
                .when('/review', {
                    templateUrl: 'views/comments.html',
                    controller: 'ReviewCommentController',
                    resolve:{
                        resolvedComment: ['Comment', function (Comment) {
                            return Comment.getCandidates();
                        }]
                    },
                    access: {
                        authorizedRoles: [USER_ROLES.all]
                    }
                })
        });
