package es.um.sisdist.backend.Service;

import java.io.IOException;
import java.util.Optional;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;

@Provider
public class FiltroAutenticacion implements ContainerRequestFilter {
	
	private AppLogicImpl impl = AppLogicImpl.getInstance();
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		String url = requestContext.getUriInfo().getAbsolutePath().toString();
		String date = requestContext.getHeaderString("Date");
		String authToken = requestContext.getHeaderString("Auth-Token");
		String user = requestContext.getHeaderString("User");
		
		Optional<User> u = impl.getUserById(user);
		
		if (u.isEmpty() || !autenticar(date, url, authToken, u.get().getToken())) {
			requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
		}
		
	}

	private boolean autenticar(String date, String url, String authToken, String userToken) {
		String token = UserUtils.md5pass(url + date + userToken);
		return authToken.equals(token);
	}
}
