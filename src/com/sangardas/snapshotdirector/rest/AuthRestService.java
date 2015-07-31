package com.sangardas.snapshotdirector.rest;

import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.sangardas.snapshotdirector.rest.utils.JsonFromFile;


@Path("/session")
public class AuthRestService {
	private static final Log LOG = LogFactory.getLog(AuthRestService.class);

	@Context
	ServletContext context;


	@PUT()
	@Produces(MediaType.APPLICATION_JSON)
	public String login() {
		String path = context.getInitParameter("rest:mock-directory");
		String result = null;
		try {
			JSONObject user = JsonFromFile.newJSONObject(path + "user.json");
			result = user.toString();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		return result;
	}
	
	
	@DELETE()
	@Produces(MediaType.APPLICATION_JSON)
	public String logout(@Context HttpServletRequest request) {
		String sessionId = request.getSession().getId();
		Set<String> allowedSessions = (Set<String>) context.getAttribute("allowedSessions");
		allowedSessions.remove(sessionId);
		return null;
		
	}
}
