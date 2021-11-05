package io.bigconnect.web.actions.google.routes;

import com.mware.bigconnect.ffmpeg.AudioFormat;
import com.mware.core.exception.BcException;
import com.mware.core.model.longRunningProcess.LongRunningProcessRepository;
import com.mware.core.model.notification.UserNotificationRepository;
import com.mware.core.model.properties.BcSchema;
import com.mware.core.model.properties.MediaBcSchema;
import com.mware.core.model.properties.RawObjectSchema;
import com.mware.core.user.User;
import com.mware.core.util.BcLogger;
import com.mware.core.util.BcLoggerFactory;
import com.mware.ge.Authorizations;
import com.mware.ge.FetchHints;
import com.mware.ge.Graph;
import com.mware.ge.Vertex;
import com.mware.ge.util.ArrayUtils;
import com.mware.web.framework.ParameterizedHandler;
import com.mware.web.framework.annotations.Handle;
import com.mware.web.framework.annotations.Required;
import com.mware.web.model.ClientApiLongRunningProcessSubmitResponse;
import com.mware.web.parameterProviders.ActiveWorkspaceId;
import io.bigconnect.dw.google.speech.Speech2TextQueueItem;
import io.bigconnect.dw.google.speech.Speech2TextSchemaContribution;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

public class GoogleSpeech2Text implements ParameterizedHandler {
    private final static BcLogger LOGGER = BcLoggerFactory.getLogger(GoogleSpeech2Text.class);
    private static final AudioFormat[] ALLOWED_AUDIO_FORMATS = new AudioFormat[]{AudioFormat.MP4};

    private final Graph graph;
    private final LongRunningProcessRepository longRunningProcessRepository;
    private final UserNotificationRepository userNotificationRepository;

    @Inject
    public GoogleSpeech2Text(
            Graph graph,
            LongRunningProcessRepository longRunningProcessRepository,
            UserNotificationRepository userNotificationRepository
    ) {
        this.graph = graph;
        this.longRunningProcessRepository = longRunningProcessRepository;
        this.userNotificationRepository = userNotificationRepository;
    }

    @Handle
    public ClientApiLongRunningProcessSubmitResponse handle(
            @Required(name = "id") String vertexId,
            User user,
            @ActiveWorkspaceId String workspaceId,
            Authorizations authorizations
    ) {
        Vertex element = graph.getVertex(vertexId, authorizations);

        boolean alreadyInProgress = Speech2TextSchemaContribution.GOOGLE_S2T_PROGRESS_PROPERTY.getPropertyValue(element, false);
        if (!alreadyInProgress) {
            // check language and videoformat
            final String language = RawObjectSchema.RAW_LANGUAGE.getFirstPropertyValue(element);
            final String audioFormat = MediaBcSchema.MEDIA_AUDIO_FORMAT.getPropertyValue(element);
            boolean hasAudio = !StringUtils.isEmpty(audioFormat) && ArrayUtils.contains(ALLOWED_AUDIO_FORMATS, AudioFormat.valueOf(audioFormat));

            if (StringUtils.isEmpty(language)) {
                throw new BcException("No language set for element: " + BcSchema.TITLE.getFirstPropertyValue(element));
            } else if (!hasAudio) {
                throw new BcException("No audio track found for element: " + BcSchema.TITLE.getFirstPropertyValue(element));
            } else {
                Vertex v = graph.getVertex(vertexId, FetchHints.NONE, authorizations);
                if (v != null) {
                    Speech2TextQueueItem queueItem = new Speech2TextQueueItem(user.getUserId(), workspaceId, authorizations.getAuthorizations(), vertexId);
                    String processId = longRunningProcessRepository.enqueue(queueItem, user, authorizations);
                    return new ClientApiLongRunningProcessSubmitResponse(processId);
                } else {
                    throw new BcException("Element not found: " + vertexId);
                }
            }
        } else {
            throw new BcException("Speech2Text already in progress for element: " + BcSchema.TITLE.getFirstPropertyValue(element));
        }
    }
}
