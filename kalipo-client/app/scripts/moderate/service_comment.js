'use strict';

kalipoApp.factory('Comment', function ($resource) {
    return $resource('app/rest/comments/:id/:method', {}, {
        'query': { method: 'GET'},
        'approve': { method: 'PUT', params: {id: '@id', method: 'approve'}},
        'reject': { method: 'PUT', params: {id: '@id', method: 'reject'}},
        'spam': { method: 'PUT', params: {id: '@id', method: 'spam'}},
        'deleteAndBan': { method: 'PUT', params: {id: '@id', method: 'delete+ban'}},
        'get': { method: 'GET'}
    });
});
