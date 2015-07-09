'use strict';

kalipoApp.config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
    $routeProvider
        .when('/tools', {
            templateUrl: 'views/tools-metrics.html',
            access: {
                authorizedRoles: [USER_ROLES.all]
            }
        })
        .when('/tools/metrics', {
            templateUrl: 'views/tools-metrics.html',
            controller: 'MetricsController',
            access: {
                authorizedRoles: [USER_ROLES.admin]
            }
        })
        .when('/tools/tracker', {
            templateUrl: 'views/tools-tracker.html',
            controller: 'TrackerController',
            access: {
                authorizedRoles: [USER_ROLES.admin]
            }
        })
        .when('/tools/logs', {
            templateUrl: 'views/tools-logs.html',
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
            templateUrl: 'views/tools-audits.html',
            controller: 'AuditsController',
            access: {
                authorizedRoles: [USER_ROLES.admin]
            }
        })
        .when('/tools/privileges', {
            templateUrl: 'views/tools-privileges.html',
            controller: 'PrivilegeController',
            resolve: {
                resolvedPrivilege: ['Privilege', function (Privilege) {
                    return Privilege.query();
                }]
            },
            access: {
                authorizedRoles: [USER_ROLES.admin]
            }
        })
});
