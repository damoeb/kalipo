'use strict';

kalipoApp.factory('Websocket', function (Thread) {

    return {

        getCommentId: function (message) {
            switch(message.type.toLowerCase()) {
                case 'vote':
                    return message.event.commentId;
                default :
                    return message.event.id;
            }
        },

        unsubscribe: function (socket) {
            console.log('unsubscribe socket');
            socket.unsubscribe();
        },

        subscribe: function (onMessage) {
            console.log('subscribe socket');
            var socket = atmosphere;
            var subSocket;
            var transport = 'websocket';

            var request = {
                url: 'websocket/live/channel',
                contentType: "application/json",
                transport: transport,
                trackMessageLength: true,
                reconnectInterval: 5000,
                enableXDR: true,
                timeout: 60000
            };

            request.onOpen = function (response) {
                transport = response.transport;
                request.uuid = response.request.uuid;
            };

            request.onMessage = function (response) {
                var message = atmosphere.util.parseJSON(response.responseBody);
                onMessage(message);
            };

            socket.subscribe(request);

            return socket;
        }

    }
});
