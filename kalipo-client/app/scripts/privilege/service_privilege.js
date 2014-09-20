'use strict';

kalipoApp.factory('Privilege', function ($resource) {
    return $resource('app/rest/privileges/:id', {}, {
        'query': { method: 'GET', isArray: true},
        'get': { method: 'GET'}
    });
});
