define('data/web-worker/services/image', ['data/web-worker/util/ajax'], function (ajax) {
    'use strict';

    return {
        objects: function (vertexId) {
            return ajax('POST', '/image/objects', { id: vertexId })
        },
        ocr: function (vertexId) {
            return ajax('POST', '/image/ocr', { id: vertexId })
        },
        caption: function (vertexId) {
            return ajax('POST', '/image/caption', { id: vertexId })
        },
    }
})
