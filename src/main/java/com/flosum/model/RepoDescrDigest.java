package com.flosum.model;

import java.io.Serializable;

public class RepoDescrDigest implements Serializable {

	private static final long serialVersionUID = 10L;

	private Long gid;
	private String url;
	private String owner;
	private String localPath;
	

	public RepoDescrDigest(Long gid, String url, String owner, String localPath) {
		this.gid = gid;
		this.url = url;
		this.owner = owner;
		this.localPath = localPath;
	}

	// needed for deserialization
	public RepoDescrDigest() {
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

}
