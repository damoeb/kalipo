'use strict';

kalipoApp.factory('Report', function ($resource) {
    return $resource('app/rest/reports/:id/:filter', {}, {
        'pendingInThread': {method: 'GET', isArray: true, params: {id: 'pending'}},
        'approve': {method: 'PUT', params: {id: '@id', filter: 'approve'}},
        'reject': {method: 'PUT', params: {id: '@id', filter: 'reject'}},
        'query': { method: 'GET', isArray: true},
        'get': { method: 'GET'}
    });
});
