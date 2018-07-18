package com.flosum.model;

import java.io.Serializable;

/**
 * A POJO class, holds a [string] message
 *
 */
public class Message implements Serializable {

	private static final long serialVersionUID = -7647619618070649931L;

	private String message;

	public Message() {
	}

	public Message(String msg) {
		this.message = msg;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String msg) {
		this.message = msg;
	}

}
