package es.um.sisdist.backend.Service;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.models.BD_DTO;
import es.um.sisdist.models.KeyValueDTO;
import es.um.sisdist.models.MR_DTO;
import es.um.sisdist.models.Query_DTO;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Response.Status;

@Path("/u")
public class UsersEndpoint
{
    private AppLogicImpl impl = AppLogicImpl.getInstance();
    private static final Logger logger = Logger.getLogger(UsersEndpoint.class.getName());
    @GET
    @Path("/hola")
    @Produces(MediaType.TEXT_PLAIN)
    public String hola() {
    	return "Hola";
    }
    
    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserInfo(@PathParam("username") String username)
    {
        return UserDTOUtils.toDTO(impl.getUserByEmail(username).orElse(null));
    }
    
    @POST
    @Path("/{id}/db")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addDB(@PathParam("id") String userID, BD_DTO bdDto) {
    	
    	Optional<String> bdID= impl.newBD(bdDto, userID);
    	
    	if (bdID.isPresent()) {
    		return Response.created(UriBuilder.fromPath("/u/{id}/db/{DBID}").build(userID,bdID.get())).build();
    	}
    	return Response.status(Status.FORBIDDEN).build();
    }
    
    @GET
    @Path("/{id}/db/{dbid}")
    @Produces(MediaType.APPLICATION_JSON)
    public BD_DTO getDatabases(@PathParam("id") String userID, @PathParam("dbid") String dbid) {
    	Optional<BD_DTO> bddto = impl.getDatabases(userID, dbid);
    	return bddto.get();
    }
    
    @GET
    @Path("/{id}/db")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserDatabases(@PathParam("id") String userID) {
    	
    	Optional<ArrayList<String>> databases = impl.getUserDatabases(userID);
    	
    	if (databases.isPresent()) {
    		return Response.ok(databases.get()).build();
    	}
    	return Response.status(Status.NOT_FOUND).build();
    }
    
    
    
    @PUT
    @Path("/{id}/db/{dbid}/d/{key}")
    public Response addKeyValue(@QueryParam("v") String value, @PathParam("id") String userID, @PathParam("dbid") String dbID, @PathParam("key") String key) {
    	
    	boolean ok = impl.addKeyValue(userID, key, value, dbID);
    	
    	if (ok) {
    		return Response.status(Status.OK).build();
    	}
    	
    	return Response.status(Status.METHOD_NOT_ALLOWED).build();
    }
    
    
    @GET
    @Path("/{id}/db/{dbid}/d/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValue(@PathParam("id") String userID, @PathParam("dbid") String dbID, @PathParam("key") String key) {
    	
    	Optional <KeyValueDTO> value = impl.getValue(userID, key, dbID);
    	
    	if (value.isPresent()) {
    		return Response.ok(value.get()).build();
    	}
    	
    	return Response.status(Status.NOT_FOUND).build();
    }
    
    @DELETE
    @Path("/{id}/db/{dbid}/d/{key}")
    public Response deletePair(@PathParam("id") String userID, @PathParam("dbid") String dbID, @PathParam("key") String key) {
    	
    	boolean ok = impl.deletePair(userID, key, dbID);
    	
    	if (ok) {
    		return Response.ok().build();
    	}
    	
    	return Response.status(Status.METHOD_NOT_ALLOWED).build();
    }
    
    
    @POST
    @Path("/{id}/db/{dbid}/mr")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response mapReduce(@PathParam("id") String userID, @PathParam("dbid") String dbid, MR_DTO dto) {
 
    		logger.info("Se autentico correctamente");
    		boolean ok = impl.mapReduce(userID, dto.getOut_db(), dto.getMap(), dto.getReduce(), dbid);

    		if (ok)
    			return Response.accepted().header("Location", UriBuilder.fromPath("/u/{id}/db/{DBID}/mr/{mrid}").build(userID, dbid, dto.getOut_db())).build();

    		return Response.status(Status.UNAUTHORIZED).build();
    }
    	
    @GET
    @Path("/{id}/db/{dbid}/q")
    @Produces(MediaType.APPLICATION_JSON)
    public Response query(@PathParam("id") String userID, @PathParam("dbid") String dbID, @QueryParam("pattern") String pattern, @QueryParam("page") int page, @DefaultValue("50") @QueryParam("perpage") int perpage) {
    	
    	
    	String patternDecoded = null;
    	
    	try {
			patternDecoded = URLDecoder.decode(pattern, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
    	
    	Optional<Query_DTO> dto = impl.makeQuery(userID, patternDecoded, dbID, page, perpage);
    	
    	if (dto.isPresent()) {
    		return Response.ok(dto.get()).build();
    	}
    	return Response.status(Status.METHOD_NOT_ALLOWED).build();
    		
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