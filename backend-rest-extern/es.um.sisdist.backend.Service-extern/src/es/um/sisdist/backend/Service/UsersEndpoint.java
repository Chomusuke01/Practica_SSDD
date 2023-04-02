package es.um.sisdist.backend.Service;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.models.MR_DTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Response.Status;

@Path("/u")
public class UsersEndpoint
{
    private AppLogicImpl impl = AppLogicImpl.getInstance();
   
    @GET
    @Path("/hola")
    @Produces(MediaType.TEXT_PLAIN)
    public String hola() {
    	return "Hola";
    }
    
    @POST
    @Path("/{id}/db/{dbid}/mr")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response mapReduce(@PathParam("id") String userID, @PathParam("dbid") String dbid, MR_DTO dto) {
    	
    	boolean ok = impl.mapReduce(userID, dto.getOut_db(), dto.getMap(), dto.getReduce(), dbid);
    	
    	if (ok)
    		return Response.accepted().header("Location", UriBuilder.fromPath("/u/{id}/db/{DBID}/mr/{mrid}").build(userID, dbid, dto.getOut_db())).build();
    	
    	return Response.status(Status.FORBIDDEN).build();
    }
    
    @GET
    @Path("/{id}/db/{dbid}/mr/{mrid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkMRStatus(@PathParam("id") String userID, @PathParam("dbid") String dbid, @PathParam("mrid") String mrID) {
    	
    	int status = impl.getMrStatus(mrID, userID);
    	if (status == -1)
    		return Response.status(Status.NOT_FOUND).build();
    	
    	return Response.ok(String.format("{\"status\": %d}", status)).build();
    }
}