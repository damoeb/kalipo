'use strict';

kalipoApp.factory('Report', function ($resource) {
    return $resource('app/rest/reports/:id', {}, {
        'query': { method: 'GET', isArray: true},
        'get': { method: 'GET'}
    });
});
