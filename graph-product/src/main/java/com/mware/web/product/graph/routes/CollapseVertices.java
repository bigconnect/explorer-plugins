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
 *
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
package com.mware.web.product.graph.routes;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mware.core.exception.BcAccessDeniedException;
import com.mware.core.exception.BcException;
import com.mware.core.model.clientapi.dto.ClientApiWorkspace;
import com.mware.core.model.graph.GraphRepository;
import com.mware.core.model.graph.GraphUpdateContext;
import com.mware.core.model.role.AuthorizationRepository;
import com.mware.core.model.workQueue.Priority;
import com.mware.core.model.workQueue.WebQueueRepository;
import com.mware.core.model.workspace.Workspace;
import com.mware.core.model.workspace.WorkspaceRepository;
import com.mware.core.user.User;
import com.mware.core.util.ClientApiConverter;
import com.mware.ge.Authorizations;
import com.mware.ge.Graph;
import com.mware.ge.Vertex;
import com.mware.product.WorkProductVertex;
import com.mware.web.framework.ParameterizedHandler;
import com.mware.web.framework.annotations.Handle;
import com.mware.web.framework.annotations.Optional;
import com.mware.web.framework.annotations.Required;
import com.mware.web.parameterProviders.ActiveWorkspaceId;
import com.mware.web.parameterProviders.SourceGuid;
import com.mware.web.product.graph.GraphWorkProductService;
import com.mware.web.product.graph.model.GraphUpdateProductEdgeOptions;
import com.mware.workspace.WorkspaceHelper;

@Singleton
public class CollapseVertices implements ParameterizedHandler {
    private final Graph graph;
    private final WorkspaceRepository workspaceRepository;
    private final WebQueueRepository webQueueRepository;
    private final AuthorizationRepository authorizationRepository;
    private final GraphRepository graphRepository;
    private final GraphWorkProductService graphWorkProductService;
    private final WorkspaceHelper workspaceHelper;

    @Inject
    public CollapseVertices(
            Graph graph,
            WorkspaceRepository workspaceRepository,
            WebQueueRepository webQueueRepository,
            AuthorizationRepository authorizationRepository,
            GraphRepository graphRepository,
            GraphWorkProductService graphWorkProductService,
            WorkspaceHelper workspaceHelper
    ) {
        this.graph = graph;
        this.workspaceRepository = workspaceRepository;
        this.webQueueRepository = webQueueRepository;
        this.authorizationRepository = authorizationRepository;
        this.graphRepository = graphRepository;
        this.graphWorkProductService = graphWorkProductService;
        this.workspaceHelper = workspaceHelper;
    }

    @Handle
    public WorkProductVertex handle(
            @Optional(name = "vertexId") String vertexId,
            @Required(name = "params") String paramsStr,
            @Required(name = "productId") String productId,
            @ActiveWorkspaceId String workspaceId,
            @SourceGuid String sourceGuid,
            User user
    ) throws Exception {
        GraphUpdateProductEdgeOptions params = ClientApiConverter.toClientApi(paramsStr, GraphUpdateProductEdgeOptions.class);
        WorkProductVertex results;

        if (!workspaceRepository.hasWritePermissions(workspaceId, user)) {
            throw new BcAccessDeniedException(
                    "user " + user.getUserId() + " does not have write access to workspace " + workspaceId,
                    user,
                    workspaceId
            );
        }

        Authorizations authorizations = authorizationRepository.getGraphAuthorizations(
                user,
                WorkspaceRepository.VISIBILITY_STRING,
                workspaceId
        );

        try (GraphUpdateContext ctx = graphRepository.beginGraphUpdate(Priority.HIGH, user, authorizations)) {
            Vertex productVertex = graph.getVertex(productId, authorizations);

            if (params.getId() == null) {
                params.setId(vertexId);
            }

            results = graphWorkProductService.addCompoundNode(ctx, productVertex, params, user, WorkspaceRepository.VISIBILITY.getVisibility(), authorizations);
        } catch (Exception e) {
            throw new BcException("Could not collapse vertices in product: " + productId, e);
        }

        workspaceHelper.broadcastWorkProductChange(workspaceId, productId, sourceGuid, user, authorizations);

        return results;
    }
}
