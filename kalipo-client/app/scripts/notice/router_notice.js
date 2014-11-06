'use strict';

kalipoApp.config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
            $routeProvider
                .when('/notices', {
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
