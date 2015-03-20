'use strict';

kalipoApp.factory('Notice', function ($resource) {
    return $resource('app/rest/notices/:userId/:filter', {}, {
        'query': {method: 'GET', params: {userId: '@userId'}},
        'seenUntilNow': {method: 'POST', params: {userId: '@userId', filter: 'seen'}}
    });
});
