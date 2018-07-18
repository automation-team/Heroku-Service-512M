package com.flosum.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReportGenerator {
	private final String REVIEW_STR = " were reviewed on ";
	private final String compType;
	private String report = "";
	private Map<String,String> reviewMap;
	
	public ReportGenerator(String compType){
		reviewMap = new HashMap<String,String>();
		this.compType = compType;
	}
	
	public void addResult(String compName, String result){
		reviewMap.putIfAbsent(compName, result);
	}
	
	public String genReport(){
		Date d = new Date();
		report += reviewMap.size() + " component(s) of type " + compType + REVIEW_STR + d.toString() + "\n";
		for (String name : reviewMap.keySet()){
			report += name + "\n";
			report += reviewMap.get(name) + "\n";
		}
		return report;
	}

}
