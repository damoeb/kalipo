'use strict';

kalipoApp.factory('Comment', function ($resource) {
    return $resource('app/rest/comments/:id', {}, {
        'query': { method: 'GET'},
        'approve': { method: 'PUT', params: {id: '@id', page: 'approve'}},
        'reject': { method: 'PUT', params: {id: '@id', page: 'reject'}},
        'spam': { method: 'PUT', params: {id: '@id', page: 'reject'}},
        'deleteAndBan': { method: 'PUT', params: {id: '@id', page: 'delete+ban'}},
        'get': { method: 'GET'}
    });
});
