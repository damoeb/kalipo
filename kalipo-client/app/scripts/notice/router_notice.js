'use strict';

App
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
            $routeProvider
                .when('/notice', {
                    templateUrl: 'views/notices.html',
                    controller: 'NoticeController',
                    resolve:{
                        resolvedNotice: ['Notice', function (Notice) {
                            return Notice.query().$promise;
                        }]
                    },
                    access: {
                        authorizedRoles: [USER_ROLES.all]
                    }
                })
        });
