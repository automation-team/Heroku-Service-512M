package com.flosum.model;

public class Session {
	
	private String serverUrl;
	private String sessionId;
	private String identity;
	private Boolean isSuccess;
	
	public Session(){
	}

	public Session(String serverUrl, String sessionId,String identity){
		this.serverUrl = serverUrl;
		this.identity = identity;
		this.sessionId = sessionId;
		this.isSuccess = false;
	}

	public Session(String serverUrl, String sessionId){
		this.serverUrl = serverUrl;
		this.sessionId = sessionId;
		this.isSuccess = false;
	}

	public String getServerUrl() {
		return serverUrl;
	}
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public Boolean getIsSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

}
