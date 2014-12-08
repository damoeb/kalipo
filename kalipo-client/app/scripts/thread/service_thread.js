'use strict';

kalipoApp.factory('Thread', function ($resource) {
    return $resource('app/rest/threads/:id/:selector', {}, {
        'query': { method: 'GET', isArray: true},
        'get': { method: 'GET'},
        'update': { method: 'PUT', params: {id: '@id'}},
        'discussion': {method: 'GET', params: {selector: 'comments', page: 0}}
    });
});
