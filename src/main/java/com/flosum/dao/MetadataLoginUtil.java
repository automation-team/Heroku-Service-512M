package com.flosum.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.flosum.model.AuthDetails;
import com.flosum.model.Session;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class MetadataLoginUtil {
	
	private final static  Logger LOG = LoggerFactory.getLogger(MetadataLoginUtil.class);

	private final static String SERVICE_ENDPOINT = "/services/Soap/m/41.0"; 
	private final static String SERVICE_URL = "/services/data/v26.0/"; 
	private final static String AUTH_URL = "/services/oauth2/token?grant_type=refresh_token"; 
	private final static String BEARER = "Bearer ";
	private final static Integer MAX_NUMBER_ATTEMPTS = 3;
	
	
	/**
	 * Login utility.
	 */
	public static MetadataConnection login(AuthDetails auth) throws ConnectionException {
		final Session loginResult = loginToSalesforce(auth);
		return createMetadataConnection(loginResult);
	}

	private static MetadataConnection createMetadataConnection(final Session session)	throws ConnectionException {
		if (session == null) return null;
		final ConnectorConfig config = new ConnectorConfig();
		config.setServiceEndpoint(session.getServerUrl() + SERVICE_ENDPOINT);
		config.setSessionId(session.getSessionId());
		return new MetadataConnection(config);
	}

	private static Session loginToSalesforce(AuthDetails auth)	throws ConnectionException {
		Session curSession;
		Integer counter = MAX_NUMBER_ATTEMPTS;
		while (counter --  > 0){
			curSession = getSFAccess(auth);
			if (curSession.getIsSuccess()){
				LOG.info("curSession: success");
				return curSession;
			}else{
				updateTokens(auth);
			}
		}
		return null;
	}
	
	/**
	 * Methods to connection with SF
	 * @return
	 */
	private static HttpHeaders createHttpHeaders(String accessToken)
	{
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.set("Authorization",BEARER + accessToken);
	    return headers;
	}
	
	/**
	 * Methods to connection with SF
	 * @return
	 */
	private static HttpHeaders createHttpHeaders()
	{
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    return headers;
	}

	
	private static Session getSFAccess(AuthDetails auth) 
	{
		String loginSFEndpoint = auth.getInstanceUrl();
		String theUrl = loginSFEndpoint + SERVICE_URL;
		Session session = new Session(loginSFEndpoint, auth.getAccessToken());

	    RestTemplate restTemplate = new RestTemplate();
	    try {
	    	HttpHeaders headers = createHttpHeaders(auth.getAccessToken());
	        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
	        ResponseEntity<Map> response = restTemplate.exchange(theUrl, HttpMethod.GET, entity, Map.class);
	        if (response != null  && response.getStatusCode().is2xxSuccessful()){
	        	LOG.info("getSFAccess - status ("+ response.getStatusCode() + ") has body: " + response.hasBody());
	        	LOG.info("body: " + response.hasBody());
	        	if (response.hasBody()){
	        		Map res = response.getBody();
	        		String url = session.getServerUrl();
	        		String accessToken = session.getSessionId();
	        		if(res.containsKey("Error")){ 
	        			return session;
	        		}
	        		if (res.containsKey("identity")){
	        			String identity = (String) res.get("identity");
	        			session.setIdentity(identity); // set additional params
	        		}
	        		session.setIsSuccess(true);
	        		return session;
	        	}
	        }
	    }
	    catch (Exception e) {
	        LOG.info("** Exception: "+ e.getMessage());
	        return session;
	    }
	    return session;
	}

	
	private static void updateTokens(AuthDetails auth) 
	{
	    String theUrl =	auth.getInstanceUrl() + AUTH_URL + 
	    				"&refresh_token=" + auth.getRefreshToken() +
	    				"&client_id=" + auth.getClientId() +
	    				"&client_secret=" + auth.getClientSecret();
	    RestTemplate restTemplate = new RestTemplate();
	    try {
	        HttpHeaders headers = createHttpHeaders();
	        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
	        ResponseEntity<Map> response = restTemplate.exchange(theUrl, HttpMethod.POST, entity, Map.class);
	        if (response != null && response.getStatusCode().is2xxSuccessful()){
	        	HttpStatus status = response.getStatusCode();
	        	LOG.info("Result - status ("+ status+ ") has body: " + response.hasBody() + " body:" + response.getBody());
	        	if (response.hasBody()){
	        		Map res = response.getBody();
		        	if (res.containsKey("access_token")){
		        		auth.setAccessToken((String) res.get("access_token"));
		        		
		        	}
		        	if (res.containsKey("instance_url")){
		        		auth.setInstanceUrl((String) res.get("instance_url"));
		        	}
		        	if (res.containsKey("id")){
		        		String id = (String) res.get("id");
		        		LOG.info("id={}",id);
		        	}
	        	}
	        }
	    }
	    catch (Exception e) {
	    	 LOG.info("** Exception: "+ e.getMessage());
	    }
	}


}
