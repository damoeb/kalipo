'use strict';

kalipoApp.factory('Comment', function ($resource) {
    return $resource('app/rest/comments/:id/:page', {}, {
        'query': { method: 'GET', isArray: true},
        'pendingInThread': {method: 'GET', isArray: true, params: {id: 'pending'}},
        'approve': { method: 'PUT', params: {id: '@id', page: 'approve'}},
        'reject': { method: 'PUT', params: {id: '@id', page: 'reject'}},
        'get': { method: 'GET'}
    });
});
