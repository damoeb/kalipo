'use strict';

kalipoApp.factory('Reputation', function ($resource) {
    return $resource('app/rest/reputations/:id', {}, {
        'query': { method: 'GET', isArray: true},
        'get': { method: 'GET'}
    });
});
