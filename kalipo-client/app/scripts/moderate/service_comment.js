'use strict';

kalipoApp.factory('Comment', function ($resource) {
    return $resource('app/rest/comments/:id', {}, {
        'query': { method: 'GET', isArray: true},
        'pendingInThread': {method: 'GET', isArray: true, params: {id: 'pending'}},
        'approve': { method: 'PUT', params: {id: '@id', page: 'approve'}},
        'reject': { method: 'PUT', params: {id: '@id', page: 'reject'}},
        'spam': { method: 'PUT', params: {id: '@id', page: 'reject'}},
        'deleteAndBan': { method: 'PUT', params: {id: '@id', page: 'delete+ban'}},
        'get': { method: 'GET'}
    });
});
