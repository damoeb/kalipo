'use strict';

kalipoApp.factory('Comment', function ($resource) {
    return $resource('app/rest/comments/:id/:page', {}, {
            'query': { method: 'GET', isArray: true},
        'getCandidates': { method: 'GET', isArray: true, params: {id: 'review', page: 0}},
        'approve': { method: 'POST'},
        'reject': { method: 'POST'},
            'get': { method: 'GET'}
        });
    });
