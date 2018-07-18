package com.flosum.utils;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Used to extract inner components and hold them as map
 */
public class XmlProcessor {

	private final static  Logger LOG = LoggerFactory.getLogger(XmlProcessor.class);
	
	final static String XMLHEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	final static Integer XMLHEADER_LENGTH = XMLHEADER.length();
	final static String XMLNS_LONG = " xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n";
	final static String XMLNS_LONG_EMPTY = " xmlns=\"http://soap.sforce.com/2006/04/metadata\"/>";
	final static String XMLNS_LONG_EMPTY2 = " xmlns='http://soap.sforce.com/2006/04/metadata'/>";
	final static String XMLNS = " xmlns";
	final static String FULLNAMETAGOPEN = "<fullName>";
	final static String FULLNAMETAGCLOSE = "</fullName>";
	final static Integer R_LEAF = 1;
	final static Integer R_ENUMERATE = 2;
	final static String OFFSETBLOCK = "    "; 

	// main structure to hold extracted inner components
	private Map<String, List<String>> mapTags;
	private Set<String> searchSet; // set of tags which need to be extracted
	private String parentNode;
	private String parentName;

	// contains xml without xml header
	public final String xml;

	public XmlProcessor(String xml,String parentName) {
		this.xml = cutHeader(xml);
		parentNode = getParentNode();
		this.parentName = parentName;
		mapTags = new HashMap<String, List<String>>();
	}

	public XmlProcessor(String xml,String parentName,Set<String> searchSet) {
		this.xml = cutHeader(xml);
		parentNode = getParentNode();
		this.parentName = parentName;
		mapTags = new HashMap<String, List<String>>();
		this.searchSet = searchSet;
	}
	
	public void setSearchSet(Set<String> searchSet){
		this.searchSet = searchSet;
	}
	
	public String getParentName(){
		return this.parentName;
	}
	
	public Map<String, List<String>> getMapTags(){
		return this.mapTags;
	}

	// process node content, extracts inner components defined in searchSet
	public void processNodeContent(Integer start) {
		Boolean isNext = true;
		mapTags.clear();
		if (searchSet == null) return;

		for(String tag: searchSet){
			Integer first = start;
			isNext = true;
			while (isNext) {
				Integer i0 = getTag("<"+tag+">",first);
				Integer i1 = getTag("</"+tag+">",first+1);
				if (i0 > -1 && i1 > -1){// found inner component; empty inner components like <fields/> omitted (2nd if branch )
					if (!mapTags.containsKey(tag)){
						mapTags.put(tag, new ArrayList<String>());
					}
					first = i1+tag.length()+3;// set pointer to next block right after closing tag
					mapTags.get(tag).add(xml.substring(i0,first)); // add to map the whole inner component, including open-close tags
				}else{
					Integer i2 = getTag("<"+tag+"/>",first);
					if (i2  != -1){//empty tag present; omit and continue
						first = i2+tag.length()+3;
					}else{// executed when no inner nether empty tags found - nothing to search 
						isNext = false;
					}
				}
			}
			LOG.info("added inner components {}:{}",tag,mapTags.get(tag) == null ? 0: mapTags.get(tag).size());
		}
	}

	

	public String normalizeXml() {

		if (parentNode == null)
			return XMLHEADER;// return empty xml
		
//		LOG.info("initial xml without head:{}",this.xml);

		if (xml.indexOf(XMLNS_LONG_EMPTY) != -1 || xml.indexOf(XMLNS_LONG_EMPTY2) != -1)// empty xml with /> tag in parent entry
			return xml;
		Integer start = xml.indexOf(">");
		if (start == -1)
			return XMLHEADER;// return empty xml
		String normal = XMLHEADER + "\n" +
						"<" + parentNode + XMLNS_LONG +
						nodeContent(start + 1, parentNode, "", parentNode).xml + 
						"</" + parentNode + ">";
		// remove last \n symbol if exists
		int len = normal.length();
		if (normal.charAt(len - 1) == '\n') {
			return normal.substring(0, len - 1);
		}
		return normal;
	}

	// returns node content
	public Block nodeContent(Integer start, String tagName, String offset, String parentTagName) {
		Integer first = start;
		String xml2return = "";
		Boolean isNext = true;
		Boolean isComplex = false;
		Block r = new Block();

		while (isNext) {
			Node curTag = nodeName(first);// get tag in block
			if (curTag != null) {
				if (curTag.end && curTag.name.equals(tagName)) {
					// found an end  of block, return all block, do not add parent tag
					if (!isComplex) {// case of leaf - returned block consists only form 1 leaf
						if (first != curTag.start)
							xml2return = xml.substring(first, curTag.start);
					}
					r.xml = xml2return;
					r.first = curTag.nextStart; // goto next marker
					return r;
				} else if (isNodeNameEmpty(curTag.name)) {// found an empty tag  like < fog/ >
					isComplex = true;
					r.leaf = false;
					xml2return += offset + "<" + curTag.name + ">\n";
					first = curTag.nextStart; // goto next marker
				} else {// found a non-number tag - process it and add to block
						// under its own name
					isComplex = true;
					r.leaf = false;
					Block subBlock = nodeContent(curTag.nextStart, curTag.name, offset + OFFSETBLOCK, curTag.name);
					if (subBlock != null) {
						if (subBlock.leaf) {
							xml2return += offset + "<" + curTag.name + ">" + subBlock.xml + "</" + curTag.name + ">\n";
						} else {
							if (!subBlock.enumerate) {
								xml2return += 	offset + "<" + curTag.name + ">\n" +
																subBlock.xml +
												offset + "</"+ curTag.name + ">\n";
							} else {
								xml2return += subBlock.xml;
							}
						}
						first = subBlock.first;
					}

				}

			} else {// EOF
				LOG.info("probably wrong xml: error during parsing @ {}",parentTagName);
				isNext = false;// probably wrong xml
			}
		}
		r.xml = xml2return;
		r.first = first;
		return r;
	}

	// returns name for node or undefined if it do not exists within block xml
	public Node nodeName(Integer first) {
		Integer i0 = xml.indexOf("<", first);
		if (i0 == -1)
			return null;
		Integer i1 = xml.indexOf(">", first + 1);
		if (i1 == -1)
			return null;
		String n = xml.substring(i0 + 1, i1);
		if (n == null || n == "")
			return null;
		if (n.charAt(0) == '/') {
			return new Node(i0, i1 + 1, n.substring(1, n.length()), true);
		} else {
			return new Node(i0, i1 + 1, n, false);
		}
	}

	public String getParentNode() {
		// find parent node
		int i0P = xml.indexOf("<");
		if (i0P == -1)
			return null;
		int i1P = xml.indexOf(XMLNS);
		if (i1P == -1)
			return null;// if xml empty, return null
		return xml.substring(i0P + 1, i1P).trim();
	}
	
	public Integer getTag(String tag,Integer first){
		return xml.indexOf(tag, first);
	}

	/**
	 * Utility methods
	 */

	// verify node for emptiness
	// return true, if name like fog/ in tag <fog/> NB: str must not be a null
	public static Boolean isNodeNameEmpty(String str) {
		if (str.charAt(str.length() - 1) == '/')
			return true;
		return false;
	}

	public static String cutHeader(String xml) {
		if (xml.indexOf(XMLHEADER) == 0) {
			return xml.substring(XMLHEADER_LENGTH);
		} else {
			return xml;
		}
	}

	public static String getXmlMainBody(String tmpXml) {
		int i2P = tmpXml.indexOf(">");
		if (i2P > -1) {
			return tmpXml.substring(i2P + 1);
		}
		return tmpXml;
	}
	
	// extracts name from string like <fullName>name</fullName>
	public static String extractName(String xmlBody){
		int i0P = xmlBody.indexOf(FULLNAMETAGOPEN);
		if (i0P == -1) return null;
		int i1P = xmlBody.indexOf(FULLNAMETAGCLOSE);
		if (i1P == -1) return null;
		String name = xmlBody.substring(i0P+10, i1P).trim();
		return name;
	}

	/**
	 * Inner classes
	 */
	class Node {

		public Node(Integer start, Integer nextStart, String name, Boolean end) {
			this.start = start;
			this.nextStart = nextStart;
			this.name = name;
			this.end = end;
		}

		public Integer start;
		public Boolean end;
		public String name;
		public Integer nextStart;
	}

	class Block {

		Block() {
			leaf = true;// by default set true - i.e. if not changed block will be a leaf
			enumerate = false;
		}

		public Boolean leaf;
		public Boolean enumerate;
		public String xml;
		public Integer first;
	}

}
