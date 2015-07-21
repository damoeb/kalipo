'use strict';

kalipoApp.factory('Thread', function ($resource) {
    return $resource('app/rest/threads/:id/:selector', {}, {
        'query': { method: 'GET'},
        'get': { method: 'GET'},
        'latest': {method: 'GET', params: {id: '@id', selector: 'latest'}},
        'discussion': {method: 'GET', params: {selector: 'comments'}}
    });
});
