package com.flosum.model;

import java.io.Serializable;

public class SystemInfo implements Serializable{
	
	private static final long serialVersionUID = 133L;
	
	private Integer load;
	private Integer roHandlers;
	private Integer rwHandlers;
	
	public SystemInfo(){
	}
	
	public SystemInfo(Integer load, Integer roHandlers, Integer rwHandlers){
		this.load = load;
		this.roHandlers = roHandlers;
		this.rwHandlers = rwHandlers;
	}

	public Integer getLoad() {
		return load;
	}

	public void setLoad(Integer load) {
		this.load = load;
	}

	public Integer getRoHandlers() {
		return roHandlers;
	}

	public void setRoHandlers(Integer roHandlers) {
		this.roHandlers = roHandlers;
	}

	public Integer getRwHandlers() {
		return rwHandlers;
	}

	public void setRwHandlers(Integer rwHandlers) {
		this.rwHandlers = rwHandlers;
	}
	

}
