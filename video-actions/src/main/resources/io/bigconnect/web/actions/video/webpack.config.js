var path = require('path');
var webpack = require('webpack');

var BcAmdExternals = [
    'antd',
    'antd-icons',
    'components/DroppableHOC',
    'product/toolbar/ProductToolbar',
    'components/RegistryInjectorHOC',
    'components/Alert',
    'components/Attacher',
    'components/Modal',
    'components/ConfirmDialog',
    'configuration/plugins/registry',
    'data/web-worker/store/actions',
    'data/web-worker/store/product/actions-impl',
    'data/web-worker/store/product/actions',
    'data/web-worker/store/user/actions-impl',
    'data/web-worker/store/user/actions',
    'data/web-worker/store/user/selectors',
    'data/web-worker/store/product/selectors',
    'data/web-worker/store/selection/actions',
    'data/web-worker/store/user/actions-impl',
    'data/web-worker/store/element/actions-impl',
    'data/web-worker/store/element/selectors',
    'data/web-worker/store/selection/actions-impl',
    'data/web-worker/store/undo/actions-impl',
    'data/web-worker/store/undo/actions',
    'data/web-worker/store/workspace/actions-impl',
    'data/web-worker/store/workspace/actions',
    'data/web-worker/store/ontology/selectors',
    'data/web-worker/util/ajax',
    'public/v1/api',
    'util/component/attacher',
    'util/formatters',
    'util/vertex/formatters',
    'util/retina',
    'util/dnd',
    'util/deepObjectCache',
    'util/parsers',
    'util/withContextMenu',
    'util/withDataRequest',
    'util/withTeardown',
    'util/withFormFieldErrors',
    'util/ontology/relationshipSelect',
    'detail/dropdowns/propertyForm/justification',
    'util/visibility/edit',
    'flight/lib/component',
    'fast-json-patch',
    'updeep',
    'underscore',
    'colorjs',
    'react',
    'create-react-class',
    'prop-types',
    'react-dom',
    'redux',
    'react-redux',
    'react-virtualized',
    'react-table',
    'px/extensions/growl',
    'videojs',
    "moment"
].map(path => ({[path]: {amd: path, commonjs2: false, commonjs: false}}));

var baseConfig = {
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: '[name].js',
        library: '[name]',
        libraryTarget: 'umd',
    },
    externals: BcAmdExternals,
    resolve: {
        extensions: ['.js', '.jsx', '.hbs']
    },
    module: {
        rules: [
            {
                test: /\.jsx?$/,
                exclude: /(dist|node_modules)/,
                use: [
                    {loader: 'babel-loader'}
                ]
            }
        ]
    },
    devtool: 'source-map'
};

module.exports = [
    Object.assign({}, baseConfig, {
        entry: {
            MergeVideoForm: './MergeVideoForm.jsx',
            CutVideoForm: './CutVideoForm.jsx',
            CombineVideoActivityResult: './CombineVideoActivityResult.jsx',
            MergeVideoActivityResult: './MergeVideoActivityResult.jsx',
        },
        target: 'web'
    })
];
