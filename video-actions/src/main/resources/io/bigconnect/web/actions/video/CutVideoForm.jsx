define([
    'react',
    'react-dom',
    'prop-types',
    'create-react-class',
    'antd',
    'util/component/attacher',
    'moment',
    'public/v1/api'
], function(React, ReactDOM, PropTypes, createReactClass, antd, attacher, moment, bcApi) {
    'use strict';

    const {Row, Col, Button, Input, Alert} = antd;

    const TIME_REGEXP = new RegExp("^(?:(?:([01]?\\d|2[0-3]):)?([0-5]?\\d):)?([0-5]?\\d)$");

    return createReactClass({
        propTypes: {
            node: PropTypes.object.isRequired,
            data: PropTypes.object.isRequired,
        },

        getInitialState() {
            return {
                startTime: '',
                endTime: '',
                error: ''
            }
        },

        componentWillUnmount() {
            attacher()
                .node(this.props.node)
                .teardown();
        },

        onCut: function () {
            let errors = [];

            let startTimeMatch = TIME_REGEXP.exec(this.state.startTime);
            if (!startTimeMatch || (!startTimeMatch[1] && !startTimeMatch[2] && !startTimeMatch[3])) {
                errors.push("Invalid start time");
            }

            let endTimeMatch = TIME_REGEXP.exec(this.state.endTime);
            if (!endTimeMatch || (!endTimeMatch[1] && !endTimeMatch[2] && !endTimeMatch[3])) {
                errors.push("Invalid end time");
            }

            if (errors.length > 0) {
                this.setState({error: errors.join(", ")})
                return;
            } else {
                this.setState({error: null})
            }

            if (!startTimeMatch[1]) startTimeMatch[1] = "00";
            if (!startTimeMatch[2]) startTimeMatch[2] = "00";
            if (!endTimeMatch[1]) endTimeMatch[1] = "00";
            if (!endTimeMatch[2]) endTimeMatch[2] = "00";

            const startTime = moment(`${startTimeMatch[1]}:${startTimeMatch[2]}:${startTimeMatch[3]}`, "HH:mm:ss");
            const endTime = moment(`${endTimeMatch[1]}:${endTimeMatch[2]}:${endTimeMatch[3]}`, "HH:mm:ss");

            if (endTime.isBefore(startTime)) {
                this.setState({error: "End time is before start time"});
                return;
            }

            if (endTime.isSame(startTime)) {
                this.setState({error: "End time is equal to start time"});
                return;
            }

            bcApi.connect().then(({dataRequest}) => {
                dataRequest('video', 'cut', this.props.data.vertices[0].id, startTime.format('HH:mm:ss'), endTime.format('HH:mm:ss'))
                    .then(() => {
                        this.onCancel();
                    })
                    .catch((e) => {
                        console.log(e);
                    })
            });
        },

        onCancel: function () {
            attacher()
                .node(this.props.node)
                .teardown();

            this.props.node.parent().remove();
        },

        render: function () {
            const {error} = this.state;

            return (
                <div className="form">
                    {error && (
                        <Row justify="center" className="m-b-2">
                            <Col span={12} className="text-center">
                                <Alert message={error} type="error"/>
                            </Col>
                        </Row>
                    )}
                    <Row justify="center">
                        <Col span={24} className={"text-center"}>
                            <Input.Group compact>
                                <Input style={{width: 200, textAlign: 'center'}}
                                       placeholder="Start (HH:mm:ss)"
                                       onChange={(e) => this.setState({startTime: e.target.value})}
                                />
                                <Input
                                    style={{
                                        width: 40,
                                        borderLeft: 0,
                                        borderRight: 0,
                                        pointerEvents: 'none',
                                    }}
                                    placeholder="->" readOnly={true} disabled={true}
                                />
                                <Input style={{borderLeftWidth: 0, width: 200, textAlign: 'center'}}
                                       placeholder="End (HH:mm:ss)"
                                       onChange={(e) => this.setState({endTime: e.target.value})}
                                />
                            </Input.Group>
                        </Col>
                    </Row>
                    <Row justify="center">
                        <Col span={12} className="text-center m-y-2">
                            <Button danger style={{marginRight: 8}} onClick={() => this.onCancel()}>Cancel</Button>
                            <Button type="primary" onClick={() => this.onCut()}>Cut</Button>
                        </Col>
                    </Row>
                </div>
            )
        }
    });
});
