package com.flosum.model;

import java.io.Serializable;

/**
 * This class used to transfer component's context between SF and Heroku
 */
public class MetaItem implements Serializable {

	private static final long serialVersionUID = 7L;

	private String name;
	private String sha;
	private String crc32;
	private String filename;
	private String dataStr;

	public MetaItem(String sha, String name, String data) {
		this.sha = sha;
		this.name = name;
		this.dataStr = data;
	}

	public MetaItem(String sha, String crc32, String name, String data) {
		this.sha = sha;
		this.crc32 = crc32;
		this.name = name;
		this.dataStr = data;
	}

	// need for deserialization
	public MetaItem() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataStr() {
		return dataStr;
	}

	public void setDataStr(String data) {
		this.dataStr = data;
	}

	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getCrc32() {
		return crc32;
	}

	public void setCrc32(String crc32) {
		this.crc32 = crc32;
	}

	public void setCrc32(Long value) {
		this.crc32 = (value == null) ? "":"" + value;
	}

}