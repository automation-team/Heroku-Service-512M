package com.flosum.model;

import java.io.Serializable;

public class DataWrapper implements Serializable {
	private final static Integer TYPE_BRANCH = 0;
	private final static Integer TYPE_COMMIT = 1;
	private final static Integer TYPE_COMPONENT = 2;
	private final static Integer TYPE_DATA = 3;
	
	private static final long serialVersionUID = 17L;
	
	private final Long opId;
	private final Integer nChunks;
	private String dataStr;
	private Boolean isNext;

	// empty wrapper - returned if no data
	public DataWrapper(Long opId) {
		this.opId = opId;
		this.nChunks = 0;
		this.isNext = false;
	}

	
	public DataWrapper(Long opId, Integer nChunks, String dataStr, Boolean isNext) {
		this.opId = opId;
		this.nChunks = nChunks;
		this.dataStr = dataStr;
		this.isNext = isNext;
	}

	public String getDataStr() {
		return dataStr;
	}

	public void setDataStr(String dataStr) {
		this.dataStr = dataStr;
	}

	public Boolean getIsNext() {
		return isNext;
	}

	public void setIsNext(Boolean isNext) {
		this.isNext = isNext;
	}

	public Long getOpId() {
		return opId;
	}

	public Integer getnChunks() {
		return nChunks;
	}
	

}
