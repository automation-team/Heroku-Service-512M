package com.flosum.utils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.flosum.constants.CompType;

/**
 * A simple utilities class to work with path strings
 * 
 */

public class StringUtils {
	
	private static final char INNER_TOKEN = '#';
	private static final char FILE_SEP = File.separatorChar;
	private static final String AURA_TOKEN = "aura" + FILE_SEP;
	private static final String DOCUMENT_TOKEN = "documents" + FILE_SEP;
	private static final String META_TOKEN = "-meta.xml";
	

	private static final Map<String, String> FOLDERTYPES = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put("documents","DocumentFolder");
			put("email","EmailFolder");
			put("dashboards","DashboardFolder");
			put("reports","ReportFolder");
		}
	});
	
	
	public static String getRelativePath(final String dir, final String path){
		if (path == null || path == "") return path;
		if (path.indexOf(dir) > -1){
			return path.substring(path.indexOf(dir) + dir.length() + 1);
		}else{
			return path;
		}
	}


	//NB: dir must be a key of FOLDERTYPES
	public static String getFolderType(String dir) {
		return FOLDERTYPES.get(dir);
	}

	// extract directory name from path, f.e. : classes/file.cls -> classes,
	// aura/component/c.js ->aura/component
	public static String getDir(String name) {
		int s = name.lastIndexOf(FILE_SEP);
		return s == -1 ? null : name.substring(0, s);
	}

	// extract file name from path, f.e. : classes/file.cls -> file.cls
	public static String getName(String name) {
		int s = name.lastIndexOf(FILE_SEP);
		return s == -1 ? name : name.substring(s + 1);
	}
	
	// extract ext name from path, f.e. : classes/file.cls -> .cls
	public static String getExt(String name) {
		if (name == null) return null;
		int s = name.lastIndexOf('.');
		return s == -1 ? null : s == 0 ? name : name.substring(s);
	}


	// extract file name from path without extension, f.e. : classes/file.cls ->
	// file
	// f.e. for aura return dir name: aura/TestComp/TestComp.aura -> TestComp
	// OR: aura/TestComp -> TestComp
	// OR: cases/Name.dots.dty -> Name.dots 
	// for *-meta.xml files return name until 1st dot: file.cls-meta.xml ->file
	public static String getBareName(String name) {
		if (name.indexOf(AURA_TOKEN) != -1)
			return secondName(name);
		int s0 = name.lastIndexOf(FILE_SEP);
		String s = (s0 == -1) ? name : name.substring(s0 + 1);// get partial name after slash
		if (s.indexOf(META_TOKEN) != -1) {
			String bareName = s.replace(META_TOKEN, "");
			if (bareName == null || bareName == "") return "";
			int s1 = bareName.lastIndexOf('.');
			return s1 == -1 ? bareName : bareName.substring(0, s1);
		} else {
			int s1 = s.lastIndexOf('.');
			return s1 == -1 ? s : s.substring(0, s1);
		}
	}
	
	// extract aura main name from path, f.e. :
	// case 1 : aura/TestComp/TestComp.aura -> TestComp
	// case 2 : aura/TestComp -> TestComp
	public static String secondName(String s) {
		int s0 = s.indexOf(FILE_SEP);
		int s1 = s.lastIndexOf(FILE_SEP);
		if (s0 != -1 && s1 != -1 ){
			if (s0 != s1) 	return s.substring(s0 + 1, s1);//1
			if (s0 == s1) 	return s.substring(s0 + 1);//2
		}
		return "";
	}
	
	public static String getCompDir(String path) {
		int i = path.indexOf(FILE_SEP);
		if (i < 0)
			return "";
		return path.substring(0, i); // cut comp dir name from path's string
	}

	// cut off extension and get pure name
	public static String getCompName(String fullName) {
		String name =  cutMetaExt(fullName);
		int i = name.lastIndexOf(".");
		if (i < 0)
			return name;
		return name.substring(0, i); // cut comp name from path's string
	}
	
	// parse path to extract aura's component name; if such a name does not exist, returns null
	// path always must have the following form: aura/test
	// return true also for all sub components
	public static Boolean isAuraComp(String path){
		// if not aura path, return false
		return (path.indexOf(AURA_TOKEN) == -1)? false: true;

	}
	

	public static Boolean isDocumentComp(String path){
	
		return (path.indexOf(DOCUMENT_TOKEN) == -1)? false: true;

	}
	
	public static String getDocumentFolderName(String path){
		int s0 = path.indexOf(FILE_SEP);  
		int s1 = path.indexOf("-meta");
		if (s0 != -1 && s1 != -1){
			return path.substring(s0 + 1, s1);
		}
		return "";
	}

	public static String getFolderName(String path){
		int s0 = path.indexOf(FILE_SEP);
		int s1 = path.indexOf("-meta");
		if (s0 == -1 || s1 == -1) return null;
		// extract dir name - the 1st part until /
		String directory = path.substring(0,s0);
		if (!FOLDERTYPES.containsKey(directory)) return null;
		String folder = path.substring(s0+1,s1);
		if (folder == "" || folder == null || folder.indexOf(FILE_SEP) !=  -1) return null;// name MUST not contain slash
		return folder;
	}

	public static Boolean isDocumentFolderComp(String path){
		// if not aura path, return
		int s0 = path.indexOf(DOCUMENT_TOKEN);  
		if  ( path.indexOf(DOCUMENT_TOKEN) == -1){
			return false;
		}else {
			String folder = path.replace(DOCUMENT_TOKEN, "");
			if (!isMetaFile(folder)) return false;
			if (folder == null || folder == "") return false;
			if (folder.indexOf(FILE_SEP) !=  -1) return false;
			return true;
		}
	}
// returns:
	//  documents/AccountingFolder/Logo.gif -> AccountingFolder/Logo.gif 
	// for meta returns the same name 
	public static String getDocComplexName(String docPath) {
		String path = cutMetaExt(docPath);
		int s0 = path.indexOf(FILE_SEP);
		if (s0 != -1) {
			return path.substring(s0 + 1);
		}
		return null;
	}

	public static String getCompComplexName(String complexPath) {
		String path = cutMetaExt(complexPath);
		int s0 = path.indexOf(FILE_SEP);
		int s1 = path.lastIndexOf('.');
		if (s0 != -1 && s1 != -1) {
			return path.substring(s0 + 1, s1);
		}
		return null;
	}
	
	public static Boolean isMetaFile(String s) {
		if (s == null || s == "") return false;
		if (s.indexOf("-meta") >= 0) {
			return true;
		}
		return false;
	}
	
	public static String cutMetaExt(String path){
		return isMetaFile(path)? path.substring(0, path.indexOf(META_TOKEN)):path;
	}

	//returns inner comp standard name if extension is appropriate
	// task.object#field1.field -> task.field1
	public static String isInnerComp(final String s) {
		
		int s1 = s.indexOf(INNER_TOKEN);
		if (s1 == -1) return null;
		int s2 = s.indexOf('.');
		int s3 = s.lastIndexOf('.');
		if (s2  == -1 || s3  == -1 || s2 == s3) return null;
		return s.substring(0, s2+1) + s.substring(s1+1,s3);
	}
	
	// returns Account of Account.QAID__c 
	public static String getParentName(String s) {
		int s1 = s.indexOf('.');
		if (s1 < 0) return s;
		return s.substring(0, s1);
	}
	
	// returns QAID__c of Account.QAID__c 
	public static String getInnerName(String s) {
		int s1 = s.indexOf('.');
		if (s1 < 0) return s;
		return s.substring(s1+1);
	}
	
	// transform task.field1 -> task.object#field1.field
	public static String composeInnerName(String masterName, String innerName, String type){
		return masterName + INNER_TOKEN + getInnerName(innerName) + CompType.getExt(type);
	}
	
	public static String format(String s ){
		return (s == "")?"":INNER_TOKEN+s;
	}
	
	/**
	 * Utility method 
	 * @param type
	 * @param name
	 * @return composite name in  the form:  type#name
	 */
	public static String compositeName(String type, String name){
		return type + '#' + name;
	}
	
}
