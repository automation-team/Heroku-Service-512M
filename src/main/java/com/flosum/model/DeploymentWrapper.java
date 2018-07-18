package com.flosum.model;

import java.io.Serializable;

public class DeploymentWrapper implements Serializable{
	private static final long serialVersionUID = 79L;
	
	private AuthDetails auth;
	private DeploymentOptions opts;
	
	public DeploymentWrapper(){
	}
	
	public AuthDetails getAuth() {
		return auth;
	}
	public void setAuth(AuthDetails auth) {
		this.auth = auth;
	}
	public DeploymentOptions getOpts() {
		return opts;
	}
	public void setOpts(DeploymentOptions opts) {
		this.opts = opts;
	}
	
}
