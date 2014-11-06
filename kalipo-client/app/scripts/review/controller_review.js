'use strict';

kalipoApp.controller('ReviewCommentController', function ($scope, resolvedComment, Comment) {

        $scope.comments = resolvedComment;

        $scope.update = function (id) {
            $scope.comment = Comment.get({id: id});
            $('#saveCommentModal').modal('show');
        };

        $scope.delete = function (id) {
            Comment.delete({id: id},
                function () {
                    $scope.comments = Comment.query();
                });
        };

    // todo approve, reject

        $scope.clear = function () {
            $scope.comment = {id: null, threadId: null, parentId: null, text: null};
        };
    });
