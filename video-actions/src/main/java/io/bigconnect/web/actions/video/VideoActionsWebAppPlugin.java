package io.bigconnect.web.actions.video;

import com.google.inject.Singleton;
import com.mware.core.model.Description;
import com.mware.core.model.Name;
import com.mware.web.BcCsrfHandler;
import com.mware.web.WebApp;
import com.mware.web.WebAppPlugin;
import com.mware.web.framework.Handler;
import com.mware.web.privilegeFilters.EditPrivilegeFilter;
import io.bigconnect.web.actions.video.routes.CutVideo;
import io.bigconnect.web.actions.video.routes.MergeVideos;

import javax.servlet.ServletContext;

@Name("Video UI Actions")
@Description("UI actions for Video cut and split")
@Singleton
public class VideoActionsWebAppPlugin implements WebAppPlugin {
    @Override
    public void init(WebApp app, ServletContext servletContext, Handler authenticationHandler) {
        app.post("/video/cut", authenticationHandler.getClass(), BcCsrfHandler.class, EditPrivilegeFilter.class, CutVideo.class);
        app.post("/video/merge", authenticationHandler.getClass(), BcCsrfHandler.class, EditPrivilegeFilter.class, MergeVideos.class);

        app.registerJavaScript("/io/bigconnect/web/actions/video/plugin.js");

        app.registerCompiledJavaScript("/io/bigconnect/web/actions/video/dist/CutVideoForm.js");
        app.registerCompiledJavaScript("/io/bigconnect/web/actions/video/dist/MergeVideoForm.js");
        app.registerCompiledJavaScript("/io/bigconnect/web/actions/video/dist/CombineVideoActivityResult.js");
        app.registerCompiledJavaScript("/io/bigconnect/web/actions/video/dist/MergeVideoActivityResult.js");

        app.registerWebWorkerJavaScript("/io/bigconnect/web/actions/video/worker/service.js");

        app.registerResourceBundle("/io/bigconnect/web/actions/video/messages.properties");
        app.registerLess("/io/bigconnect/web/actions/video/style.less");
    }
}
