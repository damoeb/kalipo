'use strict';

kalipoApp.controller('AchievementsController', ['$scope', '$rootScope', 'Vote', 'Comment', 'Achievement', 'Report', 'Reputation',
    function ($scope, $rootScope, Vote, Comment, Achievement, Report, Reputation) {

        $scope.achievements = [];

        $scope.$page = 0;

        $scope.reputations = {};

        var doFetchReputations = function () {
            Reputation.query(function (reputations) {
                _.forEach(reputations, function (reputation) {
                    $scope.reputations[reputation.type] = reputation.reputation;
                });
            });
        };

        var doFetchAchievements = function () {
            console.log('Fetch achievement of ' + $rootScope.login);

            Achievement.latest({'id': $rootScope.login, 'page': $scope.$page}, function (achievements) {

                console.log('Got ' + achievements.length + ' achievements on page ' + $scope.$page);

                var resources = {};

                var groupedByRef = _.groupBy(achievements, function (achievement) {
                    return achievement.resourceRef;
                });

                var distinctResources = _.keys(groupedByRef);

                console.log('Fetching ' + distinctResources.length + ' resources');

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
                                resources[resourceRef] = comment;
                            });

                            break;
                        case 'REPORT':
                        case 'REPORTED':
                        case 'ABUSED_REPORT':

                            Report.get({id: resourceRef}, function (report) {
                                console.log('Resolved report ' + report.id);
                                resources[resourceRef] = report;
                            });

                            break;
                        case 'WELCOME':
                            // no resource
                            break;

                    }
                });


                // attach resource to achievement
                _.forEach(achievements, function (achievement) {

                    $scope.achievements.push(achievement);
                    achievement.$resource = resources[achievement.resourceRef];

                });
            })
        };

        $scope.fetchAchievements = function () {

            if (typeof($rootScope.login) == 'undefined') {
                console.log('wait');
                $scope.$on('event:auth-authorized', doFetchAchievements);
                $scope.$on('event:auth-authorized', doFetchReputations);
            } else {
                console.log($rootScope.login);
                doFetchAchievements();
                doFetchReputations();
            }
        };

    }]);
