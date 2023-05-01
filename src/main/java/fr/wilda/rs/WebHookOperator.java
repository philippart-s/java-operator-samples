package fr.wilda.rs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.wilda.ReleaseDetectorReconciler;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/webhook")
public class WebHookOperator {
  private static final Logger log = LoggerFactory.getLogger(WebHookOperator.class);

  @Inject
  private ReleaseDetectorReconciler detectorReconciler;

  @POST
  @Path("/event")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response newTag(GHTagEvent tagEvent) {
    log.info("⚓️ Webhook called!!!!");

    if ("tag".equalsIgnoreCase(tagEvent.getRef_type())) {
      log.info("🎉 New tag: {}", tagEvent.getRef());
    }

    detectorReconciler.fireEvent(tagEvent.getRef());
    return Response.ok().build();
  }
}