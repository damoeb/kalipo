'use strict';

kalipoApp.controller('ReviewController', function ($scope, resolvedComment, Comment) {

    $scope.comments = resolvedComment;

    $scope.approve = function (id) {
        Comment.approve({id: id},
            function () {
                // todo remove
            });
    };

    $scope.reject = function (id) {
        Comment.reject({id: id},
            function () {
                // todo remove
            });
    };

    $scope.delete = function (id) {
        Comment.delete({id: id},
            function () {
                $scope.comments = Comment.query();
            });
    };
});
