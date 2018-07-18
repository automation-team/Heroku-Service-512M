package com.flosum.model;

import java.io.Serializable;

/**
 *		A simple POJO to hold info about git role (committer, author)
 */
public class GitActor implements Serializable {
	


	private static final long serialVersionUID = 9L;
	
	private String fullName;
	private String mail;

	/**
	 * 	Constructors
	 */
	public GitActor() {
	}

	public GitActor(String fullName, String mail) {
		this.fullName = fullName;
		this.mail = mail;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}
	


}
