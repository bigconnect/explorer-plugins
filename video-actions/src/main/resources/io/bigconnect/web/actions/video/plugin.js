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
    'util/component/attacher',
    'public/v1/api',
    'px/extensions/growl'
], function (registry, i18n, F, Attacher, bcApi) {
    'use strict';

    registry.registerExtension('org.bigconnect.activity', {
        type: 'combine-video',
        kind: 'longRunningProcess',
        allowCancel: true, // long running processes don't support cancelling BUT ! We leave this for the cancel button to appear. This button will remove the process from UI (needed for crashes)
        titleRenderer: function(el, process) {
            el.textContent = process.title;
        },
        finishedComponentPath: 'io/bigconnect/web/actions/video/dist/CombineVideoActivityResult'
    });

    registry.registerExtension('org.bigconnect.activity', {
        type: 'merge-videos',
        kind: 'longRunningProcess',
        allowCancel: true, // long running processes don't support cancelling BUT ! We leave this for the cancel button to appear. This button will remove the process from UI (needed for crashes)
        titleRenderer: function(el, process) {
            el.textContent = process.title;
        },
        finishedComponentPath: 'io/bigconnect/web/actions/video/dist/MergeVideoActivityResult'
    });

    registry.registerExtension('org.bigconnect.detail.toolbar', {
        title: i18n('detail.toolbar.video'),
        event: 'videoMenu',
        submenu: [
            {
                title: i18n('detail.toolbar.cutVideo'),
                subtitle: i18n('detail.toolbar.cutVideo.cutVideoSubtitle'),
                cls: 'requires-EDIT',
                event: 'cutVideo'
            }
        ],
        canHandle: (objects) => {
            const singleVertex = objects.vertices.length === 1;
            if (!singleVertex)
                return false;

            const concept = F.vertex.concept(objects.vertices[0]);
            return ONTOLOGY_CONSTANTS.CONCEPT_TYPE_VIDEO === concept.id;
        }
    });

    registry.registerExtension('org.bigconnect.detail.toolbar', {
        title: i18n('detail.toolbar.video'),
        event: 'videoMenu',
        submenu: [
            {
                title: i18n('detail.toolbar.mergeVideo'),
                subtitle: i18n('detail.toolbar.mergeVideo.mergeVideoSubtitle'),
                cls: 'requires-EDIT',
                event: 'mergeVideo'
            }
        ],
        canHandle: (objects) => {
            const manyVertices = objects.vertices.length > 1;
            if (!manyVertices)
                return false;

            const concepts = _.map(objects.vertices, (v) => F.vertex.concept(objects.vertices[0]));
            const videos = _.filter(concepts, c => ONTOLOGY_CONSTANTS.CONCEPT_TYPE_VIDEO === c.id);
            return concepts.length === videos.length;
        }
    });

    bcApi.connect().then((api) => {
        $(document).on('cutVideo', (e, data) => {
            let $container = $('.cut-video-form');

            if ($container.length === 0) {
                $container = $('<div class="cut-video-form"></div>').insertBefore(
                    $('.org-bigconnect-properties')
                );
            }

            const node = $('<div class="underneath"></div>').appendTo($container);
            require(['io/bigconnect/web/actions/video/dist/CutVideoForm'], (CutVideoForm) => {
                Attacher()
                    .node(node)
                    .component(CutVideoForm)
                    .params({
                        node,
                        data,
                    })
                    .attach();
            })
        });

        $(document).on('mergeVideo', (e, data) => {
            let $container = $('.merge-video-form');

            if ($container.length === 0) {
                $container = $('<div class="merge-video-form"></div>').insertBefore(
                    $('.multiple')
                );
            }

            const node = $('<div class="underneath"></div>').appendTo($container);
            require(['io/bigconnect/web/actions/video/dist/MergeVideoForm'], (MergeVideoForm) => {
                Attacher()
                    .node(node)
                    .component(MergeVideoForm)
                    .params({
                        node,
                        data,
                    })
                    .attach();
            })
        });
    });
});
