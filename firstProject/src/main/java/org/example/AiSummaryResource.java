package org.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/ai-summary")
public class AiSummaryResource {

    @Inject
    AiAnalysisService aiAnalysisService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response summarize(InvestigationResult investigation) {
        try {
            AiSummaryResult result = aiAnalysisService.analyze(investigation);
            return Response.ok(result).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.serverError().entity("AI summary failed: " + e.getMessage()).build();
        }
    }
}
