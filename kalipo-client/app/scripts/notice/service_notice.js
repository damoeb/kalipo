'use strict';

kalipoApp.factory('Notice', function ($resource) {
    return $resource('app/rest/notices/:userId/:filter', {}, {
        'query': {method: 'GET', params: {userId: '@userId'}},
        'hasUnseen': {method: 'GET', params: {userId: '@userId', 'filter': 'unseen'}},
        'seenUntilNow': {method: 'PUT', params: {userId: '@userId', filter: 'seen'}}
    });
});
