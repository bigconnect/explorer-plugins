package io.bigconnect.web.actions.image;

import com.mware.web.BcCsrfHandler;
import com.mware.web.WebApp;
import com.mware.web.WebAppPlugin;
import com.mware.web.framework.Handler;
import com.mware.web.privilegeFilters.EditPrivilegeFilter;
import io.bigconnect.web.actions.image.routes.Caption;
import io.bigconnect.web.actions.image.routes.DetectFaces;
import io.bigconnect.web.actions.image.routes.DetectObjects;
import io.bigconnect.web.actions.image.routes.Ocr;

import javax.servlet.ServletContext;

public class ImageActionsWebAppPlugin implements WebAppPlugin {
    @Override
    public void init(WebApp app, ServletContext servletContext, Handler authenticationHandler) {
        app.post("/image/objects", authenticationHandler.getClass(), BcCsrfHandler.class, EditPrivilegeFilter.class, DetectObjects.class);
        app.post("/image/ocr", authenticationHandler.getClass(), BcCsrfHandler.class, EditPrivilegeFilter.class, Ocr.class);
        app.post("/image/caption", authenticationHandler.getClass(), BcCsrfHandler.class, EditPrivilegeFilter.class, Caption.class);
        app.post("/image/faces", authenticationHandler.getClass(), BcCsrfHandler.class, EditPrivilegeFilter.class, DetectFaces.class);

        app.registerJavaScript("/io/bigconnect/web/actions/image/plugin.js");
        app.registerResourceBundle("/io/bigconnect/web/actions/image/messages.properties");
        app.registerWebWorkerJavaScript("/io/bigconnect/web/actions/image/worker/service.js");
    }
}
