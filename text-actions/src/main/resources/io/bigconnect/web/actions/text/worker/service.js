define('data/web-worker/services/text', ['data/web-worker/util/ajax'], function (ajax) {
    'use strict';

    return {
        translate: function (vertexId) {
            return ajax('POST', '/text/translate', { id: vertexId })
        },
        summary: function (vertexId) {
            return ajax('POST', '/text/summary', { id: vertexId })
        },
    }
})
