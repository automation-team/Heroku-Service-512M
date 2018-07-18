package com.flosum.sfcomponents;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flosum.constants.CompType;
import com.flosum.model.MetaItem;
import com.flosum.model.ZBlob;
import com.flosum.utils.IOLogger;
import com.flosum.utils.StringUtils;
import com.flosum.utils.XmlInnerComponent;
import com.flosum.utils.XmlPack;
import com.flosum.utils.XmlProcessor;
import com.flosum.utils.XmlUtils;
import com.flosum.utils.ZipUtils;

public class InnerComponent {
	private static final char INNER_TOKEN = '#';
	private static final Logger LOG = LoggerFactory.getLogger(InnerComponent.class);
	final static String OFFSETBLOCK = "    "; 


	public InnerComponent() {
	}
	
	// creates package consisted from inner types, like this:
	// list of names: [Account.QAID__c, Custom_object_for_Flow__c.Active__c]
	// compType: CustomField
	// 1) from map objects/Account.object => List[realpaths]
	// 2) for each map's key: load  & merge all inner comp 
	public static String createInnerTypePackage(final List<MetaItem> names, final String compType,final File repoDir)
			throws IOException, UnsupportedEncodingException {
		String path;
		
		final String compParentType = CompType.getParentType(compType);
		// set base directory the same as for parent object
		final String baseDir = CompType.getDir(compParentType);
		final String ext = CompType.getExt(compParentType);
		final String innerExt = CompType.getExt(compType);
		LOG.info("InnerTypePack");
		IOLogger log = new IOLogger();
		int fails = 0;

		if (names.size() > 0) {
			for (MetaItem mi : names) {
				String parentName = StringUtils.getParentName(mi.getName());
				String innerName = StringUtils.getInnerName(mi.getName());
				// form key as {objects/Account.object}; it will be a zpath in zip
				String key =  baseDir + parentName + ext; 
				path = key + INNER_TOKEN + innerName + innerExt;// actual (real) file path of component
				try{
					ZipUtils.createInnerZipPack(mi, key, new File(repoDir,path));
				}catch(Exception e){
					fails++;
					log.error(e.getMessage());
				}

				LOG.info(" form zip with key={} from path={}", key, path);
			}
		}
		log.download(compType, names.size() - fails);
		return log.getLog();

	}

	public static void unpackInnerTypes(final String zip, final String compType, final File repoDir)
			throws UnsupportedEncodingException, IOException {
		List<XmlProcessor> lstXml = new ArrayList<XmlProcessor>();
		List<ZBlob> files = new ArrayList<ZBlob>();
		String packDescriptor = ZipUtils.unpackZip(zip, lstXml);
		if (packDescriptor != null && lstXml != null){
			LOG.info("Get all members from:{}",packDescriptor);
			Map<String,List<String>> membersMap = XmlUtils.getAllMembers(packDescriptor);
			String path;
			
			final String compParentType = CompType.getParentType(compType);
			// set base directory the same as for parent object
			final String baseDir = CompType.getDir(compParentType);
			final String ext = CompType.getExt(compParentType);
			final String innerExt = CompType.getExt(compType);
			Set<String> searchSet = new HashSet<String>();
			searchSet.add(CompType.getInnerTag(compType));
			LOG.info("InnerTypePack");
			for (XmlProcessor member: lstXml){
				member.setSearchSet(searchSet);
				member.processNodeContent(0);
				// get map fields => [innerCompBody]
				Map<String, List<String>> allMembers = member.getMapTags();
				if (allMembers != null && allMembers.size() > 0){
					for (Map.Entry<String,List<String>> pair : allMembers.entrySet()) {
						// now curCompType = fields
						String curCompType = pair.getKey();
						// get all list of bodies
						for (String inner: pair.getValue()){
							// create inner component CustomField for CustomObject
							// TODO: verify for actionOverrides which has not the tag fullName
							if (inner != null){
								XmlInnerComponent xic = new XmlInnerComponent(compType, compParentType);
								xic.addEntry(OFFSETBLOCK+inner);
								path = baseDir + member.getParentName()+ ext + INNER_TOKEN + XmlProcessor.extractName(inner)+innerExt;
								LOG.info("to path {}",path);
								LOG.info("added {}",xic.toString());
								File f = new File(repoDir, path);
								files.add(new ZBlob(ZBlob.FileType.FILE, f, new File(repoDir, baseDir), xic.toString()));
							}
						}
					}
				}
			}
		}
		if (files.size() > 0){
			ZipUtils.unpackZip(files);
		}
		
	}
	
	public static void unpackInnerType(final String zip, final String compName, final String compType, final File repoDir)
			throws UnsupportedEncodingException, IOException {
			String path;
			
//			final String compParentType = CompType.getParentType(compType);
			// set base directory the same as for parent object
//			final String baseDir = CompType.getDir(compParentType);
//			final String ext = CompType.getExt(compParentType);
//			final String innerExt = CompType.getExt(compType);
//			final String suffix = INNER_TOKEN + StringUtils.getInnerName(compName) + innerExt;
//			LOG.info("suffix {}",suffix);
			ZipUtils.unpackZip(zip, repoDir, compType, compName);
	}



}
