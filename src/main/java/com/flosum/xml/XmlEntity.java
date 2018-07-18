package com.flosum.xml;

public class XmlEntity {
	private Integer pos;
	private Integer last;
	private Boolean isEmpty;
	private Boolean isSimple;
	private Boolean hasComment;
	private String tagName;
	private String name;
	private String value;
	private String comment;
	
	public XmlEntity(){
		this.comment = "";// comment must not be null
		this.last = -1;
	}

	public XmlEntity(Integer pos, Integer last, String tagName, String name, String value, String comment) {
		this.pos = pos;
		this.last = last;
		this.tagName = tagName;
		this.name = name;
		this.value = value;
		this.comment = comment;
	}

	public Integer getPos() {
		return pos;
	}
	public void setPos(Integer pos) {
		this.pos = pos;
	}
	public Integer getLast() {
		return last;
	}
	public void setLast(Integer last) {
		this.last = last;
	}
	public Boolean getIsEmpty() {
		return value == null;
	}

	public Boolean getIsSimple() {
		return name == null || name.equals("NA");
	}

	public Boolean getHasComment() {
		return comment == null;
	}

	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

}
