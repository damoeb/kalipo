'use strict';

kalipoApp.factory('Comment', function ($resource) {
    return $resource('app/rest/comments/:id/:page', {}, {
        'query': { method: 'GET', isArray: true},
        'reviewList': { method: 'GET', isArray: true, params: {id: 'reviews'}},
        'approve': { method: 'PUT', params: {id: '@id', page: 'approve'}},
        'reject': { method: 'PUT', params: {id: '@id', page: 'reject'}},
        'get': { method: 'GET'}
    });
});
