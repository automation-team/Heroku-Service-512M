package com.flosum.model;

import java.util.List;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

public class Branch {

	private ObjectId id;
	private String name;
	private Ref commits;// reference to head commit structure in branch with sha = id 

	public Branch(ObjectId id, String name, Ref commits) {
		this.id = id;
		this.name = name;
		this.commits = commits;
	}

	public ObjectId getId() {
		return this.id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Ref getCommits() {
		return commits;
	}

	public void setCommits(Ref commits) {
		this.commits = commits;
	}

}
