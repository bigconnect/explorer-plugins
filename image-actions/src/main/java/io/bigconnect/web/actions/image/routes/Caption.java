package io.bigconnect.web.actions.image.routes;

import com.mware.core.ingest.dataworker.ElementOrPropertyStatus;
import com.mware.core.model.workQueue.Priority;
import com.mware.core.model.workQueue.WorkQueueRepository;
import com.mware.core.user.User;
import com.mware.ge.Authorizations;
import com.mware.ge.Graph;
import com.mware.ge.Vertex;
import com.mware.ge.Visibility;
import com.mware.web.BcResponse;
import com.mware.web.framework.ParameterizedHandler;
import com.mware.web.framework.annotations.Handle;
import com.mware.web.framework.annotations.Required;
import com.mware.web.model.ClientApiSuccess;
import com.mware.web.parameterProviders.ActiveWorkspaceId;
import io.bigconnect.dw.image.ocr.ImageCaptionSchemaContribution;
import io.bigconnect.dw.image.ocr.ImageCaptioningResponse;
import io.bigconnect.dw.image.ocr.ImageOcrSchemaContribution;

import javax.inject.Inject;

public class Caption implements ParameterizedHandler {
    private final Graph graph;
    private final WorkQueueRepository workQueueRepository;

    @Inject
    public Caption(Graph graph, WorkQueueRepository workQueueRepository) {
        this.graph = graph;
        this.workQueueRepository = workQueueRepository;
    }

    @Handle
    public ClientApiSuccess handle(
            @Required(name = "id") String vertexId,
            @ActiveWorkspaceId String workspaceId,
            User user,
            Authorizations authorizations
    ) {
        Vertex e = graph.getVertex(vertexId, authorizations);
        ImageCaptionSchemaContribution.PERFORM_CAPTION.setProperty(e, true, Visibility.EMPTY, authorizations);
        graph.flush();

        workQueueRepository.pushOnDwQueue(
                e,
                null, ImageCaptionSchemaContribution.PERFORM_CAPTION.getPropertyName(),
                workspaceId, null, Priority.HIGH, ElementOrPropertyStatus.UPDATE,
                null
        );

        return BcResponse.SUCCESS;
    }
}