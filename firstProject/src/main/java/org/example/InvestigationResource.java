package org.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api")
public class InvestigationResource {

    @Inject ClusterService      clusterService;
    @Inject InvestigationService investigationService;
    @Inject NodeInspector       nodeInspector;
    @Inject DeploymentInspector deploymentInspector;

    /** GET /api/clusters → reachable kubectl contexts on this machine. */
    @GET
    @Path("/clusters")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listClusters() {
        try {
            List<String> contexts = clusterService.listContexts();
            return Response.ok(contexts).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Could not list clusters. Is kubectl installed and on PATH? " + e.getMessage())
                    .build();
        }
    }

    /**
     * POST /api/investigate/{context}
     * Runs pod + log + event + node + deployment investigation and returns
     * the full InvestigationResult as JSON.
     */
    @POST
    @Path("/investigate/{context}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response investigate(@PathParam("context") String context) {
        try {
            InvestigationResult result = investigationService.runFullInvestigation(context);
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Investigation failed: " + e.getMessage())
                    .build();
        }
    }

    /** GET /api/nodes/{context} → node health snapshot (standalone, for refresh). */
    @GET
    @Path("/nodes/{context}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listNodes(@PathParam("context") String context) {
        try {
            List<NodeSummary> nodes = nodeInspector.getNodes(context);
            return Response.ok(nodes).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Could not list nodes: " + e.getMessage())
                    .build();
        }
    }

    /** GET /api/deployments/{context} → deployment rollout status snapshot. */
    @GET
    @Path("/deployments/{context}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listDeployments(@PathParam("context") String context) {
        try {
            List<DeploymentSummary> deps = deploymentInspector.getDeployments(context);
            return Response.ok(deps).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Could not list deployments: " + e.getMessage())
                    .build();
        }
    }
}
