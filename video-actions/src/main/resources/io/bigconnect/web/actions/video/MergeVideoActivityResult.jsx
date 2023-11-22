/*eslint no-labels:0*/
define([
    'react',
    'create-react-class',
    'antd'
], function (React, createReactClass, antd) {
    'use strict';

    const {Button} = antd;

    return createReactClass({
        showResult() {
            $(document).trigger('selectObjects', { vertexIds: [this.props.process.finalId] });
        },

        render() {
            return (
                <Button type="primary" size="small" onClick={() => this.showResult()}>Show result</Button>
            )
        },
    });
});
