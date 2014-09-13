'use strict';

kalipoApp.factory('Comment', function ($resource) {
        return $resource('app/rest/comments/:id', {}, {
            'query': { method: 'GET', isArray: true},
            'get': { method: 'GET'}
        });
    });
