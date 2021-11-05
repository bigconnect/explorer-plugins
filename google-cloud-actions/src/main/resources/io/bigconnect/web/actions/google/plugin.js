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
require([
    'configuration/plugins/registry',
    'util/messages',
    'util/vertex/formatters',
    'public/v1/api',
    'px/extensions/growl'
], function (registry, i18n, F, bcApi) {
    'use strict';

    registry.registerExtension('org.bigconnect.activity', {
        type: 's2t',
        kind: 'longRunningProcess',
        allowCancel: false,
        titleRenderer: function(el, process) {
            el.textContent = i18n('activity.tasks.type.s2t.title');
            require([
                'util/withDataRequest',
                'util/vertex/formatters'
            ], function(withDataRequest, F) {
                withDataRequest.dataRequest('vertex', 'store', {
                    workspaceId: process.workspaceId,
                    vertexIds: [ process.vertexId ]
                }).done(function(vertices) {
                    if (vertices.length === 1) {
                        el.textContent = F.string.truncate(F.vertex.title(vertices[0]), 16);
                    }
                });
            });
        },
        finishedComponentPath: 'io/bigconnect/web/actions/google/speech2TextResult'
    });

    registry.registerExtension('org.bigconnect.detail.toolbar', {
        title: i18n('google.menu'),
        event: 'google',
        submenu: [
            {
                title: i18n('google.translate.title'),
                subtitle: i18n('google.translate.subtitle'),
                cls: 'requires-EDIT',
                event: 'googleTranslate',
                canHandle: (objects) => {
                    const hasLanguage = F.vertex.props(objects.vertices[0], ONTOLOGY_CONSTANTS.PROP_RAW_LANGUAGE).length > 0;
                    if (!hasLanguage)
                        return false;

                    return F.vertex.props(objects.vertices[0], ONTOLOGY_CONSTANTS.PROP_TEXT).length > 0
                }
            },
            {
                title: i18n('google.s2t.title'),
                subtitle: i18n('google.s2t.subtitle'),
                cls: 'requires-EDIT',
                event: 'googleS2T',
                canHandle: (objects) => {
                    const hasLanguage = F.vertex.props(objects.vertices[0], ONTOLOGY_CONSTANTS.PROP_RAW_LANGUAGE).length > 0;
                    if (!hasLanguage)
                        return false;

                    return F.vertex.props(objects.vertices[0], "mediaVideoFormat").length > 0
                        || F.vertex.props(objects.vertices[0], "mediaAudioFormat").length > 0;
                }
            },
        ],
        canHandle: (objects) => {
            const singleVeretx = objects.vertices.length === 1;
            if (!singleVeretx)
                return false;

            const hasLanguage = F.vertex.props(objects.vertices[0], ONTOLOGY_CONSTANTS.PROP_RAW_LANGUAGE).length > 0;
            if (!hasLanguage)
                return false;

            return (F.vertex.props(objects.vertices[0], ONTOLOGY_CONSTANTS.PROP_TEXT).length > 0
                    || F.vertex.props(objects.vertices[0], "mediaVideoFormat").length > 0
                    || F.vertex.props(objects.vertices[0], "mediaAudioFormat").length > 0)
        }
    });

    bcApi.connect().then((api) => {
        $(document).on('googleTranslate', (e, data) => {
            const vertex = data.vertices[0];
            api.dataRequest('google', 'translate', vertex.id)
                .then(() => {
                    $.growl.notice({
                        message: 'The item was submitted to Google. You can check the progress in the activity pane.',
                    });
                })
                .catch(e => {
                    console.log(e);
                    $.growl.error({ title: 'Error queueing document for translation' });
                });
        });
        $(document).on('googleS2T', (e, data) => {
            const vertex = data.vertices[0];
            const inProgress = F.vertex.propRaw(vertex, "GS2TProgress");

            if (inProgress) {
                $.growl.warning({
                    message: `Operation in progress. Please refresh the item to see the latest updates.`,
                });
                return;
            }

            api.dataRequest('google', 's2t', vertex.id)
                .then((result) => {
                    $.growl.notice({
                        message: 'The item was submitted to Google. Please use the Refresh action to see the latest updates.',
                    });
                })
                .catch(e => {
                    console.log(e);
                    $.growl.error({ title: 'Error', message: e.json && e.json.error ? e.json.error : 'Unknown error' });
                });
        });
    });
});
