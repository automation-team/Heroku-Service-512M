package com.flosum.model;

import java.util.List;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * A POJO class, holds info about Commit in a Git repository
 *
 */

public class Commit {

	private ObjectId id;
	private String commiter;
	private String message;
	private Integer commitTime;
	private RevCommit head;// list of components, includes every blob
									// object in this slice

	public Commit(ObjectId id, String commiter, String msg, Integer commitTime, RevCommit head) {
		this.id = id;
		this.commiter = commiter;
		this.message = msg;
		this.commitTime = commitTime;
		this.head = head;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public RevCommit getCompRef() {
		return head;
	}

	public void setCompRef(RevCommit head) {
		this.head = head;
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
