package com.flosum.model;

import java.io.File;
import java.util.Set;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.lib.Repository;

public class RepoDescr {

	private Long id;
	private String url;
	private File localPath;
	private Git git;
	private Repository repo;
	private GitCredentials gc;
	private Set<String> localBranches;

	public RepoDescr(Long id, String url, File localPath, Git git, Repository repo, GitCredentials gc) {
		this.id = id;
		this.url = url;
		this.localPath = localPath;
		this.git = git;
		this.repo = repo;
		this.gc = gc;
	}

	public RepoDescr() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public File getPath() {
		return localPath;
	}

	public void setPath(File localPath) {
		this.localPath = localPath;
	}

	public Git getGit() {
		return git;
	}

	public void setGit(Git git) {
		this.git = git;
	}

	public Repository getRepo() {
		return repo;
	}

	public void setRepo(Repository repo) {
		this.repo = repo;
	}

	public GitCredentials getGitCredentials() {
		return gc;
	}

	public void setGitCredentials(GitCredentials gc) {
		this.gc = gc;
	}

	public Set<String> getLocalBranches() {
		return localBranches;
	}

	public void setLocalBranches(Set<String> localBranches) {
		this.localBranches = localBranches;
	}

}