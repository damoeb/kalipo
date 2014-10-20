'use strict';

kalipoApp.factory('Thread', function ($resource) {
    return $resource('app/rest/threads/:id/:selector', {}, {
        'query': { method: 'GET', isArray: true},
        'get': { method: 'GET'},
        'getComments': { method: 'GET', params: {selector: 'comments'}, isArray: true}
    });
});
