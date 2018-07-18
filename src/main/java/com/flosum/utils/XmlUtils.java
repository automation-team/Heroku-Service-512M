package com.flosum.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlUtils {
	final static  String XMLHEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	final static Integer XMLHEADER_LENGTH = XMLHEADER.length();
	final static  String XMLNS_LONG = " xmlns=\"http://soap.sforce.com/2006/04/metadata\">";
	final static  String XMLNS = " xmlns";
	final static  String MEMBERSTAGOPEN = "<members>";
	final static  String MEMBERSTAGCLOSE = "</members>";
	final static  String NAMETAGOPEN = "<name>";
	final static  String NAMETAGCLOSE = "</name>";

	// return map of all members from package.xml like:
	// Account						=> [QAID__c]
	// Custom_object_for_Flow__c		=> [LocalFlosum__CRC32__c]
	// values in list define the section with value <fullName>QAID__c</fullName> in file
	public static Map<String,List<String>> getAllMembers(final String xmlPack){
		Map<String,List<String>> mapMembers = new HashMap<String,List<String>>(); 
		// iterate over all 
		Integer first = 0;
		Boolean isNext = true;
		
		while (isNext) {
			Integer i0 = xmlPack.indexOf(MEMBERSTAGOPEN,first);
			Integer i1 = xmlPack.indexOf(MEMBERSTAGCLOSE,first+1);
			if (i0 > -1 && i1 > -1){// found member entry
				String member = xmlPack.substring(i0+9, i1).trim();
				String parentName = StringUtils.getParentName(member);
				if (!mapMembers.containsKey(parentName)){
					mapMembers.put(parentName, new ArrayList<String>());
				}
				first = i1+10;
				mapMembers.get(parentName).add(StringUtils.getInnerName(member));
			}else{
				isNext = false;
			}
		}
		return mapMembers;
	}
	

}
