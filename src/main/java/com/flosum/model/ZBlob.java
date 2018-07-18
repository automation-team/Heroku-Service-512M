package com.flosum.model;

import java.io.File;

/**
 * Used to describe 1 entity in zip archive with relative name =  zipPath
 * NB: blob is a data to save
 *
 */


public class ZBlob {

	public enum FileType {
		FILE, DIR
	}

	private FileType fileType;
	private File absPath;
	private File dir;
	private String blob;

	public ZBlob(FileType fileType, File absPath, File dir, String blob) {
		this.fileType = fileType;
		this.absPath = absPath;
		this.dir = dir;
		this.blob = blob;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public File getAbsPath() {
		return absPath;
	}

	public void setAbsPath(File absPath) {
		this.absPath = absPath;
	}

	public String getBlob() {
		return blob;
	}

	public void setBlob(String blob) {
		this.blob = blob;
	}

	public Boolean isDirectory() {
		return (fileType == FileType.DIR) ? true : false;
	}

	public File getDir() {
		return dir;
	}

	public void setDir(File dir) {
		this.dir = dir;
	}

}