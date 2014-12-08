'use strict';

kalipoApp.factory('Feedback', function ($resource) {
    return $resource('app/rest/feedbacks/:id', {}, {
        'query': { method: 'GET', isArray: true},
        'get': { method: 'GET'}
    });
});
