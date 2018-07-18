package com.flosum.model;

import java.io.Serializable;

/**
 * This class used to transfer component's context between SF and Heroku
 */
public class PayloadD implements Serializable {

	private static final long serialVersionUID = 4L;

	private String base64;

	public PayloadD(String base64) {
		this.base64 = base64;
	}


	// need for deserialization
	public PayloadD() {
	}
	
	/**
	 * 		Getter and Setter Methods
	 */


	public String getBase64() {
		return base64;
	}


	public void setBase64(String base64) {
		this.base64 = base64;
	}


}