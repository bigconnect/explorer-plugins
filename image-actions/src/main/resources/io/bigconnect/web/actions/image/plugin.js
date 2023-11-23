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

    registry.registerExtension('org.bigconnect.detail.toolbar', {
        title: i18n('image.menu'),
        event: 'image',
        submenu: [
            {
                title: i18n('detail.toolbar.detect-objects.title'),
                subtitle: i18n('detail.toolbar.detect-objects.subtitle'),
                cls: 'requires-EDIT',
                event: 'detectObjects'
            },
            {
                title: i18n('detail.toolbar.ocr.title'),
                subtitle: i18n('detail.toolbar.ocr.subtitle'),
                cls: 'requires-EDIT',
                event: 'ocr'
            },
            {
                title: i18n('detail.toolbar.caption.title'),
                subtitle: i18n('detail.toolbar.caption.subtitle'),
                cls: 'requires-EDIT',
                event: 'caption'
            }
        ],
        canHandle: (objects) => {
            if (objects.vertices.length === 1) {
                const v = objects.vertices[0];
                return F.vertex.concept(v).displayType === 'image';
            }

            return false;
        }
    });

    bcApi.connect().then((api) => {
        $(document).on('detectObjects', (e, data) => {
            const vertex = data.vertices[0];
            api.dataRequest('image', 'objects', vertex.id)
                .then(() => {
                    $.growl.notice({
                        message: 'Obiectul a fost trimits catre serviciul de detectie. Folositi Refresh pentru a vedea progresul.',
                    });
                })
                .catch(e => {
                    console.log(e);
                    $.growl.error({ title: 'Eroare trimitere document la servicul de detectie' });
                });
        });

        $(document).on('ocr', (e, data) => {
            const vertex = data.vertices[0];
            api.dataRequest('image', 'ocr', vertex.id)
                .then(() => {
                    $.growl.notice({
                        message: 'Obiectul a fost trimits catre serviciul de detectie. Folositi Refresh pentru a vedea progresul.',
                    });
                })
                .catch(e => {
                    console.log(e);
                    $.growl.error({ title: 'Eroare trimitere document la servicul de detectie' });
                });
        });

        $(document).on('caption', (e, data) => {
            const vertex = data.vertices[0];
            api.dataRequest('image', 'caption', vertex.id)
                .then(() => {
                    $.growl.notice({
                        message: 'Obiectul a fost trimits catre serviciul de detectie. Folositi Refresh pentru a vedea progresul.',
                    });
                })
                .catch(e => {
                    console.log(e);
                    $.growl.error({ title: 'Eroare trimitere document la servicul de detectie' });
                });
        });

    });
});