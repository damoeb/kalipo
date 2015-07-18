'use strict';

kalipoApp.factory('Websocket', function (Thread) {

    return {

        getCommentId: function (message) {
            switch(message.type.toLowerCase()) {
                case 'vote':
                    return message.data.commentId;
                default :
                    return message.data.id;
            }
        },

        unsubscribe: function (socket) {
            console.log('unsubscribe');
            socket.unsubscribe();
        },

        subscribe: function (threadId, onMessage) {
            console.log('subscribe to', threadId);

            var socket = atmosphere;
            var subSocket;
            var transport = 'websocket';

            var request = {
                url: 'websocket/live/' + threadId,
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
