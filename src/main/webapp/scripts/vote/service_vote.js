'use strict';

kalipoApp.factory('Vote', function ($resource) {
    return $resource('app/rest/votes/:id', {}, {
        'query': { method: 'GET', isArray: true},
        'get': { method: 'GET'}
    });
});
