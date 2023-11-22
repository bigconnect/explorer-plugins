define('data/web-worker/services/nvr', ['data/web-worker/util/ajax'], function (ajax) {
    'use strict';

    return {
        cut: function (vertexId, startTime, endTime) {
            return ajax('POST', '/video/cut', {
                vertexId,
                startTime,
                endTime
            });
        },

        merge: function (vertexIds, title) {
            return ajax('POST', '/video/merge', {
                vertexIds,
                title
            });
        }
    }
})
