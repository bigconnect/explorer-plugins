package io.bigconnect.web.actions.text;

import com.mware.web.BcCsrfHandler;
import com.mware.web.WebApp;
import com.mware.web.WebAppPlugin;
import com.mware.web.framework.Handler;
import com.mware.web.privilegeFilters.EditPrivilegeFilter;
import io.bigconnect.web.actions.text.routes.GoogleTranslate;
import io.bigconnect.web.actions.text.routes.Summary;

import javax.servlet.ServletContext;

public class TextActionsWebAppPlugin implements WebAppPlugin {

    @Override
    public void init(WebApp app, ServletContext servletContext, Handler authenticationHandler) {
        app.post("/text/translate", authenticationHandler.getClass(), BcCsrfHandler.class, EditPrivilegeFilter.class, GoogleTranslate.class);
        app.post("/text/summary", authenticationHandler.getClass(), BcCsrfHandler.class, EditPrivilegeFilter.class, Summary.class);

        app.registerJavaScript("/io/bigconnect/web/actions/text/plugin.js");
        app.registerResourceBundle("/io/bigconnect/web/actions/text/messages.properties");
        app.registerWebWorkerJavaScript("/io/bigconnect/web/actions/text/worker/service.js");
    }
}
