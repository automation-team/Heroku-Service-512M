package com.flosum.model;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

import org.eclipse.jgit.lib.ObjectId;

public class BranchDigest implements Serializable {

	private static final long serialVersionUID = 3899649469154606565L;

	private String sha;
	private String name;
	private Integer totalCommits;
	private List<CommitDigest> commits;

	public BranchDigest(String sha, String name, List<CommitDigest> commits, Integer totalCommits) {
		this.sha = sha;
		this.name = name;
		this.commits = commits;
		this.totalCommits = totalCommits;
	}

	public BranchDigest(Branch b) {
		this.sha = b.getId().getName();
		this.name = b.getName();
	}


	public BranchDigest() {
	}

	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<CommitDigest> getCommits() {
		return commits;
	}

	public void setCommits(List<CommitDigest> commits) {
		this.commits = commits;
	}
	
	public Integer getTotalCommits(){
		return totalCommits;
	}
	
	public void setTotalCommits(Integer totalCommits){
		this.totalCommits = totalCommits;
	}
	
}
