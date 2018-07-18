package com.flosum.dao;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flosum.model.CommitDigest;

@Component
public class ScheduledTasks {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduledTasks.class);
	private static final String PROD = "Production";
	private static final String SANDBOX = "Sandbox";
	
	private static String 	refresh_token = "5Aep8613hy0tHCYdhwXFxJDx1ZQFoantJpmqCA5IMpgA8FuYoILMReaa8avua9W24YMJ_xCOoTyeBsZnhugtB3x";
	private static String	client_id = "2NsSCiOkE5MoZcG1PXFNibFgdvAtTxH4zy48OtLVJBBSnhOJzjy1PyZ2w7XRxz0il7uSz47FQqUgUK3TToaQF";
	private static String	client_secret = "Y946TCnzr25eYmX3kGC";
	private static String	org_type = "Production";
	
	private static final String loginSFEndpoint_Sandbox = "https://test.salesforce.com/services/oauth2/token?grant_type=refresh_token&format=json";
	private static final String loginSFEndpoint_Production = "https://login.salesforce.com/services/oauth2/token?grant_type=refresh_token&format=json";
	private static final String SFEndpoint = "/services/apexrest/Flosum/sync";// for package version  = "/services/apexrest/Flosum/sync";
	private static final String SFRESPONSE = "OK";
	
	private static GitFactory factory;
	
	private	static String accessToken;
	private static String instanceUrl;
	// map repositoryId => { isRequestProcessed} NB: for tracking must be non-empty!
	private static ConcurrentMap<Long,TrackingState> trackedList = new ConcurrentHashMap<Long,TrackingState>() ;
	private static volatile Boolean trackingOn = true;
	// used to prevent concurrent execution - if true, then server already processing request
	private static volatile Boolean requestProcessing = false;
	// used to detect after reboot case
	private static volatile Boolean bAfterReboot = true;
	
	private List<UpdateDescriptor> changedList;
	private  CopyOnWriteArrayList<UpdateDescriptor> lastChangedList;
	private static volatile Boolean bDelivered = false;
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	public ScheduledTasks(){
		LOG.info("constuctor: ScheduledTasks");
		lastChangedList = new CopyOnWriteArrayList<UpdateDescriptor>();
		changedList = new ArrayList<UpdateDescriptor>();
		bDelivered = true;
	}

	@Scheduled(fixedRate = 40000)
	public void reportAboutGitChanges() {
        LOG.info("Check time: {}", dateFormat.format(new Date()));
        LOG.info("Track list: {}", trackedList.size());
        LOG.info("requestProcessing: {}", requestProcessing);
        LOG.info("bAfterReboot: {}", bAfterReboot);
        if ((trackedList.isEmpty() || requestProcessing) && !bAfterReboot) return;// nothing to track | busy
        if (!trackingOn) return;// check does  tracking functionality turned on
        requestProcessing = true;

        if (factory == null){
        	LOG.info("Init factory");
        	factory = RepoDAOImpl.getGFactory();
        }
        
        changedList = new ArrayList<UpdateDescriptor>();
        for (Long repoId : trackedList.keySet()){// iterate over all available repositories
  		 LOG.info("Processing repository: {}", repoId);
//        	if (trackedList.get(repoId).isProcessed()){
        	if (true){
        		// get updates only if was no response from SF about sync completion 
        		// (this flag is autoset after pull/clone completed for this repoId)
        		LOG.info("Tracking repo: {}", repoId);
    			try {
					GitHandlerRO gh = factory.getGitROHandler(repoId);
    				if (gh != null){// verify is read handler available
        				Map<String, CommitDigest> map = gh.ShowBranchTrackingStatus();
        				UpdateDescriptor ud = extractChanges(map,repoId, trackedList.get(repoId).getUri());
        				if ( ud  != null){
        					changedList.add(ud);
        					trackedList.get(ud.getRepoId()).setIsChanged(true);
        				}

    				}else{// remove this repository  from track list
    					removeTrackingRepository(repoId);
    				}
    			} catch (GitAPIException | IOException e) {
    				LOG.info(" error: "+e.getMessage());
    				requestProcessing = false;
    				return;
    			} catch (Exception e) {
    		        LOG.info(" Exception: "+ e.getMessage());
    		        requestProcessing = false;
    		        return;
    		    }
        	}
        }
        // verify  After Reboot case
	LOG.info("verify  After Reboot case: {}", bAfterReboot);
        if (bAfterReboot){
        	changedList.add(new UpdateDescriptor(null, 0L,"AllActive"));
        	bAfterReboot = false;
        }
        bDelivered = false;
        
        // verify is return list empty
	LOG.info("changedList.isEmpty(): {}", changedList.isEmpty());
        if (changedList.isEmpty()){
        	requestProcessing = false;
        	bDelivered = true;
        	return;
        }
        updateCache();
        
        // get access key
        String token = System.getenv("sftoken");
        if (token == null){
        	LOG.info(" no token set");
        	return;
        }
        
        String[] tokens = token.split(" ");
        LOG.info("tokens:{}",tokens);
        if (tokens == null){
        	LOG.info("wrong token");
        	return;
        }
        refresh_token = tokens[0];
        client_id = tokens[1];
        client_secret = tokens[2];
        org_type = tokens[3];
    	LOG.info("refresh_token={}",refresh_token);
    	LOG.info("client_id={}",client_id);
    	LOG.info("client_secret={}",client_secret);
    	LOG.info("org_type={}",org_type);

    	// send it to SF
        if (accessToken == null){
        	getSFAccess();
        }
        // try call SF with token
        HttpStatus result = null;
        
        int nFails = 4;
        
        result = doSFPost();
        while (nFails--  > 0){
        	if ((result != null && result.is4xxClientError()) || result == null){
        		// try to get a new access key
        		getSFAccess();
        		result = doSFPost();
        		LOG.info("2nd call result:"+result);
        	}

        	// reset accessToken if there still errors are
        	if (result != null){
        		if (!result.is2xxSuccessful()){
        			result = null;
        			accessToken = null;
        		}else{// success
        			break;
        		}
        	}else{
        		accessToken = null;
        	}
        }

    	requestProcessing = false;
    }
	
	private UpdateDescriptor extractChanges(Map<String,CommitDigest> map, Long repoId, String uri) {
		UpdateDescriptor ud = new UpdateDescriptor(new ArrayList<BranchDescriptor>(),repoId,uri);
		for (String branchName : map.keySet()){
			CommitDigest detail = map.get(branchName);
				ud.getbranchNameList().add( new BranchDescriptor(branchName,detail.getSha(),detail.getCommiter(), detail.getCommitTime()));
				LOG.info("For branch: " + branchName);
		}
		if (ud.getbranchNameList().isEmpty()){
			return null;
		}else{
			return ud;
		}
	}

	/**
	 * Adds id of repository to list to track changes
	 * uri is its unique address in the canonical form
	 */
	public void addTrackingRepository(Long repoId, String uri){
		LOG.info("addTrackingRepository:"+repoId + ",uri: " + uri);
		trackedList.putIfAbsent(repoId, new TrackingState(true,uri));
	}
	
	/**
	 * if repository (invalid | stop exists on hdd) remove it from list
	 * @param repoId
	 */
	public void removeTrackingRepository(Long repoId){
		if (trackedList.containsKey(repoId)){
			trackedList.remove(repoId);
		}
	}
	
	/**
	 * Used to set flag indicating to ignore Or track
	 * @param repoId
	 * @param value
	 */
	public void setFlagTrackingRepository(Long repoId, Boolean value){
		if (trackedList.containsKey(repoId)){
			trackedList.get(repoId).setProcessedFlag(value);
			trackedList.get(repoId).setIsChanged(false);
		}else{
			LOG.info("attempt to set flag for non-existing repository {}",repoId);
		}
	}

	public void clearTrackingRepositories(){
		trackedList.clear();
	}

	/**
	 * Methods to connection with SF
	 * @return
	 */
	private HttpHeaders createHttpHeaders()
	{
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.set("Authorization","Bearer " + accessToken);
	    return headers;
	}
	
	private void getSFAccess() 
	{
		String loginSFEndpoint = null;
		if (PROD.equals(org_type)){
			loginSFEndpoint = loginSFEndpoint_Production;
		}else if (SANDBOX.equals(org_type)){
			loginSFEndpoint = loginSFEndpoint_Sandbox;
		}else {
			 LOG.info("Error: unsupported org type ");
			return;
		}
	    String theUrl = loginSFEndpoint + "&client_id=" + client_id + "&client_secret=" + client_secret + "&refresh_token=" + refresh_token;
	    RestTemplate restTemplate = new RestTemplate();
	    try {
	        HttpHeaders headers = new HttpHeaders();
	        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
	        ResponseEntity<Map> response = restTemplate.exchange(theUrl, HttpMethod.POST, entity, Map.class);
	        LOG.info("getSFAccess - status ("+ response.getStatusCode() + ") has body: " + response.hasBody());
	        LOG.info("body: " + response.hasBody());
	        if (response.hasBody()){
	        	Map res = response.getBody();
	        	if (res.containsKey("access_token")){
	        		accessToken = (String) res.get("access_token");
	        	}
	        	if (res.containsKey("instance_url")){
	        		instanceUrl = (String) res.get("instance_url");
	        	}
	        }
	    }
	    catch (Exception e) {
	        LOG.info("** Exception: "+ e.getMessage());
	    }
	}

	
	private HttpStatus doSFPost() 
	{
	    String theUrl = instanceUrl + SFEndpoint;
	    RestTemplate restTemplate = new RestTemplate();
	    try {
	        HttpHeaders headers = createHttpHeaders();

	        String json = toJson(changedList);
	        LOG.info("json="+json);
	        HttpEntity<String> entity = new HttpEntity<String>(json, headers);
	        ResponseEntity<String> response = restTemplate.exchange(theUrl, HttpMethod.POST, entity, String.class);
	        HttpStatus status = response.getStatusCode();
	        LOG.info("Result - status ("+ status+ ") has body: " + response.hasBody() + " body:" + response.getBody());
	        if (response.getBody() != "" && response.getBody().contains(SFRESPONSE)){
	        	LOG.info("Delivered");
	        	for (UpdateDescriptor ud : changedList){// update states
	        		if (trackedList.containsKey(ud.getRepoId()))
	        			trackedList.get(ud.getRepoId()).setProcessedFlag(false);
	        	}
	        }
	        return status;
	    }
	    catch (Exception e) {
	    	 LOG.info("** Exception: "+ e.getMessage());
	    	 return null;
	    }
	}
	
	private String toJson(List<UpdateDescriptor> obj){
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			LOG.info("Json error,{}",e.getMessage());
			return "{}";
		} 

	}
	
	public Boolean isRepositoryChanged(Long rid){
		if (trackedList.containsKey(rid)){
			return trackedList.get(rid).getIsChanged();
		}
		return false;
	}
	
	public Boolean resetRepositoryChanges(Long rid){
		if (trackedList.containsKey(rid)){
			trackedList.get(rid).setIsChanged(false);
			return true;
		}
		return false;
	}
	
	public List<UpdateDescriptor> getCache(){
		List<UpdateDescriptor> ret = new ArrayList<UpdateDescriptor>();
		for (UpdateDescriptor ud : lastChangedList){
			ret.add(ud);
		}
		lastChangedList.clear();
		bDelivered = true;
		return ret;
	}
	
	public void updateCache(){
		lastChangedList.clear();
		for (UpdateDescriptor ud : changedList){
			lastChangedList.add(ud);
		}
		bDelivered = false;
	}

	/*
	 * Inner classes to call resources end point on SF 
	 */
	public class UpdateDescriptor implements Serializable{

		private static final long serialVersionUID = -7944823388268912518L;
		private List<BranchDescriptor> branchNameList;
		private Long repoId;
		private String repoUri;// url from repodescriptor
		
		public UpdateDescriptor(){
		}

		public UpdateDescriptor(List<BranchDescriptor> branchNameList, Long repoId, String repoUri){
			this.branchNameList = branchNameList;
			this.repoId = repoId;
			this.repoUri = repoUri;
		}

		public List<BranchDescriptor> getbranchNameList() {
			return branchNameList;
		}
		public void setbranchNameList(List<BranchDescriptor> branchNameList) {
			this.branchNameList = branchNameList;
		}
		public Long getRepoId() {
			return repoId;
		}
		public void setRepoId(Long repoId) {
			this.repoId = repoId;
		}

		public String getRepoUri() {
			return repoUri;
		}

		public void setRepoUri(String repoUri) {
			this.repoUri = repoUri;
		}
		
	}
	
	public class TrackingState {
		private Boolean isChanged;


		private Boolean isProcessed;
		private String uri;
		
		public TrackingState(Boolean isProcessed, String uri){
			this.isProcessed = isProcessed;
			this.uri = uri;
			this.isChanged = false;
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public Boolean isProcessed() {
			return isProcessed;
		}

		public void setProcessedFlag(Boolean isProcessed) {
			this.isProcessed = isProcessed;
		}
		
		public Boolean getIsChanged() {
			return isChanged;
		}

		public void setIsChanged(Boolean isChanged) {
			this.isChanged = isChanged;
		}
	}
	
	public class BranchDescriptor  implements Serializable{
		
		private static final long serialVersionUID = -1144823388368912518L;

		private String name;
		private String commitId;
		private String author;
		private Long commitDate;
		
		
		public BranchDescriptor(){
		}

		public BranchDescriptor(String name, String commitId, String author, Integer commitDate){
			this.name = name;
			this.commitId = commitId;
			this.author = author;
			this.commitDate = 0L + commitDate;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getCommitId() {
			return commitId;
		}
		public void setCommitId(String commitId) {
			this.commitId = commitId;
		}
		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public Long getCommitDate() {
			return commitDate;
		}

		public void setCommitDate(Long commitDate) {
			this.commitDate = commitDate;
		}
	}

	public Boolean getTrackingOn() {
		return trackingOn;
	}

	public void setTrackingOn(Boolean trackingOn) {
		this.trackingOn = trackingOn;
	}
	
	/**
	 *  Utility for diagnostics
	 */
	public void printStatus(Map<String, List<Integer>> map){
		for (String branchName : map.keySet()){
			List<Integer> counts = map.get(branchName);
			LOG.info("For branch: " + branchName);
			LOG.info("Commits ahead : " + counts.get(0));
			LOG.info("Commits behind : " + counts.get(1));
		}
	}

}
