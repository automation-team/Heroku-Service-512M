package com.flosum.model;

import java.util.List;
import java.io.File;
import org.eclipse.jgit.lib.ObjectId;

public class Component {

	private String sha;
	private String compType;// equal to parent's directory name
	private String compName;
	private Long length;
	private File path;
	private String relPath;

	public Component(String sha, String compType, String compName, Long length, File path, String relPath) {
		this.sha = sha;
		this.compType = compType;
		this.compName = compName;
		this.length = length;
		this.path = path;
		this.relPath = relPath;
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

	public File getPath() {
		return path;
	}

	public void setPath(File path) {
		this.path = path;
	}

	public String getRelPath() {
		return relPath;
	}

	public void setRelPath(String relPath) {
		this.relPath = relPath;
	}

}
