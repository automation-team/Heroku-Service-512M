package com.flosum.utils;

import com.flosum.constants.CompType;

/**
 * A simple class to work with Xml entities
 * Form xml like:
 * <?xml version="1.0" encoding="UTF-8"?>
 * <CustomObject xmlns="http://soap.sforce.com/2006/04/metadata">
 *   <fields> <- entry starts
 *       <fullName>addit__Amount__c</fullName>
 *       <type>Currency</type>
 *   </fields> <- entry ends
 * </CustomObject>
 * here: 
 * parentType = CustomObject
 * innerTag = fields
 * innerType = CustomField (used map from class CompType)
 * 
 */

public class XmlInnerComponent {
	private static final String pHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	private static final String	XMLNS = " xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n";

	
	private String xmlPack;
	private final String  innerTag;
	private final String  openInnerTag;
	private final String  closeInnerTag;
	private final String  parentType;
	private final String  pFooter;

	public XmlInnerComponent(String innerType, String parentType) {
		xmlPack = pHeader + "<"+ parentType + XMLNS;
		pFooter = "</"+parentType+">";
		this.innerTag = CompType.getInnerTag(innerType); // get from map CustomField => fields
		this.openInnerTag = "<"+innerTag+">";
		this.closeInnerTag = "</"+innerTag+">";
		this.parentType = parentType;
	}
	
	// creates entry withing tags
	public  void createEntry(String entry) {
		xmlPack += openInnerTag + entry + closeInnerTag;
	}

	// creates entry without tags
	// add only block defined by innerTag
	public  void addEntry(String entry) {
		xmlPack +=  entry + "\n" ;
	}

	
	@Override
	public String toString() {
		return xmlPack +pFooter;
	}

}
