'use strict';

kalipoApp.config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
    $routeProvider
        .when('/tools', {
            templateUrl: 'views/metrics.html',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/tools/metrics', {
            templateUrl: 'views/metrics.html',
            controller: 'MetricsController',
            access: {
                authorizedRoles: [USER_ROLES.admin]
            }
        })
        .when('/tools/tracker', {
            templateUrl: 'views/tracker.html',
            controller: 'TrackerController',
            access: {
                authorizedRoles: [USER_ROLES.admin]
            }
        })
        .when('/tools/logs', {
            templateUrl: 'views/logs.html',
            controller: 'LogsController',
            resolve: {
                resolvedLogs: ['LogsService', function (LogsService) {
                    return LogsService.findAll();
                }]
            },
            access: {
                authorizedRoles: [USER_ROLES.admin]
            }
        })
        .when('/tools/audits', {
            templateUrl: 'views/audits.html',
            controller: 'AuditsController',
            access: {
                authorizedRoles: [USER_ROLES.admin]
            }
        })
});
