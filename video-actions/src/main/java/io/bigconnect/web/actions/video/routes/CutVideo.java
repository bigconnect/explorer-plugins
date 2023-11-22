package io.bigconnect.web.actions.video.routes;

import com.google.inject.Inject;
import com.mware.core.model.longRunningProcess.LongRunningProcessRepository;
import com.mware.core.user.User;
import com.mware.ge.Authorizations;
import com.mware.ge.Graph;
import com.mware.web.framework.ParameterizedHandler;
import com.mware.web.framework.annotations.Handle;
import com.mware.web.framework.annotations.Required;
import com.mware.web.model.ClientApiLongRunningProcessSubmitResponse;
import com.mware.web.parameterProviders.ActiveWorkspaceId;
import io.bigconnect.web.actions.video.CutVideoQueueItem;
import io.bigconnect.web.actions.video.VideoSchemaContribution;

public class CutVideo implements ParameterizedHandler {
    private LongRunningProcessRepository longRunningProcessRepository;
    private Graph graph;

    @Inject
    public CutVideo(
            LongRunningProcessRepository longRunningProcessRepository,
            Graph graph
    ) {
        this.longRunningProcessRepository = longRunningProcessRepository;
        this.graph = graph;
    }

    @Handle
    public ClientApiLongRunningProcessSubmitResponse handle(
            @Required(name = "vertexId") String vertexId,
            @Required(name = "startTime") String startTime,
            @Required(name = "endTime") String endTime,
            @ActiveWorkspaceId String workspaceId,
            User user,
            Authorizations authorizations
    ) {
        CutVideoQueueItem queueItem = new CutVideoQueueItem(
                vertexId,
                startTime,
                endTime,
                VideoSchemaContribution.EDGE_LABEL_HAS_VIDEO,
                user.getUserId(),
                workspaceId,
                authorizations.getAuthorizations()
        );
        String id = this.longRunningProcessRepository.enqueue(queueItem, user, authorizations);
        return new ClientApiLongRunningProcessSubmitResponse(id);
    }
}
