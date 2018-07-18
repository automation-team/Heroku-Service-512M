package com.flosum.dao;

import java.util.List;
import java.util.Map;

public class PackageGenerator {
	private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n";
	private static final String FOOTER = "</Package>";
	private static final String TYPETAG = "types";
	private static final String NAMETAG = "name";
	private static final String MEMBERTAG = "members";
	private static final String VERSIONTAG = "version";
	
	private String packageXml;
	private final Map<String, List<String>> map;
	private final String VERSION;
	private Boolean isComplete = false;
	
	public PackageGenerator(Map<String, List<String>> map, String version){
		packageXml = HEADER;
		this.map = map;
		this.VERSION = version;
	}
	
	public void createPackageManifest(){
		if (map == null || map.size() == 0) return;
		for (String name : map.keySet()) {
			List<String> members = map.get(name);
			if (members != null && members.size() > 0){
				packageXml += "<types>\n";
				for (String member: members){
					packageXml += addNode(member,MEMBERTAG);
				}	
				packageXml += addNode(name,NAMETAG);
				packageXml += "</types>\n";
			}
		}
	}

	
	public String addNode(String value,String tag){
		if (value == null || value == "") return "";
		return "<" + tag + ">" + value + "</" + tag + ">\n";
	}
	
	public String getPackage(){
		if (!isComplete){
			packageXml += addNode(VERSION,VERSIONTAG);
			packageXml += FOOTER;
			isComplete = true;
		}
		return packageXml;
	}

}
