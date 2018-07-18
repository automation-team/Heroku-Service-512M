package com.flosum.model;

import java.util.List;
import java.io.Serializable;

public class GitStatus implements Serializable {

	private static final long serialVersionUID = -1L;

	private Integer blobsTotalSize;
	private Integer added;
	private Integer changed;
	private Integer modified;
	private Integer removed;

	// needed for deserialization
	public GitStatus() {
	}

}
