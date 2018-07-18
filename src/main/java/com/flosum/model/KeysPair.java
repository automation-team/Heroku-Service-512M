package com.flosum.model;

import java.io.Serializable;

public class KeysPair implements Serializable {

	private static final long serialVersionUID = -7L;

	private String publicKey;
	private String privateKey;
	private String knownHosts;
	
	public KeysPair(){
	}
	
	public KeysPair(String publicKey, String privateKey, String knownHosts){
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.knownHosts = knownHosts;
	}

	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getKnownHosts() {
		return knownHosts;
	}

	public void setKnownHosts(String knownHosts) {
		this.knownHosts = knownHosts;
	}

}
