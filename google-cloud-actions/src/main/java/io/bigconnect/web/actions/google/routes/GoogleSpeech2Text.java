package io.bigconnect.web.actions.google.routes;

import com.mware.core.ingest.dataworker.ElementOrPropertyStatus;
import com.mware.core.model.workQueue.Priority;
import com.mware.core.model.workQueue.WorkQueueRepository;
import com.mware.core.user.User;
import com.mware.core.util.BcLogger;
import com.mware.core.util.BcLoggerFactory;
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
import io.bigconnect.dw.google.speech.Speech2TextSchemaContribution;

import javax.inject.Inject;

public class GoogleSpeech2Text implements ParameterizedHandler {
    private final static BcLogger LOGGER = BcLoggerFactory.getLogger(GoogleSpeech2Text.class);
    private final Graph graph;
    private final WorkQueueRepository workQueueRepository;

    @Inject
    public GoogleSpeech2Text(Graph graph, WorkQueueRepository workQueueRepository) {
        this.graph = graph;
        this.workQueueRepository = workQueueRepository;
    }

    @Handle
    public ClientApiSuccess handle(
            @Required(name = "id") String vertexId,
            User user,
            @ActiveWorkspaceId String workspaceId,
            Authorizations authorizations
    ) {
        Vertex e = graph.getVertex(vertexId, authorizations);

        boolean alreadyInProgress = Speech2TextSchemaContribution.GOOGLE_S2T_PROGRESS_PROPERTY.getPropertyValue(e, false);
        if (!alreadyInProgress) {
            Speech2TextSchemaContribution.GOOGLE_S2T_PROPERTY.setProperty(e, Boolean.TRUE, Visibility.EMPTY, authorizations);
            graph.flush();

            workQueueRepository.pushOnDwQueue(
                    e,
                    null, Speech2TextSchemaContribution.GOOGLE_S2T_PROPERTY.getPropertyName(),
                    workspaceId, null, Priority.HIGH, ElementOrPropertyStatus.UPDATE, null
            );
        } else {
            LOGGER.warn("Speech2Text already in progress for element: "+vertexId);
        }

        return BcResponse.SUCCESS;
    }
}
