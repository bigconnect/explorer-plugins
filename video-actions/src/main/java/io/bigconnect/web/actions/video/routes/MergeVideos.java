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
import io.bigconnect.web.actions.video.MergeVideosQueueItem;

import java.util.Arrays;

public class MergeVideos implements ParameterizedHandler {
    private final LongRunningProcessRepository longRunningProcessRepository;
    private final Graph graph;

    @Inject
    public MergeVideos(
            LongRunningProcessRepository longRunningProcessRepository,
            Graph graph
    ) {
        this.longRunningProcessRepository = longRunningProcessRepository;
        this.graph = graph;
    }

    @Handle
    public ClientApiLongRunningProcessSubmitResponse handle(
            @Required(name = "title") String title,
            @Required(name = "vertexIds[]") String[] vertexIds,
            @ActiveWorkspaceId String workspaceId,
            User user,
            Authorizations authorizations
    ) {
        MergeVideosQueueItem queueItem = new MergeVideosQueueItem(
                title,
                Arrays.asList(vertexIds),
                user.getUserId(),
                workspaceId,
                authorizations.getAuthorizations()
        );
        String id = this.longRunningProcessRepository.enqueue(queueItem, user, authorizations);
        return new ClientApiLongRunningProcessSubmitResponse(id);
    }
}