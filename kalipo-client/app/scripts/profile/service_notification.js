'use strict';

kalipoApp.factory('Notice', function ($resource) {
    return $resource('app/rest/notifications/:userId/:filter', {}, {
        'query': {method: 'GET', params: {userId: '@userId'}},
        'seenUntilNow': {method: 'POST', params: {userId: '@userId', filter: 'seen'}}
    });
});
