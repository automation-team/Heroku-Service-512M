package com.flosum.model;

import java.io.Serializable;

import com.jcraft.jsch.KeyPair;

public class GitCredentials implements Serializable {
	
	private static final long serialVersionUID = -7L;


	private String userId;
	private String host;
	private String path;
	private String password;
	private String username;
	private KeysPair pair;
	private String protocol;

	// needed for deserialization
	public GitCredentials() {
	}

	
	public GitCredentials(String host, String path, String password, String username, String userId, KeysPair pair, String protocol) {
		this.host = host;
		this.path = path;
		this.password = password;
		this.username = username;
		this.userId = userId;
		this.pair = pair;
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}


	public KeysPair getPair() {
		return pair;
	}


	public void setPair(KeysPair pair) {
		this.pair = pair;
	}


	public String getProtocol() {
		return protocol;
	}


	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

}