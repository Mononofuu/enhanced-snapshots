package com.sungardas.snapdirector.rest;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.sungardas.snapdirector.aws.EnvironmentBasedCredentialsProvider;
import com.sungardas.snapdirector.aws.dynamodb.DynamoUtils;
import com.sungardas.snapdirector.aws.dynamodb.model.User;
import com.sungardas.snapdirector.aws.dynamodb.repository.TaskRepository;
import com.sungardas.snapdirector.rest.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RequestMapping("/session")
public class AuthenticationController {

	private static final Logger LOG = LogManager.getLogger(AuthenticationController.class);

    @Autowired
    private ServletContext context;
    @Autowired
    private HttpServletRequest servletRequest;

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<String> login(@RequestBody User user) {
		// check whether there is already user logged in with the same session id
		Map<String, String> allowedSessions = getAllowedSessions();
		String sessionId = servletRequest.getSession().getId();
		// JIRA: SNAP-102
		// Temp solution. Replace email which is mapped to session in case when another user logged in from the same browser
		if (!allowedSessions.get(sessionId).equals(user.getEmail().toLowerCase())) {
			allowedSessions.put(sessionId, user.getEmail().toLowerCase());
		}
		ResponseEntity<String> responseEntity;
		String result = DynamoUtils.getFullUserInfoByEmail(user.getEmail(), getMapper(servletRequest));
		if (result == null) {
			responseEntity = new ResponseEntity("No appropriate user was found", HttpStatus.INTERNAL_SERVER_ERROR);
			LOG.debug("No user registered with email [{}] was found.", user.getEmail());
		} else {
			responseEntity = new ResponseEntity(result, HttpStatus.OK);
		}
		return responseEntity;
	}

	private DynamoDBMapper getMapper(ServletRequest request) {
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(new EnvironmentBasedCredentialsProvider());
		String region = request.getServletContext().getInitParameter(Constants.JSON_DYNAMODB_REGION);
		client.setRegion(Region.getRegion(Regions.fromName(region)));
		return new DynamoDBMapper(client);
	}

	@RequestMapping(method = RequestMethod.DELETE)
	public ResponseEntity<String> logout() {
		String sessionId = servletRequest.getSession().getId();
		Map<String, String> allowedSessions = getAllowedSessions();
		allowedSessions.remove(sessionId);
		LOG.debug("Logout for session: [{}].", sessionId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private Map<String, String> getAllowedSessions() {
		return (Map<String, String>) context.getAttribute(Constants.CONTEXT_ALLOWED_SESSIONS_ATR_NAME);
	}

}