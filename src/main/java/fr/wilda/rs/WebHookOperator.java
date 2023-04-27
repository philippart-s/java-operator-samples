package fr.wilda.rs;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.wilda.ReleaseDetectorReconciler;

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