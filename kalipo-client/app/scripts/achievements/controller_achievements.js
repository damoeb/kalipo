'use strict';

kalipoApp.controller('AchievementsController', ['$scope', '$rootScope', 'Vote', 'Comment', 'Achievement', 'Report', 'Reputation',
    function ($scope, $rootScope, Vote, Comment, Achievement, Report, Reputation) {

        $scope.pages = [];

        $scope.$page = 0;

        $scope.reputations = {};

        var __doFetchReputations = function () {
            Reputation.query(function (reputations) {
                _.forEach(reputations, function (reputation) {
                    $scope.reputations[reputation.type] = reputation.reputation;
                });
            });
        };

        $scope.next = function () {
            $scope.$page = 1;
            __doFetchAchievements();
        };

        var __doFetchAchievements = function () {
            console.log('Fetch achievement of ' + $rootScope.login);

            Achievement.list({'id': $rootScope.login, 'page': $scope.$page}, function (achievements) {

                console.log('Got ' + achievements.length + ' achievements on page ' + $scope.$page);

                var resources = {};

                var groupedByRef = _.groupBy(achievements, function (achievement) {
                    return achievement.resourceRef;
                });

                var distinctResources = _.keys(groupedByRef);

                console.log('Fetching ' + distinctResources.length + ' resources');

                var __refresh = function () {
                    // attach resource to achievement
                    _.forEach(achievements, function (achievement) {

                        achievement.$resource = resources[achievement.resourceRef];
                    });
                };

                $scope.pages.push({
                    page: $scope.$page,
                    achievements: achievements
                });

                _.forEach(distinctResources, function(resourceRef){

                    var first = groupedByRef[resourceRef][0];

                    switch (first.type) {
                        case 'LIKE':
                        case 'LIKED':
                        case 'DISLIKE':
                        case 'DISLIKED':
                        case 'RM_COMMENT':

                            Comment.get({id: resourceRef}, function (comment) {
                                console.log('Resolved comment ' + comment.id);
                                comment.$isComment = true;
                                resources[resourceRef] = comment;
                                __refresh();
                            });

                            break;
                        case 'REPORT':
                        case 'REPORTED':
                        case 'ABUSED_REPORT':

                            Report.get({id: resourceRef}, function (report) {
                                console.log('Resolved report ' + report.id);
                                report.$isComment = false;
                                resources[resourceRef] = report;
                                __refresh();
                            });

                            break;
                        case 'WELCOME':
                            // no resource
                            break;

                    }
                });

            })
        };

        $scope.fetchAchievements = function () {

            if (typeof($rootScope.login) == 'undefined') {
                console.log('wait');
                $scope.$on('event:auth-authorized', __doFetchAchievements);
                $scope.$on('event:auth-authorized', __doFetchReputations);
            } else {
                console.log($rootScope.login);
                __doFetchAchievements();
                __doFetchReputations();
            }
        };

    }]);
