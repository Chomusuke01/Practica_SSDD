package es.um.sisdist.backend.Service;

import java.util.Optional;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.models.BD_DTO;
import es.um.sisdist.models.KeyValueDTO;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
   
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(UserDTO uo) {
    	
    	Optional<User> user = impl.newUser(uo);
    	
    	if (user.isPresent()) {
    		return Response.created(UriBuilder.fromPath("/u/{id}").build(user.get().getEmail())).build();
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
}
