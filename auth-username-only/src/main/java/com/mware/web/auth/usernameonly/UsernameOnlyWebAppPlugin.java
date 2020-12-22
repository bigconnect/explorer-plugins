package com.mware.web.auth.usernameonly;

import com.mware.web.auth.usernameonly.routes.Login;
import com.mware.web.framework.Handler;
import com.mware.core.bootstrap.InjectHelper;
import com.mware.core.model.Description;
import com.mware.core.model.Name;
import com.mware.web.AuthenticationHandler;
import com.mware.web.WebApp;
import com.mware.web.WebAppPlugin;

import javax.servlet.ServletContext;

@Name("Username Only Authentication")
@Description("Allows authenticating using just a username")
public class UsernameOnlyWebAppPlugin implements WebAppPlugin {
    @Override
    public void init(WebApp app, ServletContext servletContext, Handler authenticationHandler) {

        app.registerBeforeAuthenticationJavaScript("/com/mware/web/auth/usernameonly/plugin.js");
        app.registerJavaScriptTemplate("/com/mware/web/auth/usernameonly/templates/login.hbs");
        app.registerJavaScript("/com/mware/web/auth/usernameonly/authentication.js", false);

        app.registerLess("/com/mware/web/auth/usernameonly/less/login.less");

        app.post(AuthenticationHandler.LOGIN_PATH, InjectHelper.getInstance(Login.class));
    }
}
