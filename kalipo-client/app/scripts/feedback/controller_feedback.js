'use strict';

kalipoApp.controller('FeedbackController', function ($scope, Feedback) {

    $scope.feedback = {};

    $scope.clear = function () {
        $scope.feedback = {};
    };

    $scope.submitFeedback = function () {
        Feedback.save($scope.feedback,
            function () {
                $scope.clear();
            });
    };

});
