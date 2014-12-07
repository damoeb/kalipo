'use strict';

kalipoApp.factory('Vote', function ($resource) {
    return $resource('app/rest/votes/:id/:filter', {}, {
        'query': { method: 'GET', isArray: true},
        'byAuthor': {method: 'GET', isArray: true},
        'get': { method: 'GET'}
    });
});
