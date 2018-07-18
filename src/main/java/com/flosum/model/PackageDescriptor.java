package com.flosum.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PackageDescriptor implements Serializable {

	private static final long serialVersionUID = 8L;

	private File localDir;
	private Integer totalSize = 0;
	private Integer totalSizeBytes = 0;
	private Map<String, List<String>> packageMap;// name => list of members 
	private List<File> files;
	private File packageFile;

	public PackageDescriptor() {
	}

	public PackageDescriptor(File localDir) {
		this.localDir = localDir;
		this.totalSizeBytes = 0;
		this.totalSize = 0;
		this.packageMap = new HashMap<String, List<String>>();
		this.files = new ArrayList<File>();
		this.packageFile = null;
	}
	
	public void addMember(String name, String member){
		List<String> members;
		if (!packageMap.containsKey(name)){
			packageMap.put(name, new ArrayList<String>());
		}
		members = packageMap.get(name);
		members.add(member);
		totalSize ++;
	}

	public void addMembers(String name, List<String> membersToAdd){
		if (membersToAdd == null || membersToAdd.size() == 0) return;
		List<String> members;
		if (!packageMap.containsKey(name)){
			packageMap.put(name, new ArrayList<String>());
		}
		members = packageMap.get(name);
		members.addAll(membersToAdd);
		totalSize += membersToAdd.size();
	}
	
	public void addFiles(List<File> lst){
		files.addAll(lst);
	}

	
	public Set<String> getTypes(){
		return packageMap.keySet();
	}

	public List<String> getMembers(String type){
		if (packageMap.containsKey(type)){
			return packageMap.get(type);
		}
		return new ArrayList<String>();
	}

	
	public File getLocalDir() {
		return localDir;
	}

	public void setLocalDir(File localDir) {
		this.localDir = localDir;
	}

	public Integer getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(Integer totalSize) {
		this.totalSize = totalSize;
	}

	public Integer getTotalSizeBytes() {
		return totalSizeBytes;
	}

	public void setTotalSizeBytes(Integer totalSizeBytes) {
		this.totalSizeBytes = totalSizeBytes;
	}

	public Map<String, List<String>> getPackageMap() {
		return packageMap;
	}

	public void setPackageMap(Map<String, List<String>> packageMap) {
		this.packageMap = packageMap;
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public File getPackageFile() {
		return packageFile;
	}

	public void setPackageFile(File packageFile) {
		this.packageFile = packageFile;
	}


}