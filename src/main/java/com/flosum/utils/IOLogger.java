package com.flosum.utils;

import com.flosum.constants.ServiceConst;

public class IOLogger {
	
	private static final Integer MAX_SIZE = 8192;
	private String log;
	private Boolean bLimitExceeded = false;

	public IOLogger() {
		log = "";
	}
	
	public void add(String line){
		if (!bLimitExceeded && line != null && line != ""){
			if (line.length()+log.length() < MAX_SIZE){
				log += (line + "\n");
			}else{
				bLimitExceeded = true;
				log += "LOG SIZE LIMITED TO 8K";
			}
		}
	}
	
	public void download(String comptype, int size){
		add("Downloaded "+ size + " " + comptype + " components");
	}

	public void upload(String comptype, int size){
		add("Uploaded "+ size + " " + comptype + " components");
	}

	public void error(String msg){
		add("ERROR: " + msg);
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}
	
	public static String changeResultString(String compType, Integer size) {
		if (ServiceConst.DELETED.equals(compType)) {
			return ServiceConst.I_DELCOMPONENTS + ((size == 0 ) ? "": ("("+ size +" items)"));
		}else {
			return ServiceConst.I_ADDCOMPONENTS + ((size == 0 ) ? "": ("("+ size +" items)"));
		}
	}

}
