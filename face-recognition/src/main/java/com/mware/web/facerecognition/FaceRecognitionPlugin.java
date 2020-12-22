package com.mware.web.facerecognition;

import com.mware.core.bootstrap.InjectHelper;
import com.mware.core.model.Description;
import com.mware.core.model.Name;
import com.mware.web.WebApp;
import com.mware.web.WebAppPlugin;
import com.mware.web.facerecognition.routes.FaceEvent;
import com.mware.web.framework.Handler;

import javax.servlet.ServletContext;

@Name("Face Recognition")
@Description("Registers routes for face recognition process")
public class FaceRecognitionPlugin implements WebAppPlugin {
    /**
     * @param app
     * @param servletContext
     * @param authenticationHandler
     */
    @Override
    public void init(WebApp app, ServletContext servletContext, Handler authenticationHandler) {
        app.post("/face-event", InjectHelper.getInstance(FaceEvent.class));
    }
}
