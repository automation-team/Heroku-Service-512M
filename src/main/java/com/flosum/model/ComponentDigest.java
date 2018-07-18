package com.flosum.model;

import java.io.Serializable;

public class ComponentDigest implements Serializable {

	private static final long serialVersionUID = -1L;

	private String sha;
	// f.e. CustomObject; implicitly associated with parent's directory name through maps in CompType class
	private String compType;
	// f.e. Account
	private String compName;
	private Integer lastModified;
	//f.e. objects/Account.object
	// NB: for inner components this is a parent comp file
	private String mLabel;
	// size of component's body
	private Long length;

	public ComponentDigest(String sha, String compType, String compName, Long length, Integer lastModified, String mLabel) {
		this.sha = sha;
		this.compType = compType;
		this.compName = compName;
		this.lastModified = lastModified;
		this.mLabel = mLabel;
		this.length = length;
	}

	public ComponentDigest(Component c) {
		this.sha = c.getSha();
		this.compType = c.getCompType();
		this.compName = c.getCompName();
		this.mLabel = c.getRelPath();
		this.length = c.getLength();
		this.lastModified = null;
	}

	// needed for deserialization
	public ComponentDigest() {
	}

	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public String getCompType() {
		return compType;
	}

	public void setCompType(String compType) {
		this.compType = compType;
	}

	public String getCompName() {
		return compName;
	}

	public void setCompName(String compName) {
		this.compName = compName;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public Integer getLastModified() {
		return lastModified;
	}

	public void setLastModified(Integer lastModified) {
		this.lastModified = lastModified;
	}

	public String getmLabel() {
		return mLabel;
	}

	public void setmLabel(String mLabel) {
		this.mLabel = mLabel;
	}

}
