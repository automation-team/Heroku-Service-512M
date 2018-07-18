package com.flosum.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;

import com.flosum.constants.CompType;

public class MergeUtils {
	public static String EMPTYSTRING = "";
	
	public static String sha1(final String file){
		try{
			final MessageDigest messageDigest = MessageDigest.getInstance("SHA1");

			messageDigest.update(file.getBytes());

			// Convert the byte to hex format
			try (Formatter formatter = new Formatter()) {
				for (final byte b : messageDigest.digest()) {
					formatter.format("%02x", b);
				}
				return formatter.toString();
			}
		}catch(NoSuchAlgorithmException e){
			return "0";
		}
	}

	public static String insertChild(String parentXml, String childXml){
		return null;
	}
	
	public static void addArray(List<String> lsAddTo, List<String> lsAddFrom){
		for(String s:lsAddFrom){
			lsAddTo.add(s);
		}
	}
	
	/**
	 *  For each string in list: add as inner component
	 *  each string must be cut off to remove header and inner tags
	 */
	public static String mergeInnerComponents(String compType, List<String> lst){
		XmlInnerComponent xio = new XmlInnerComponent(compType,CompType.getParentType(compType));// use map f.e. CustomField => CustomObject
		for(String str: lst){
			xio.addEntry(getBareComponentContext(str,CompType.getInnerTag(compType),0));
		}
		return xio.toString();
	}
	
	public static String getBareComponentContext(String xml, String tag, Integer first){
		if (xml == null || xml == "") return EMPTYSTRING;
		Integer startIdx =  xml.indexOf("<"+ tag + ">", first);
		if (startIdx == -1) return EMPTYSTRING;
		Integer endIdx =  xml.indexOf("</" + tag + ">", first);
		if (endIdx == -1) return EMPTYSTRING;
		return xml.substring(startIdx, endIdx+tag.length()+3);
	}


}
