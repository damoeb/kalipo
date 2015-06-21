'use strict';

kalipoApp.factory('Thread', function ($resource) {
    return $resource('app/rest/threads/:id/:selector', {}, {
        'query': { method: 'GET'},
        'get': { method: 'GET'},
        'update': { method: 'PUT', params: {id: '@id'}},
        'latest': {method: 'GET', params: {id: '@id', selector: 'latest'}},
        'discussion': {method: 'GET', params: {selector: 'comments', page: 0}},
        'diff': {method: 'GET', params: {selector: 'diff'}, isArray: true}
    });
});
