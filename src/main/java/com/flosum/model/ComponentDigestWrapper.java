package com.flosum.model;

import java.io.Serializable;
import java.util.List;

public class ComponentDigestWrapper  implements Serializable {

	private static final long serialVersionUID = -11L;
	
	private Integer totalComponents;
	private List<ComponentDigest> listComp;

	
	public ComponentDigestWrapper(Integer totalComponents, List<ComponentDigest> listComp) {
		this.totalComponents = totalComponents;
		this.listComp = listComp;
	}

	// needed for deserialization
	public ComponentDigestWrapper() {
	}

	public Integer getTotalComponents() {
		return totalComponents;
	}

	public void setTotalComponents(Integer totalComponents) {
		this.totalComponents = totalComponents;
	}

	public List<ComponentDigest> getListComp() {
		return listComp;
	}

	public void setListComp(List<ComponentDigest> listComp) {
		this.listComp = listComp;
	}

}
