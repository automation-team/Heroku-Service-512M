package com.flosum.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.zip.ZipEntry;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.CompType;
import com.flosum.model.*;
import com.flosum.xml.XmlPreprocessor;

/**
 *	Class to work with zip-packages 
 * 
 *
 */

public class ZipUtils {

	private static final Logger LOG = LoggerFactory.getLogger(ZipUtils.class);
	private static final int BUFFER_SIZE = 16384;
	private static final String PACKAGE_NAME = "package.xml";
	// define which files do not needed to write to package
	private static final Set<String> OMITFILES;
	static {
		OMITFILES = new HashSet<String>();
		OMITFILES.add(PACKAGE_NAME);
	}

	/**
	 * Creates a zip archive from files described in list of file descriptors
	 * Typical zip includes 1 folder with files + 1 file - manifest - in  root directory
	 * Path for package.xml added automatically
	 * Returns a base64 enc string  
	 */

	public static void createZipPack(List<ZEntry> files2pack, MetaItem comp)
			throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (ZipOutputStream zos = new ZipOutputStream(baos)) {
			for (ZEntry file : files2pack) {
				if (file.isDirectory()) {
					// add Dir To Zip
					ZipEntry ze = new ZipEntry(file.getZipPath());
					zos.putNextEntry(ze);
//					LOG.info("zip dir:{}", file.getZipPath());
				} else {
					byte[] buf = new byte[BUFFER_SIZE];

					int len;
					try (FileInputStream in = new FileInputStream(file.getAbsPath())) {
						ZipEntry ze = new ZipEntry(file.getZipPath());
						zos.putNextEntry(ze);
//						LOG.info("zip file:{}, crc before:{}", file.getZipPath(),ze.getCrc());
						ByteArrayOutputStream memoryBuf = new ByteArrayOutputStream(); 
						while ((len = in.read(buf)) != -1){
							memoryBuf.write(buf, 0, len);
						}

//						CRC32 zipEntryCrc = new CRC32(); 
						byte[] sZipData = memoryBuf.toByteArray();
						zos.write(sZipData, 0, memoryBuf.size());
						Long crc32 = SystemUtils.crc32(sZipData, 32);
//						zipEntryCrc.update(sZipData); 
//						LOG.info("crc after:{}",zipEntryCrc.getValue());
//						ze.setCrc(crc32);
						file.setCrc32(crc32);
					}
				}
			}
			zos.close();
			comp.setDataStr(Base64.getEncoder().encodeToString(baos.toByteArray()));
		}
	}

	/**
	 * Creates a zip archive from files described in list of file descriptors
	 * Typical zip includes 1 folder with files + 1 file - manifest - in  root directory
	 * Path for package.xml added automatically
	 * Returns a base64 enc string  
	 */

	public static byte[] createZipPack(List<File> files, final String dir)
			throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (ZipOutputStream zos = new ZipOutputStream(baos)) {
			Set<File> saved = new HashSet<File>();
			Set<String> savedDirs = new HashSet<String>();
			for(File file : files){
				if (saved.contains(file)) continue;
				saved.add(file);
				String tmpPath = file.toString();
				String path = StringUtils.getRelativePath(dir, tmpPath);
				int directory = path.lastIndexOf(File.separatorChar); 
				if (directory > -1){
					String dirPath = path.substring(0, directory + 1);
//					LOG.info("zip path:{}", dirPath);
					if (!savedDirs.contains(dirPath)){
						savedDirs.add(dirPath);
						ZipEntry ze = new ZipEntry(dirPath);
						zos.putNextEntry(ze);
					}
				}
//				if (PACKAGE_NAME.equals(path)) path = File.separatorChar + path; 
//				LOG.info("zip path:{}", path);
				if (file.isDirectory()) {
					// add Dir To Zip
					ZipEntry ze = new ZipEntry(path);
					zos.putNextEntry(ze);
				} else {
					byte[] buf = new byte[BUFFER_SIZE];

					int len;
					try (FileInputStream in = new FileInputStream(file)) {
						ZipEntry ze = new ZipEntry(path);
						zos.putNextEntry(ze);
						ByteArrayOutputStream memoryBuf = new ByteArrayOutputStream(); 
						while ((len = in.read(buf)) != -1){
							memoryBuf.write(buf, 0, len);
						}

						byte[] sZipData = memoryBuf.toByteArray();
						zos.write(sZipData, 0, memoryBuf.size());
					}
				}
			}
			zos.close();
			return baos.toByteArray();
		}
	}
	
	
	/**
	 * Unpacks a zip archive to fs preserving directory structure. 
	 * All files  with the same name will be rewritten. 
	 * Typical zip includes 1 folder with files + 1 file in root directory
	 * Hierarchy of folders created  automatically
	 * NB: if innerName specified, unpack zip with name replacing 
	 */
	public static File unpackZip(final String zip, File outdir, List<File> files) throws IOException, UnsupportedEncodingException {

		byte[] base64decodedBytes = Base64.getDecoder().decode(zip);
		File packageFile = null;
		// LOG.info("zip:{}", base64decodedBytes);
		try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(base64decodedBytes))) {
			ZipEntry entry;
			String name, dir, superDir;
			String sha = "";
			while ((entry = zin.getNextEntry()) != null) {// unpack all entries
				// name = objects/DL_104_1__c.object
				name = entry.getName();
				if (entry.isDirectory()) {
//					LOG.info("create empty dir:{}", name);
					File f = mkdirs(outdir, name);
					files.add(f);
					continue;
				}
				/*
				 * needed if directory comes after file: /dir/foo.txt /dir/
				 */
//				LOG.info("write file:{}", name);
				// dir = objects/DL_104_1__c.object -> objects 
				dir = StringUtils.getDir(name);
//				LOG.info("to directory:{}", dir);
				if (dir != null) {
					// verify, is another sublevel exist (f.e. in aura case)
					superDir = StringUtils.getDir(dir);
					if (superDir != null) {
						mkdirs(outdir, superDir);
					}
					mkdirs(outdir, dir);
				}
				String fname = StringUtils.getName(name);
				File f = extractFile(zin, outdir, name);
				if (!OMITFILES.contains(fname)) {
					files.add(f);
//					LOG.info("unpack file:{}, name:{}", name, fname);
				}else{
					packageFile = f;
				}
			}
			zin.close();
		}
		return packageFile;
	}



	/**
	 * Used to create a pack from inner components which are saved in memory
	 * Creates a zip archive with 1 folder with 1 file + 1 file in
	 * root directory
	 */

	public static void createInnerZipPack(MetaItem comp, String zEntry, File file)
			throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (ZipOutputStream zos = new ZipOutputStream(baos)) {

			byte[] buf = new byte[BUFFER_SIZE];

			int len;
			try (FileInputStream in = new FileInputStream(file)) {
				ZipEntry ze = new ZipEntry(zEntry);
				zos.putNextEntry(ze);
				ByteArrayOutputStream memoryBuf = new ByteArrayOutputStream(); 
				while ((len = in.read(buf)) != -1){
					memoryBuf.write(buf, 0, len);
				}

//				CRC32 zipEntryCrc = new CRC32(); 
				byte[] sZipData = memoryBuf.toByteArray();
				zos.write(sZipData, 0, memoryBuf.size());
				Long crc32 = SystemUtils.crc32(sZipData, 32);
//				zipEntryCrc.update(sZipData); 
//				LOG.info("crc after:{}",zipEntryCrc.getValue());
//				ze.setCrc(crc32);
				comp.setCrc32(crc32);
			}
			zos.close();
		}
		comp.setDataStr(Base64.getEncoder().encodeToString(baos.toByteArray()));
	}

	/**
	 * Creates a zip archive for general string with path =  name. 
	 * Used to compress  the serialized object's data
	 *
	 */

	public static String createZipPack(final String s, final String name) throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (ZipOutputStream zos = new ZipOutputStream(baos)) {
				byte[] buf = new byte[BUFFER_SIZE];

				int len;
				try (ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes())) {
					zos.putNextEntry(new ZipEntry(name));
					LOG.info("zip file:{}", name);
					while ((len = in.read(buf)) > 0) {
						zos.write(buf, 0, len);
					}
				}

			zos.close();
			String base64encodedString = Base64.getEncoder().encodeToString(baos.toByteArray());
			// LOG.info("zip:{}", base64encodedString);
			return base64encodedString;
		}
	}

	/**
	 * Unpacks a zip archive to fs preserving directory structure. 
	 * All files  with the same name will be rewritten. 
	 * Typical zip includes 1 folder with files + 1 file in root directory
	 * Hierarchy of folders created  automatically
	 * NB: if innerName specified, unpack zip with name replacing 
	 */
	public static void unpackZip(final String zip, File outdir, String compType, String compName) throws IOException, UnsupportedEncodingException {

		byte[] base64decodedBytes = Base64.getDecoder().decode(zip);
		// LOG.info("zip:{}", base64decodedBytes);
		try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(base64decodedBytes))) {
			ZipEntry entry;
			String name, dir, superDir;
			String sha = "";
			while ((entry = zin.getNextEntry()) != null) {// unpack all entries
				// name = objects/DL_104_1__c.object
				name = entry.getName();
				if (entry.isDirectory()) {
//					LOG.info("create empty dir:{}", name);
					mkdirs(outdir, name);
					continue;
				}
				/*
				 * needed if directory comes after file: /dir/foo.txt /dir/
				 */
//				LOG.info("write file:{}", name);
				// dir = objects/DL_104_1__c.object -> objects 
				dir = StringUtils.getDir(name);
//				LOG.info("to directory:{}", dir);
				if (dir != null) {
					// verify, is another sublevel exist (f.e. in aura case)
					superDir = StringUtils.getDir(dir);
					if (superDir != null) {
						mkdirs(outdir, superDir);
					}
					mkdirs(outdir, dir);
				}
				String fname = StringUtils.getName(name);
				if (!OMITFILES.contains(fname)) {
					if (!CompType.isInner(compType)){
						extractFile(zin, outdir, name);
					}else{// add to file name the inner name + ext
//						LOG.info("InnerName:{}",compName);
//						LOG.info("composeInnerName:{}",StringUtils.composeInnerName(name, compName, compType));
						extractFile(zin, outdir, StringUtils.composeInnerName(name, compName, compType));
					}
//					LOG.info("unpack file:{}, name:{}", name, fname);
				}
			}
			zin.close();
		}
	}
	
	public static void unpackZip(final String zip, File outdir, String compType, String compName, XmlPreprocessor xmlPreprocessor) throws IOException, UnsupportedEncodingException {
		byte[] base64decodedBytes = Base64.getDecoder().decode(zip);
		// LOG.info("zip:{}", base64decodedBytes);
		try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(base64decodedBytes))) {
			ZipEntry entry;
			String name, dir, superDir;
			String sha = "";
			while ((entry = zin.getNextEntry()) != null) {// unpack all entries
				// name = objects/DL_104_1__c.object
				name = entry.getName();
				if (entry.isDirectory()) {
//					LOG.info("create empty dir:{}", name);
					mkdirs(outdir, name);
					continue;
				}
				/*
				 * needed if directory comes after file: /dir/foo.txt /dir/
				 */
//				LOG.info("write file:{}", name);
				// dir = objects/DL_104_1__c.object -> objects 
				dir = StringUtils.getDir(name);
//				LOG.info("to directory:{}", dir);
				if (dir != null) {
					// verify, is another sublevel exist (f.e. in aura case)
					superDir = StringUtils.getDir(dir);
					if (superDir != null) {
						mkdirs(outdir, superDir);
					}
					mkdirs(outdir, dir);
				}
				String fname = StringUtils.getName(name);
				if (!OMITFILES.contains(fname)) {
					if (!CompType.isInner(compType)){
						extractFile(zin, outdir, name);
					}else{// add to file name the inner name + ext
//						LOG.info("InnerName:{}",compName);
//						LOG.info("composeInnerName:{}",StringUtils.composeInnerName(name, compName, compType));
						File f = extractFile(zin, outdir, StringUtils.composeInnerName(name, compName, compType));
						xmlPreprocessor.add(compName, compType, f);
					}
//					LOG.info("unpack file:{}, name:{}", name, fname);
				}
			}
			zin.close();
		}
		
	}
	
	/**
	 * Unpacks a zip archive to fs preserving directory structure. 
	 * All files  with the same name will be rewritten. 
	 * Typical zip includes 1 folder with files + 1 file in root directory
	 * Hierarchy of folders created  automatically
	 * NB: if innerName specified, unpack zip with name replacing 
	 */
	public static Map<String,String> unpackZip(final String zip, File outdir) throws IOException, UnsupportedEncodingException {

		byte[] base64decodedBytes = Base64.getDecoder().decode(zip);
		Map<String,String> contentMap = new HashMap<String,String>();
		// LOG.info("zip:{}", base64decodedBytes);
		try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(base64decodedBytes))) {
			ZipEntry entry;
			String name, dir, superDir;
			String sha = "";
			while ((entry = zin.getNextEntry()) != null) {// unpack all entries
				// name = objects/DL_104_1__c.object
				name = entry.getName();
				if (entry.isDirectory()) {
//					LOG.info("create empty dir:{}", name);
					mkdirs(outdir, name);
					continue;
				}
				/*
				 * needed if directory comes after file: /dir/foo.txt /dir/
				 */
//				LOG.info("write file:{}", name);
				// dir = objects/DL_104_1__c.object -> objects 
				dir = StringUtils.getDir(name);
//				LOG.info("to directory:{}", dir);
				if (dir != null) {
					// verify, is another sublevel exist (f.e. in aura case)
					superDir = StringUtils.getDir(dir);
					if (superDir != null) {
						mkdirs(outdir, superDir);
					}
					mkdirs(outdir, dir);
				}
				String fname = StringUtils.getName(name);
				if (!OMITFILES.contains(fname)) {
						contentMap.put(name, fname);
						extractFile(zin, outdir, name);
//					LOG.info("unpack file:{}, name:{}", name, fname);
				}
			}
			zin.close();
		}
		return contentMap;
	}



	/**
	 * Extracts zip to lstXml
	 * Returns package file
	 */
	public static String unpackZip(final String zip,List<XmlProcessor> lstXml)
			throws IOException, UnsupportedEncodingException {

		byte[] base64decodedBytes = Base64.getDecoder().decode(zip);
		String packageFile = null;

		try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(base64decodedBytes))) {
			ZipEntry entry;
			String name;
			while ((entry = zin.getNextEntry()) != null) {
				name = entry.getName();
//				LOG.info("unpack  :{}", name);
				if (OMITFILES.contains(StringUtils.getName(name))) {
					packageFile = extractFile(zin);
				}else if (!entry.isDirectory()) {
					lstXml.add(new XmlProcessor(extractFile(zin),StringUtils.getBareName(name)));
				}
			}
			zin.close();
		}
		return packageFile;
	}
	
	 /**
	  *  Unpack a zip archive to general string with path =  name. 
	  *  Usually used to decompress  the serialized object's data
	  */
	public static String unpackZip(final String zip,final String name) throws IOException{
		String unpacked = null;
		byte[] base64decodedBytes = Base64.getDecoder().decode(zip);
		try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(base64decodedBytes))) {
			ZipEntry entry;
			while ((entry = zin.getNextEntry()) != null) {
				String fName = entry.getName();
				if (fName.equals(name)) {
					unpacked = extractFile(zin);
				}
			}
			zin.close();
		}
		return unpacked;
	}
	
	public static void unpackZip(List<ZBlob> files) throws IOException {
		for(ZBlob zBlob: files){
			try (ByteArrayInputStream in = new ByteArrayInputStream(zBlob.getBlob().getBytes())) {
				mkdirs(zBlob.getDir());
//				LOG.info("write file:{}, [{}]", zBlob.getAbsPath().toString(),zBlob.getBlob().length());
				extractFile(in,zBlob.getAbsPath());
			}
		}
	}
	

	public static void write(String fileString, File fName) throws IOException {
		if (fileString == null) return;
			try (ByteArrayInputStream in = new ByteArrayInputStream(fileString.getBytes())) {
				extractFile(in,fName);
			}
	}

	
	public static String createPlainPack(File file, String path)
			throws IOException, UnsupportedEncodingException {
		byte[] buf = new byte[BUFFER_SIZE];

		int len;
		try (FileInputStream in = new FileInputStream(file)) {
			ByteArrayOutputStream memoryBuf = new ByteArrayOutputStream(); 
			while ((len = in.read(buf)) != -1){
				memoryBuf.write(buf, 0, len);
			}
			return memoryBuf.toString();
		}		
	}

	
	
	/**
	 * 
	 * 		Lowest level utilities
	 * 
	 */
	/**
	 * Extracts a single file to disk from byte stream
	 * NB:  1) overwrites it, if exists
	 * 		2) before call this dir must be created
	 * @param in
	 * @param f
	 * @throws IOException
	 */
	private static void extractFile(ByteArrayInputStream in, File f) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
		int count = -1;
		while ((count = in.read(buffer)) != -1){
			out.write(buffer, 0, count);
		}
		out.flush();
		out.close();
	}
	/**
	 * Extracts a single file - main method which creates files
	 * NB:  1) overwrites it, if exists
	 * 		2) before call this dir must be created
	 * 		3) if file not empty, calc sha sum for it
	 * @param in
	 * @param outdir
	 * @param name
	 * @throws IOException
	 */
	private static File extractFile(ZipInputStream in, File outdir, String name) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		File f = new File(outdir, name);
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
		int count = -1;
		while ((count = in.read(buffer)) != -1){
			out.write(buffer, 0, count);
		}
		out.flush();
		out.close();
		return f;
	}

	/**
	 * Extracts a single file to string in memory
	 * NB: overwrites it, if exists
	 * 
	 * @param in
	 * @param outdir
	 * @param name
	 * @throws IOException
	 */
	private static String extractFile(ZipInputStream in) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		int count = -1;
		while ((count = in.read(buffer)) != -1){
			baos.write(buffer, 0, count);
		}
		return new String(baos.toByteArray(), "UTF-8"); 
	}


	/**
	 * Creates directory, if not exists
	 * 
	 * @param outdir
	 * @param path
	 */
	private static File mkdirs(File outdir, String path) {

		File d = new File(outdir, path);
//		LOG.info("dir:{}", d);
		mkdirs(d);
		return d;

	}

	/**
	 * Creates directory, if not exists
	 * 
	 * @param dir
	 */
	private static void mkdirs(File d) {

//		LOG.info("dir:{}", d);
		if (!d.exists()) {
			d.mkdirs();
		}
	}




	// end of class
}
