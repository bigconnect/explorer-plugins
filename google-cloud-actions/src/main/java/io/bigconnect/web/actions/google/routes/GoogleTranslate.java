package io.bigconnect.web.actions.google.routes;

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
import io.bigconnect.dw.google.translate.GoogleTranslateSchemaContribution;

import javax.inject.Inject;

public class GoogleTranslate implements ParameterizedHandler {
    private final Graph graph;
    private final WorkQueueRepository workQueueRepository;

    @Inject
    public GoogleTranslate(Graph graph, WorkQueueRepository workQueueRepository) {
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
        GoogleTranslateSchemaContribution.GOOGLE_TRANSLATE_PROPERTY.setProperty(e, Boolean.TRUE, Visibility.EMPTY, authorizations);
        graph.flush();

        workQueueRepository.pushGraphPropertyQueue(
                e,
                null, GoogleTranslateSchemaContribution.GOOGLE_TRANSLATE_PROPERTY.getPropertyName(),
                workspaceId, null, Priority.HIGH, ElementOrPropertyStatus.UPDATE, null
        );

        return BcResponse.SUCCESS;
    }
}
