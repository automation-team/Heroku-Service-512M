package com.flosum.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.flosum.model.Descriptor;
import com.flosum.model.PackageDescriptor;
import com.flosum.utils.StringUtils;
import com.flosum.utils.ZipUtils;
import com.sforce.soap.metadata.PackageTypeMembers;

public class PackageManager {

	private final static  Logger LOG = LoggerFactory.getLogger(PackageManager.class);
	private static final long ONE_SECOND = 1000;
	// manifest file that controls which components get retrieved
	private static final String MANIFEST_FILE = "package.xml";
	private static final double API_VERSION = 41.0;
	
	private Map<Long,PackageDescriptor> packageMap;
	
	public PackageManager(){
		packageMap = new HashMap<Long,PackageDescriptor>();
	}
	
	public synchronized PackageDescriptor addPackage(Long id, File dir){
		PackageDescriptor d = new PackageDescriptor(dir);
		packageMap.put(id, d);
		return d;
	}

	public PackageDescriptor getPackageDescriptor(Long id){
		return packageMap.get(id);
	}

	public Boolean isExist(Long id){
		return packageMap.containsKey(id);
	}
	
	public synchronized void removePackage(Long id){
		if (isExist(id)) packageMap.remove(id);
	}
	
	
	/**
	 *  Parses and adds to map all components from the package
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public void parsePackage(Long id, File file) throws Exception {
		PackageDescriptor descriptor;
		if (isExist(id)){
			descriptor = getPackageDescriptor(id);
			descriptor.setPackageFile(file);
		}else{
			return; 
		}
		try {
			InputStream is = new FileInputStream(file);
			List<PackageTypeMembers> pd = new ArrayList<PackageTypeMembers>();
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Element d = db.parse(is).getDocumentElement();
			for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
				if (c instanceof Element) {
					Element ce = (Element) c;
					//
					NodeList namee = ce.getElementsByTagName("name");
					if (namee.getLength() == 0) {
						// not
						continue;
					}
					String name = namee.item(0).getTextContent();
					NodeList m = ce.getElementsByTagName("members");
					List<String> members = new ArrayList<String>(); 
					for (int i = 0; i < m.getLength(); i++) {
						Node mm = m.item(i);
						members.add(mm.getTextContent());
					}
					descriptor.addMembers(name, members);
				}
			}
		} catch (ParserConfigurationException pce) {
			throw new Exception("Cannot create XML parser", pce);
		} catch (IOException ioe) {
			throw new Exception(ioe);
		} catch (SAXException se) {
			throw new Exception(se);
		}
	}

	public byte[] composePackage(Long id) throws Exception {
		byte[] packageZip = null;
		PackageDescriptor descriptor;
		if (isExist(id)){
			descriptor = getPackageDescriptor(id);
		}else{
			return packageZip; 
		}
		List<File> files = descriptor.getFiles();
		File manifestFile = descriptor.getPackageFile();
//		LOG.debug("manifestFile:{}",manifestFile);
		if (manifestFile != null){
			String manifest = composePackageManifest(id);
			if (manifest != null){
//				LOG.debug("manifest:{}",manifest);
//				LOG.debug("manifest(rel):{}",StringUtils.getRelativePath(descriptor.getLocalDir().toString(), manifestFile.toString()));
				ZipUtils.write(manifest, manifestFile);
				files.add(manifestFile);
			}
		}
//		LOG.debug("getLocalDir:{}",descriptor.getLocalDir());
		packageZip = ZipUtils.createZipPack(files, descriptor.getLocalDir().toString());
		return packageZip;
	}

	public String composePackageManifest(Long id) throws Exception {
		String packageXml = "";
		PackageDescriptor descriptor;
		if (isExist(id)){
			descriptor = getPackageDescriptor(id);
		}else{
			return packageXml; 
		}
		PackageGenerator pg = new PackageGenerator(descriptor.getPackageMap(),API_VERSION + "");
		pg.createPackageManifest();
		packageXml = pg.getPackage();
		
		return packageXml;
	}


}
