package com.flosum.model;

import java.io.Serializable;

public class Descriptor implements Serializable {

	private static final long serialVersionUID = 8L;

	private String url;
	private String owner;
	private String localDir;
	private Integer lastModified;
	private Integer totalSize;

	public Descriptor() {
	}

	public Descriptor(String url, String owner, String localDir, Integer lastModified, Integer totalSize) {
		this.url = url;
		this.owner = owner;
		this.localDir = localDir;
		this.lastModified = lastModified;
		this.totalSize = totalSize;

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

	public String getLocalDir() {
		return localDir;
	}

	public void setLocalDir(String localDir) {
		this.localDir = localDir;
	}

	public Integer getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(Integer totalSize) {
		this.totalSize = totalSize;
	}

	public Integer getLastModified() {
		return lastModified;
	}

	public void setLastModified(Integer lastModified) {
		this.lastModified = lastModified;
	}

}