package io.bigconnect.web.actions.image;

import com.mware.web.BcCsrfHandler;
import com.mware.web.WebApp;
import com.mware.web.WebAppPlugin;
import com.mware.web.framework.Handler;
import com.mware.web.privilegeFilters.EditPrivilegeFilter;
import io.bigconnect.web.actions.image.routes.DetectObjects;

import javax.servlet.ServletContext;

public class ImageActionsWebAppPlugin implements WebAppPlugin {
    @Override
    public void init(WebApp app, ServletContext servletContext, Handler authenticationHandler) {
        app.post("/image/objects", authenticationHandler.getClass(), BcCsrfHandler.class, EditPrivilegeFilter.class, DetectObjects.class);

        app.registerJavaScript("/io/bigconnect/web/actions/image/plugin.js");
        app.registerResourceBundle("/io/bigconnect/web/actions/image/messages.properties");
        app.registerWebWorkerJavaScript("/io/bigconnect/web/actions/image/worker/service.js");
    }
}
