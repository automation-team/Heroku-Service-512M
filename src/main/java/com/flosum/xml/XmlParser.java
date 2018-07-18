package com.flosum.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlParser {
	private final static String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	private final static String NS_SECTION_1 = " xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n";
	private final static String NS_SECTION_2 = " xmlns=\"http://soap.sforce.com/2006/04/metadata\"/>";
	private final static String OPEN_TAG = "<";
	private final static String CLOSE_TAG = ">";
	private final static String OPEN_TAG_CL = "</";
	private final static String EMPTY_CLOSE_TAG = "/>";
	private final static String EOL = "\n";
	private final static String COMMENT_OPEN_TAG = "<!--";
	private final static String COMMENT_CLOSE_TAG = "-->";
	private final static String OFFSETBLOCK = "    ";
	private final static String NONAME = "NA";
	private final static Boolean SORT = true;
	
	private static final Map<String, String> TAG2NAME = Collections.unmodifiableMap(new HashMap<String, String>() {
		private static final long serialVersionUID = -1018304490248603383L;

		{
			//(13)
			put("actionOverrides","actionName");
			put("businessProcesses","fullName");
			put("compactLayouts","fullName");
			put("fields","fullName");
			put("fieldSets","fullName");
			put("historyRetentionPolicy","fullName");
			put("listViews","fullName");
			put("recordTypes","fullName");
			put("searchLayouts","fullName");
			put("sharingReasons","fullName");
			put("sharingRecalculations","fullName");
			put("validationRules","fullName");
			put("webLinks","fullName");
			//(3)

			put("sharingCriteriaRules","fullName");
			put("sharingOwnerRules","fullName");
			put("sharingTerritoryRules","fullName");
			//(5)
			
			put("alerts","fullName");
			put("fieldUpdates","fullName");
			put("outboundMessages","fullName");
			put("rules","fullName");
			put("tasks","fullName");
			//(1)
			
			put("assignmentRule","fullName");
			//(1)

			put("escalationRule","fullName");
			//(1)

			put("labels","fullName");
		}
	});

	
	private final static  Logger LOG = LoggerFactory.getLogger(XmlParser.class);


	private final String ROOT_TAG_OPEN;
	private final String ROOT_TAG_CLOSE;
	private final String type;

	private String ns_section;
	private String originalXml;
	private String composedXml;

	private Map<String,Map<String,XmlEntity>> tagsMap;
	
	public XmlParser(String type){
		ROOT_TAG_OPEN = OPEN_TAG + type;
		ROOT_TAG_CLOSE = OPEN_TAG_CL + type + CLOSE_TAG;
		tagsMap = new HashMap<String,Map<String,XmlEntity>>();
		ns_section = NS_SECTION_2;
		this.type = type;
	}
	
	// add to tagsMap all 1st level xml-entities
	public void extractTags(){
//		LOG.info("extractTags: {}",ROOT_TAG_OPEN);
//		LOG.info("originalXml: {}",originalXml);
		if (originalXml == null || originalXml == "") return;
		Integer from = findTheBeginning();
//		LOG.info("from={}",from);
		if (from == -1) return;
		
		while(from > -1){
			XmlEntity entity = findTag(from);
			if (entity != null){
				if (entity.getLast() == -1) return;
//				LOG.info("-----------");
//				LOG.info("fullname:{}",entity.getName());
//				LOG.info("tag:{}",entity.getTagName());
//				LOG.info("value:{}",entity.getValue());
//				LOG.info("isEmpty? {}",entity.getIsEmpty());
//				LOG.info("simple? {}",entity.getIsSimple());
				String tagName = entity.getTagName();
				if (!tagName.contains(type) && !tagName.contains("/")){
					if (!tagsMap.containsKey(tagName)){
						tagsMap.put(tagName, new HashMap<String,XmlEntity>());
					}
					tagsMap.get(tagName).put(entity.getName(), entity);
					from = entity.getLast();
				}else{// do not add last tag
					from = -1;
				}
			}else{
//				LOG.info("entity is null!");
				from = -1;
			}
		}
//		LOG.info("final size:{}",tagsMap.size());
	}
	
	// find the 1st level xml-entity starting from: from
	private  XmlEntity findTag(Integer from){
		if (originalXml.length() < from) return null;
		XmlEntity entity = new XmlEntity();
		while(from > 0){
			// find the start
			Integer start = findSymbolFrom(OPEN_TAG,from);
			entity.setPos(start);
			if (isComment(start)){
				// if equal, extract comment and go further
				from = findSymbolFrom(COMMENT_CLOSE_TAG,from);
				if (from > 0){
					String comment = entity.getComment() +  COMMENT_OPEN_TAG + originalXml.substring(start + 1, from + 1) + COMMENT_CLOSE_TAG + EOL;
					entity.setComment(comment);
				}
			}else{// not comment - get tagName, value, fullName
				from = findSymbolFrom(CLOSE_TAG,start);
				if (from > 0){
					String tag;
					if (isEmpty(from)){
						tag = getEmptyTagName(start + 1,from);
						// do not set value for empty tag
						entity.setLast(start + tag.length() + 3);
					}else{
						tag = getTagName(start + 1,from);
						String block = getBlock(start,tag);// always not null
						String fullName = getFullName(tag,block);
						Integer last = start + block.length();
						if (start.equals(last)) last += tag.length();
						entity.setLast(last);
						entity.setName(fullName);// if empty then "" or null
						entity.setValue(block);
					}
					entity.setTagName(tag);
					from = -1;// exit cycle
				}
			}
		}
		return entity;
	}

	// input: always NOT null: ROOT_TAG & originalXml
	// find the 0-level root tag: 
	// return -1 if xml not valid OR empty
	// else return the beginning of analyzed area 
	// set the ns_section to namespace string from xml
	private  Integer findTheBeginning(){
		Integer start = originalXml.indexOf(ROOT_TAG_OPEN); 
		if (start  < 0){
			return -1;
		}
		start += ROOT_TAG_OPEN.length();
		// look for closing tag from start position and cut this as ns_section
		Integer end = findSymbolFrom(CLOSE_TAG,start);
		if (end  < 0){
			return -1; // wrong xml
		}
		ns_section = originalXml.substring(start + 1, end + 1) + EOL; 
//		LOG.info("ns_section={}",ns_section);
		return end + 1;
	}
	
	private Integer findSymbolFrom(String sym, Integer f){
		return originalXml.indexOf(sym, f);
	}
	
	private Boolean isComment(Integer f){
		String piece = originalXml.substring(f, f + COMMENT_OPEN_TAG.length());
		return COMMENT_OPEN_TAG.equals(piece);
	}

	private Boolean isEmpty(Integer f){
		String piece = originalXml.substring(f - 1 , f + 1);
		return EMPTY_CLOSE_TAG.equals(piece);
	}
	
	private String getTagName(Integer start, Integer end){
		return originalXml.substring(start, end);
	}

	private String getEmptyTagName(Integer start, Integer end){
		return originalXml.substring(start, end - 1);// without '/'
	}

	private String getBlock(Integer start, String tag){
		if (tag == null || tag == "") return "";
		Integer from = originalXml.indexOf(OPEN_TAG + tag + CLOSE_TAG,start);
		if (from < 0) return "";
		Integer to = originalXml.indexOf(OPEN_TAG_CL + tag + CLOSE_TAG,start);
		if (to < 0) return "";
		return originalXml.substring(from, to + tag.length() + 3);
	}

	private String getFullName(String tag, String block){
		if (block == null || block == "") return NONAME;
		if (!TAG2NAME.containsKey(tag)) return NONAME;
		String nameTag = TAG2NAME.get(tag);
		Integer from = block.indexOf(OPEN_TAG + nameTag + CLOSE_TAG);
		if (from < 0) return NONAME;
		Integer to = block.indexOf(OPEN_TAG_CL + nameTag + CLOSE_TAG);
		if (to < 0) return NONAME;
		return block.substring(from + nameTag.length() + 2, to).trim();
	}

	public String compileXml(){
//		LOG.info("compile nodes:{}",tagsMap.size());
		composedXml = HEADER + ROOT_TAG_OPEN + ns_section;
//		LOG.info("header:{}",composedXml);
		if (tagsMap.isEmpty()) return composedXml;
		List<String> allTags = new ArrayList(tagsMap.keySet());
		if (SORT){
			Collections.sort(allTags);//
		}
//		LOG.info("tags:{}",allTags.size());
		for (Integer i = 0; i < allTags.size(); i++){
			String tag = allTags.get(i);
//			LOG.info("tag:{}",tag);
			Map<String,XmlEntity> map = tagsMap.get(tag);
			if (map.isEmpty()) continue;
			List<String> allBlocks = new ArrayList(map.keySet());
			if (SORT){
				Collections.sort(allBlocks);//
			}
//			LOG.info("blocks:{}",allBlocks.size());
			for (Integer j = 0; j < allBlocks.size(); j++){
				String fullName = allBlocks.get(j);
				XmlEntity entity = map.get(fullName);
				if (entity.getHasComment()){
					composedXml += (OFFSETBLOCK + entity.getComment() + EOL);
				}
				if (entity.getIsEmpty()){
					composedXml += (OFFSETBLOCK + OPEN_TAG + entity.getTagName() + EMPTY_CLOSE_TAG + EOL);
				}else{
					composedXml += (OFFSETBLOCK + entity.getValue() + EOL);
				}
			}			
		}
		// add the footer
		composedXml += ROOT_TAG_CLOSE;
//		LOG.info("composedXml:{}",composedXml);
		return composedXml;
	}

	public String getOriginalXml() {
		return originalXml;
	}

	public void setOriginalXml(String originalXml) {
		this.originalXml = originalXml;
	}

	public String getComposedXml() {
		return composedXml;
	}

	public void setComposedXml(String composedXml) {
		this.composedXml = composedXml;
	}

	public Map<String, Map<String, XmlEntity>> getTagsMap() {
		return tagsMap;
	}

	public void setTagsMap(Map<String, Map<String, XmlEntity>> tagsMap) {
		this.tagsMap = tagsMap;
	}
	
	
	
}
