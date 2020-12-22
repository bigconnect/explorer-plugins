package com.mware.web.facerecognition.routes;

import com.google.inject.Inject;
import com.mware.core.model.clientapi.dto.ClientApiObject;
import com.mware.core.model.properties.BcSchema;
import com.mware.core.model.properties.FaceRecognitionSchema;
import com.mware.core.model.properties.RawObjectSchema;
import com.mware.core.model.role.AuthorizationRepository;
import com.mware.core.model.schema.SchemaConstants;
import com.mware.core.model.workQueue.WebQueueRepository;
import com.mware.core.user.SystemUser;
import com.mware.core.util.BcLogger;
import com.mware.core.util.BcLoggerFactory;
import com.mware.core.util.ClientApiConverter;
import com.mware.ge.*;
import com.mware.ge.values.storable.DateTimeValue;
import com.mware.web.facerecognition.FaceRecognitionPlugin;
import com.mware.web.framework.ParameterizedHandler;
import com.mware.web.framework.annotations.Handle;
import com.mware.web.framework.annotations.Required;
import com.mware.web.model.ClientApiSuccess;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;

import static com.mware.ge.values.storable.Values.stringValue;

public class FaceEvent implements ParameterizedHandler {
    private static final BcLogger LOGGER = BcLoggerFactory.getLogger(FaceEvent.class);

    private static final long THROTTLE_TIME = 10 * 1000; // 10s

    private final Graph graph;
    private final AuthorizationRepository authorizationRepository;
    private final WebQueueRepository webQueueRepository;

    /**
     * @param graph
     * @param authorizationRepository
     * @param webQueueRepository
     */
    @Inject
    public FaceEvent(Graph graph,
                     AuthorizationRepository authorizationRepository,
                     WebQueueRepository webQueueRepository) {
        this.graph = graph;
        this.authorizationRepository = authorizationRepository;
        this.webQueueRepository = webQueueRepository;
    }

    /**
     * @param vertexId
     * @param sourceName
     * @return
     */
    @Handle
    public ClientApiObject handle(@Required(name = "vertexId") String vertexId,
                                  @Required(name = "sourceName") String sourceName) {
        LOGGER.info("New face event received for vertex: " + vertexId);

        final Authorizations authorizations = authorizationRepository.getGraphAuthorizations(new SystemUser());
        Vertex vertex = this.graph.getVertex(vertexId, authorizations);
        if (vertex == null) {
            return new ClientApiSuccess();
        }
        // Throttle
        if (tooSoon(vertex)) {
            return new ClientApiSuccess();
        }

        final Metadata metadata = Metadata.create();
        final ZonedDateTime eventTime = ZonedDateTime.now();
        final long eventMillis = eventTime.toInstant().toEpochMilli();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        final String title = vertex.getProperty(BcSchema.TITLE.getPropertyName()).getValue()+ " spotted in " + sourceName + " at " + sdf.format(eventTime);

        Vertex event = this.graph.prepareVertex(vertex.getVisibility(), SchemaConstants.CONCEPT_TYPE_EVENT)
                .addPropertyValue("", BcSchema.TITLE.getPropertyName(),
                        stringValue(title), metadata, eventMillis, vertex.getVisibility())
                .addPropertyValue("", RawObjectSchema.SOURCE.getPropertyName(),
                        stringValue(sourceName), metadata, eventMillis, vertex.getVisibility())
                .addPropertyValue("", BcSchema.EVENT_TIME.getPropertyName(),
                        DateTimeValue.datetime(eventTime), metadata, eventMillis, vertex.getVisibility())
                .save(authorizations);

        this.graph.prepareEdge(event, vertex, SchemaConstants.EDGE_LABEL_FACE_EVENT, vertex.getVisibility())
                  .save(authorizations);

        FaceRecognitionSchema.LAST_FACE_EVENT.setProperty(vertex, eventTime, vertex.getVisibility(), authorizations);

        webQueueRepository.broadcastPropertyChange(event, "",  BcSchema.EVENT_TIME.getPropertyName(), null);
        webQueueRepository.broadcastPropertyChange(vertex, "", FaceRecognitionSchema.LAST_FACE_EVENT.getPropertyName(), null);
        graph.flush();

        return ClientApiConverter.toClientApi(event, null, authorizations);
    }

    private boolean tooSoon(Vertex vertex) {
        Property prop = vertex.getProperty(FaceRecognitionSchema.LAST_FACE_EVENT.getPropertyName());
        if (prop != null) {
            Object propValue = prop.getValue();
            if (propValue != null) {
                long lastEvent = ((Date)propValue).getTime();
                if ((new Date().getTime() - lastEvent) < THROTTLE_TIME) {
                    return true;
                }
            }
        }
        return false;
    }
}
