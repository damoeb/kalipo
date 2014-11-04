'use strict';

App.factory('Notice', function ($resource) {
        return $resource('app/rest/notices/:id', {}, {
            'query': { method: 'GET', isArray: true},
            'get': { method: 'GET'}
        });
    });
