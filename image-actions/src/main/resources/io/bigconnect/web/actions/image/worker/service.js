define('data/web-worker/services/image', ['data/web-worker/util/ajax'], function (ajax) {
    'use strict';

    return {
        objects: function (vertexId) {
            return ajax('POST', '/image/objects', { id: vertexId })
        }
    }
})
