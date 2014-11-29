'use strict';

kalipoApp.factory('Report', function ($resource) {
    return $resource('app/rest/reports/:id', {}, {
        'pendingInThread': {method: 'GET', isArray: true, params: {id: 'pending'}},
        'query': { method: 'GET', isArray: true},
        'get': { method: 'GET'}
    });
});
