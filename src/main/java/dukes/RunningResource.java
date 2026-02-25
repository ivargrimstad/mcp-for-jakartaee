package dukes;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("status")
public class RunningResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "MCP Server for Jakarta EE is running";
    }
}
