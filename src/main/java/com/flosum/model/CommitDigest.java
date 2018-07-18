package com.flosum.model;

import java.io.Serializable;

import org.eclipse.jgit.revwalk.RevCommit;

/**
 * A POJO class, used to transfer data about single commit
 *
 */

public class CommitDigest implements Serializable {
	private static final long serialVersionUID = 8765789944191931199L;

	private String sha;
	private String commiter;
	private String message;
	private Integer commitTime;

	public CommitDigest(String sha, String commiter, String msg, Integer commitTime) {
		this.sha = sha;
		this.commiter = commiter;
		this.message = msg;
		this.commitTime = commitTime;
	}

	public CommitDigest(Commit c) {
		this.sha = c.getId().getName();
		this.commiter = c.getCommiter();
		this.message = c.getMessage();
		this.commitTime = c.getCommitTime();
	}

	public CommitDigest(RevCommit rc) {
		this.sha = rc.getId().getName();
		this.commiter = rc.getAuthorIdent().getName();
		this.message = rc.getShortMessage();
		this.commitTime = rc.getCommitTime();
	}

	public CommitDigest(){
	}

	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCommiter() {
		return commiter;
	}

	public void setCommiter(String commiter) {
		this.commiter = commiter;
	}

	public Integer getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(Integer commitTime) {
		this.commitTime = commitTime;
	}

}
