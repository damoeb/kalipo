'use strict';

kalipoApp
    .config(function ($routeProvider, $httpProvider, $translateProvider, USER_ROLES) {
        $routeProvider
            .when('/report', {
                templateUrl: 'views/reports.html',
                controller: 'ReportController',
                resolve: {
                    resolvedReport: ['Report', function (Report) {
                        return Report.query();
                    }]
                },
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
