package com.flosum.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Task {
	private final Long ticket;
	private final Boolean isTransactional;
	private Long opId; // equal to the number of sub operation in complex op
	private List<Long> resources; // associated resources
	private Map<String,Long> mapResources;

	public Task(Long ticket,  Long opId) {
		isTransactional = false;
		this.ticket = ticket;
		this.opId = opId;
		resources = new ArrayList<Long>();
		mapResources = new HashMap<String,Long>();
	}

	public Task(Long ticket,  Long opId, Boolean isTransactional) {
		this.isTransactional = isTransactional;
		this.ticket = ticket;
		this.opId = opId;
		resources = new ArrayList<Long>();
		mapResources = new HashMap<String,Long>();
	}

	public Long getOpId() {
		return opId;
	}

	public void setOpId(Long opId) {
		this.opId = opId;
	}

	public List<Long> getResources() {
		return resources;
	}

	public void setResources(List<Long> resources) {
		this.resources = resources;
	}
	
	public Long getResources(String type) {
		return mapResources.get(type);
	}
	
	public void addResources(Long resId, String type){
		this.resources.add(resId);
		this.mapResources.put(type, resId);
	}

	public Long getTicket() {
		return ticket;
	}

}
