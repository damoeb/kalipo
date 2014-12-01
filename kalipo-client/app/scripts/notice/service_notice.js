'use strict';

kalipoApp.factory('Notice', function ($resource, $rootScope) {
    return $resource('app/rest/notices/:userId/:filter', {}, {
        'query': { method: 'GET', isArray: true, params: {userId: '@userId'}},
        'hasUnseen': { method: 'GET', params: {userId: '@userId', 'filter': 'unseen'}},
        'seenUntilNow': { method: 'PUT', params: {userId: '@userId'}},
            'get': { method: 'GET'}
        });
    });
