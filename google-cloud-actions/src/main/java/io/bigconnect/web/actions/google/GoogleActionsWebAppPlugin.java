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
package io.bigconnect.web.actions.google;

import com.google.inject.Singleton;
import com.mware.core.model.Description;
import com.mware.core.model.Name;
import com.mware.web.BcCsrfHandler;
import com.mware.web.WebApp;
import com.mware.web.WebAppPlugin;
import com.mware.web.framework.Handler;
import com.mware.web.privilegeFilters.EditPrivilegeFilter;
import io.bigconnect.web.actions.google.routes.GoogleSpeech2Text;
import io.bigconnect.web.actions.google.routes.GoogleTranslate;

import javax.servlet.ServletContext;

@Name("Google Cloud UI Actions")
@Description("UI actions for Google Speech2Text and Google Translate")
@Singleton
public class GoogleActionsWebAppPlugin implements WebAppPlugin {
    @Override
    public void init(WebApp app, ServletContext servletContext, Handler authenticationHandler) {
        app.post("/google/translate", authenticationHandler.getClass(), BcCsrfHandler.class, EditPrivilegeFilter.class, GoogleTranslate.class);
        app.post("/google/s2t", authenticationHandler.getClass(), BcCsrfHandler.class, EditPrivilegeFilter.class, GoogleSpeech2Text.class);

        app.registerJavaScript("/io/bigconnect/web/actions/google/plugin.js");
        app.registerResourceBundle("/io/bigconnect/web/actions/google/messages.properties");
        app.registerWebWorkerJavaScript("/io/bigconnect/web/actions/google/service.js");
    }
}
