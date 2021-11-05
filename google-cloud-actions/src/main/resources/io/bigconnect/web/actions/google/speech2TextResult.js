/*eslint no-labels:0*/
define([
    'flight/lib/component'
], function (defineComponent) {
    'use strict';

    function OperationResult() {
        this.defaultAttrs({
            openButton: '.s2tOpResult'
        });

        this.after('initialize', function () {
            const $button = $('<button>').addClass("btn btn-xs s2tOpResult").text("Open");
            this.$node.empty().append($button);

            this.on('click', {
                openButton: this.onPathClick,
            });
        });

        this.onPathClick = function(event) {
            $(document).trigger('selectObjects', { vertexIds: [this.attr.process.vertexId] });
        }
    }

    return defineComponent(OperationResult);
});
