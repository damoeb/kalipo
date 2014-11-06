'use strict';

kalipoApp.factory('Notice', function ($resource, $rootScope) {
        return $resource('app/rest/notices/:id/:optPage', {}, {
            // todo fix retrieve username from $rootScope
            'query': { method: 'GET', isArray: true, params:{id: 'admin', optPage:0}},
            'get': { method: 'GET'}
        });
    });
