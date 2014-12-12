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

                // todo groupBy resourceRef to avoid multiple requests on same resource

                _.forEach(achievements, function (achievement) {

                    $scope.achievements.push(achievement);

                    switch (achievement.type) {
                        case 'LIKE':
                        case 'LIKED':
                        case 'DISLIKE':
                        case 'DISLIKED':
                        case 'RM_COMMENT':

                            Comment.get({id: achievement.resourceRef}, function (comment) {
                                console.log('Resolved comment ' + comment.id);
                                achievement.$resource = comment;
                            });

                            break;
                        case 'REPORT':
                        case 'REPORTED':
                        case 'ABUSED_REPORT':

                            Report.get({id: achievement.resourceRef}, function (report) {
                                console.log('Resolved report ' + report.id);
                                achievement.$resource = report;
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
                $scope.$on('event:auth-authorized', doFetchAchievements)
                $scope.$on('event:auth-authorized', doFetchReputations)
            } else {
                console.log($rootScope.login);
                doFetchAchievements();
                doFetchReputations();
            }
        };

    }]);
