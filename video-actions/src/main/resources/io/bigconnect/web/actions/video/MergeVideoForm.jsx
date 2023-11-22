define([
    'react',
    'react-dom',
    'prop-types',
    'create-react-class',
    'antd',
    'util/component/attacher',
    'moment',
    'public/v1/api',
    'util/vertex/formatters',
    'react-sortable-hoc',
    'array-move',
    'antd-icons',
], function (React, ReactDOM, PropTypes, createReactClass, antd, attacher, moment, bcApi, F, sortableHOC, arrayMove, antdIcons) {
    'use strict';

    const {Row, Col, Button, Table, Form, Input, Typography, Alert} = antd;
    const {MenuOutlined} = antdIcons;
    const {sortableContainer, sortableElement, sortableHandle} = sortableHOC;
    const {arrayMoveImmutable} = arrayMove;

    const DragHandle = sortableHandle(() => <MenuOutlined style={{cursor: 'grab', color: '#999'}}/>);

    const columns = [
        {
            title: 'Sort',
            dataIndex: 'sort',
            width: 30,
            className: 'drag-visible',
            render: () => <DragHandle/>,
        },
        {
            title: 'Title',
            dataIndex: 'title',
            className: 'drag-visible',
        },
    ];

    const SortableItem = sortableElement(props => <tr {...props} />);
    const SortableContainer = sortableContainer(props => <tbody {...props} />);

    return createReactClass({
        propTypes: {
            node: PropTypes.object.isRequired,
            data: PropTypes.object.isRequired,
        },

        getInitialState() {
            const data = _.map(this.props.data.vertices, (v, idx) => {
                return {
                    key: `${idx}`,
                    index: idx,
                    title: F.vertex.title(v),
                    id: v.id
                }
            });

            return {
                dataSource: data,
                error: ''
            }
        },

        componentWillUnmount() {
            attacher()
                .node(this.props.node)
                .teardown();
        },

        onMerge(values) {
            const vertexIds = _.map(this.state.dataSource, v => v.id);
            bcApi.connect().then(({dataRequest}) => {
                dataRequest('nvr', 'merge', vertexIds, values.title)
                    .then(() => {
                        this.onCancel();
                    })
                    .catch((e) => {
                        console.log(e);
                    })
            });
        },

        onCancel() {
            attacher()
                .node(this.props.node)
                .teardown();

            this.props.node.parent().remove();
        },

        onSortEnd({oldIndex, newIndex}) {
            const {dataSource} = this.state;
            if (oldIndex !== newIndex) {
                const newData = arrayMoveImmutable([].concat(dataSource), oldIndex, newIndex).filter(el => !!el);
                this.setState({dataSource: newData});
            }
        },

        DraggableContainer(props) {
            return <SortableContainer
                useDragHandle
                disableAutoscroll
                helperClass="row-dragging"
                onSortEnd={this.onSortEnd}
                {...props}
            />
        },

        DraggableBodyRow({className, style, ...restProps}) {
            const {dataSource} = this.state;
            // function findIndex base on Table rowKey props and should always be a right array index
            const index = dataSource.findIndex(x => x.index === restProps['data-row-key']);
            return <SortableItem index={index} {...restProps} />;
        },

        render() {
            const {error, dataSource} = this.state;

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
                        <Col span={24} className={"text-left"}>
                            <Typography.Title level={5}>Ordering:</Typography.Title>
                            <Table
                                size={"small"}
                                showHeader={false}
                                pagination={false}
                                dataSource={dataSource}
                                columns={columns}
                                rowKey="index"
                                components={{
                                    body: {
                                        wrapper: this.DraggableContainer,
                                        row: this.DraggableBodyRow,
                                    },
                                }}
                            />
                        </Col>
                    </Row>
                    <Row justify="center">
                        <Col span={24} className={"text-left"}>
                            <Form
                                labelCol={{span: 4}}
                                wrapperCol={{span: 16}}
                                onFinish={this.onMerge}
                                autoComplete="off"
                            >
                                <Form.Item
                                    label="Title"
                                    name="title"
                                    rules={[{required: true, message: 'Title is required!'}]}
                                >
                                    <Input/>
                                </Form.Item>

                                <Form.Item wrapperCol={{offset: 8, span: 16}}>
                                    <Button danger style={{marginRight: 8}}
                                            onClick={() => this.onCancel()}>Cancel</Button>
                                    <Button type="primary" htmlType="submit">Submit</Button>
                                </Form.Item>
                            </Form>
                        </Col>
                    </Row>
                </div>
            )
        }
    });
});
