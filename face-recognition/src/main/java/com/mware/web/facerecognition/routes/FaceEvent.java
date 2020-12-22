/*
 * This file is part of the BigConnect project.
 *
 * Copyright (c) 2013-2020 MWARE SOLUTIONS SRL
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * MWARE SOLUTIONS SRL, MWARE SOLUTIONS SRL DISCLAIMS THE WARRANTY OF
 * NON INFRINGEMENT OF THIRD PARTY RIGHTS

 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the BigConnect software without
 * disclosing the source code of your own applications.
 *
 * These activities include: offering paid services to customers as an ASP,
 * embedding the product in a web application, shipping BigConnect with a
 * closed source product.
 */
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
