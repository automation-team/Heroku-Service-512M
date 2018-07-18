package com.flosum.model;

import java.io.Serializable;

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
		if (commitDate != null){
			this.commitDate = 0L + commitDate;
		}else{
			this.commitDate = null;
		}
	}

/*	public BranchDescriptor(String name, String commitId, String author, Long commitDate){
		this.name = name;
		this.commitId = commitId;
		this.author = author;
		this.commitDate = commitDate;
	}
*/
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
