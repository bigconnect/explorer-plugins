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
package com.mware.web.product.map;

import com.mware.core.model.Description;
import com.mware.core.model.Name;
import com.mware.web.BcCsrfHandler;
import com.mware.web.WebApp;
import com.mware.web.WebAppPlugin;
import com.mware.web.framework.Handler;
import com.mware.web.product.map.routes.RemoveVertices;
import com.mware.web.product.map.routes.UpdateVertices;

import javax.inject.Inject;
import javax.servlet.ServletContext;

@Name("Product: Map")
@Description("Map visualization for entities containing geolocation data")
public class MapWebAppPlugin implements WebAppPlugin {
    private final MapWorkProductService workProductService;

    @Inject
    public MapWebAppPlugin(MapWorkProductService workProductService) {
        this.workProductService = workProductService;
    }

    @Override
    public void init(WebApp app, ServletContext servletContext, Handler authenticationHandler) {
        Class<? extends Handler> authenticationHandlerClass = authenticationHandler.getClass();
        Class<? extends Handler> csrfHandlerClass = BcCsrfHandler.class;

        app.post("/product/map/vertices/remove", authenticationHandlerClass, csrfHandlerClass, RemoveVertices.class);
        app.post("/product/map/vertices/update", authenticationHandlerClass, csrfHandlerClass, UpdateVertices.class);

        app.registerJavaScript("/com/mware/web/product/map/plugin.js");
        app.registerJavaScript("/com/mware/web/product/map/detail/pluginGeoShapeDetail.js", true);
        //app.registerJavaScript("/com/mware/web/product/map/layerSwitcher.js");
        //app.registerJavaScript("/com/mware/web/product/map/controlBar.js");

        app.registerCompiledJavaScript("/com/mware/web/product/map/dist/geoShapePreview.js");
        app.registerCompiledJavaScript("/com/mware/web/product/map/dist/MapLayersContainer.js");
        app.registerCompiledJavaScript("/com/mware/web/product/map/dist/Map.js");
        app.registerCompiledJavaScript("/com/mware/web/product/map/dist/actions-impl.js");

        app.registerCompiledWebWorkerJavaScript("/com/mware/web/product/map/dist/plugin-worker.js");

        app.registerResourceBundle("/com/mware/web/product/map/messages.properties");
        
        app.registerLess("/com/mware/web/product/map/style.less");
        app.registerLess("/com/mware/web/product/map/layers/mapLayers.less");
        app.registerLess("/com/mware/web/product/map/detail/geoShapeDetail.less");
    }

    public MapWorkProductService getWorkProductService() {
        return workProductService;
    }
}
