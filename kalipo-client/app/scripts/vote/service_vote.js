'use strict';

kalipoApp.factory('Vote', function ($resource) {
    return $resource('app/rest/votes/:id/:filter', {}, {
        'query': { method: 'GET', isArray: true},
        'likes': {method: 'GET', isArray: true, params: {'filter': 'like'}},
        'get': { method: 'GET'}
    });
    //'id':'@id',
});
