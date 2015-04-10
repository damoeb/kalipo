'use strict';

kalipoApp.controller('BrowseController', function ($scope, Thread, Comment) {

    $scope.currentPage = 0;
    $scope.$isLastPage = true;

    // -- Scope Functions -- ---------------------------------------------------------------------------------------

    $scope.loadMore = function () {
        console.log("load more");

        $scope.currentPage = $scope.currentPage + 1;

        Thread.query(function (result) {
            $scope.$isLastPage = result.lastPage;

            // group by days
            $scope.byDays = _.groupBy(result.content, function(thread) {
                return moment(thread.createdDate).format('dddd, DD.MM.YYYY');
            });
        });
    };

    $scope.loadMore();
});
