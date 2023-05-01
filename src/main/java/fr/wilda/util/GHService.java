package fr.wilda.util;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/repos")
@RegisterRestClient
public interface GHService {

    @GET
    @Path("/{owner}/{repo}/releases/latest")
    GitHubRelease getByOrgaAndRepo(@PathParam(value = "owner") String owner, @PathParam(value = "repo") String repo);
}
