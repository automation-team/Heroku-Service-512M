package com.flosum.model;

import java.io.File;


/**
 * Used to describe 1 entity in zip archive with relative name =  zipPath
 * NB: relativePath, absPath relates to real file on disk
 *
 */
public class ZEntry {

	public enum FileType {
		FILE, DIR
	}

	private FileType fileType;
	private String relativePath;
	private File absPath;
	private String zipPath;
	private Long crc32;

	public ZEntry(FileType fileType, String relativePath, File absPath, String zipPath) {
		this.fileType = fileType;
		this.relativePath = relativePath;
		this.absPath = absPath;
		this.zipPath = zipPath;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public File getAbsPath() {
		return absPath;
	}

	public void setAbsPath(File absPath) {
		this.absPath = absPath;
	}

	public Boolean isDirectory() {
		return (fileType == FileType.DIR) ? true : false;
	}

	public String getZipPath() {
		return zipPath;
	}

	public void setZipPath(String zipPath) {
		this.zipPath = zipPath;
	}

	public Long getCrc32() {
		return crc32;
	}

	public void setCrc32(Long crc32) {
		this.crc32 = crc32;
	}

}