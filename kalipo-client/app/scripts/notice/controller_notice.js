'use strict';

kalipoApp.controller('NoticeController', function ($rootScope, $scope, $sce, Session, Notice) {

    $scope.$page = 0;
    $scope.$pageCount = 0;
    $scope.$lastPage = true;
    $scope.$firstPage = true;

    var __customize = function (notices) {

        var comp_custom_notice = _.template('<strong><%- notice.initiatorId %></strong> mentioned you <a href="#/find/comment:<%- notice.resourceId %>">here</a>');

        _.forEach(notices, function (notice) {
            switch (notice.type) {
                case 'MENTION':
                    notice.$customized = true;
                    notice.$custom = $sce.trustAsHtml(comp_custom_notice({notice: notice}));
                    break;
                default:
                    //LIKE, REPLY, DELETION, REPORT, REVIEW, FRAUDULENT_USER, REJECTED, PENDING, APPROVAL
                    notice.$customized = false;
            }
        });

        return notices;
    };

    $scope.nextPage = function () {
        if ($scope.$page + 1 < $scope.$pageCount) {
            $scope.$page++;
            $scope.fetchAndRender();
        }
    };

    $scope.previousPage = function () {
        if ($scope.$page > 0) {
            $scope.$page--;
            $scope.fetchAndRender();
        }
    };

    // todo this is similar to achievements, combine it somehow

    $scope.fetchAndRender = function () {
        __fetch(function(response) {
            $scope.$pageCount = response.totalPages;
            $scope.$lastPage = response.lastPage;
            $scope.$firstPage = response.firstPage;
            // todo group by resource, enrich with resource(e.g. comment)
            $scope.notices = __customize(response.content);

            Notice.seenUntilNow({userId: Session.login});
        });
    };

    var __fetch = function (onSuccess) {

        var __doFetchNotices = function () {
            Notice.query({userId: $rootScope.login, page: $scope.$page}, function (response) {
                if(_.isFunction(onSuccess)) {
                    onSuccess(response);
                }
            });
        };

        if (typeof($rootScope.login) == 'undefined') {
            console.log('wait');
            $scope.$on('event:auth-authorized', __doFetchNotices);
        } else {
            console.log($rootScope.login);
            __doFetchNotices();
        }
    };

    $scope.hasFreshNotices = function () {
        __fetch(function(response) {
            var freshCount = 0;
            _.forEach(response.content, function(n) {
                if(!n.seen) {
                    freshCount ++;
                }
            });
            $scope.$freshNoticesCount = freshCount;
        });
    };

});
