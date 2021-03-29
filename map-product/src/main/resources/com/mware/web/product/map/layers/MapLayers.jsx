/*
 * This file is part of the BigConnect project.
 *
 * Copyright (c) 2013-2020 MWARE SOLUTIONS SRL
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * MWARE SOLUTIONS SRL, MWARE SOLUTIONS SRL DISCLAIMS THE WARRANTY OF
 * NON INFRINGEMENT OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the BigConnect software without
 * disclosing the source code of your own applications.
 *
 * These activities include: offering paid services to customers as an ASP,
 * embedding the product in a web application, shipping BigConnect with a
 * closed source product.
 */
define([
    'prop-types',
    'create-react-class',
    'react-sortable-hoc',
    './MapLayersList',
    '../util/layerHelpers'
], function(
    PropTypes,
    createReactClass,
    { arrayMove },
    MapLayersList,
    layerHelpers) {

    const MapLayers = createReactClass({

        propTypes: {
            product: PropTypes.shape({
                extendedData: PropTypes.shape({
                    vertices: PropTypes.object,
                    edges: PropTypes.object }
                ).isRequired
            }).isRequired,
            map: PropTypes.object.isRequired,
            baseLayer: PropTypes.object,
            layersConfig: PropTypes.object,
            layerOrder: PropTypes.array.isRequired,
            layerIds: PropTypes.array.isRequired,
            layers: PropTypes.array.isRequired,
            editable: PropTypes.bool,
            setLayerOrder: PropTypes.func.isRequired,
            updateLayerConfig: PropTypes.func.isRequired
        },

        getInitialState() {
            return { futureIndex: null }
        },

        componentWillReceiveProps(nextProps) {
            if (nextProps.layerOrder !== this.props.layerOrder && this.state.futureIndex) {
                this.setState({ futureIndex: null });
            }
        },

        render() {
            const { futureIndex } = this.state;
            const { baseLayer, layers, layersConfig, editable, ol, map } = this.props;
            let layerList = futureIndex ? arrayMove(layers, futureIndex[0], futureIndex[1]) : layers;
            layerList = layerList.map(layer => ({
                config: layersConfig[layer.get('id')],
                layer
            }));

            return (
                <div className="map-layers">
                    <MapLayersList
                        baseLayer={{ config: layersConfig['base'], layer: baseLayer }}
                        layers={layerList}
                        editable={editable}
                        onToggleLayer={this.onToggleLayer}
                        onSelectLayer={this.onSelectLayer}
                        onOrderLayer={this.onOrderLayer}
                    />
                </div>
            );
        },

        onOrderLayer(oldSubsetIndex, newSubsetIndex) {
            const { product, layerIds, layerOrder, setLayerOrder } = this.props;
            const orderedSubset = arrayMove(layerIds, oldSubsetIndex, newSubsetIndex);

            const oldIndex = layerOrder.indexOf(orderedSubset[newSubsetIndex]);
            let newIndex;
            if (newSubsetIndex === orderedSubset.length - 1) {
                const afterId = orderedSubset[newSubsetIndex - 1];
                newIndex = layerOrder.indexOf(afterId);
            } else {
                const beforeId = orderedSubset[newSubsetIndex + 1];
                const displacementOffset = oldSubsetIndex > newSubsetIndex ? 0 : 1;
                newIndex = Math.max((layerOrder.indexOf(beforeId) - displacementOffset), 0);
            }

            //optimistically update item order in local component state so it doesn't jump
            this.setState({ futureIndex: [ oldSubsetIndex, newSubsetIndex ]});

            setLayerOrder(arrayMove(layerOrder, oldIndex, newIndex));
        },

        onToggleLayer(layer) {
            const { product, layersConfig, updateLayerConfig } = this.props;

            const layerId = layer.get('id');
            const config = { ...(layersConfig[layerId] || {}), visible: !layer.getVisible() };

            layerHelpers.setLayerConfig(config, layer);
            updateLayerConfig(config, layerId);
        }

    });

    return MapLayers;
});
