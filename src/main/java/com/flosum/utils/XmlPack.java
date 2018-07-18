package com.flosum.utils;

/**
 * A simple class to work with Xml entities
 * 
 */

public class XmlPack {
	private static final String pHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n";
	private static final String pFooter = "</Package>";
	private static final String API_VER = "38.0";

	
	private String xmlPack;

	public XmlPack() {
		xmlPack = pHeader;
	}
	
	public  void openTypesEntry(){
		xmlPack += "<types>";
	}

	public  void closeTypesEntry(){
		xmlPack += "</types>\n";
	}

	public  void createMemberEntry(String entry) {
		xmlPack += "<members>" + entry + "</members>\n";
	}

	public void createNameEntry(String entry) {
		xmlPack += "<name>" + entry + "</name>\n";
	}

	public  void createVersionEntry() {
		xmlPack += "<version>" + API_VER + "</version>\n";
	}
	
	public  void createApiEntry(){
		
	}

	@Override
	public String toString() {
		return xmlPack +pFooter;
	}

}
