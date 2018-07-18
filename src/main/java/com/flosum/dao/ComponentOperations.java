package com.flosum.dao;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.flosum.constants.CompType;
import com.flosum.model.MetaItem;
import com.flosum.model.Payload;
import com.flosum.sfcomponents.*;

public class ComponentOperations {

	public ComponentOperations() {
	}
	
	private static final String AURA_BUNDLE = "AuraDefinitionBundle";
	private static final String DOCUMENT = "Document";
	private static final String CUSTOM_LABELS = "CustomLabel";
	private static final String EMAIL_TEMPLATE = "EmailTemplate";
	private static final String TERRITORY2 = "Territory2";
	private static final String TERRITORY2MODEL = "Territory2Model";
	private static final String TERRITORY2RULE = "Territory2Rule";


	/**
	 * Creates a package to return to Flosum
	 * encoded as base64 string
	 * 
	 * @param ids
	 * 			 - list of names of components to add to package
	 * @param compType
	 *            - type of components in the package (every package may contains only comp of 1 type)
	 * @param repoDir
	 *            - base directory of Git repository
	 */
	public static String createPackage(List<MetaItem> names, String compType, File repoDir)
			throws IOException, UnsupportedEncodingException {
		if (compType.equals(AURA_BUNDLE)){
			return AuraBundle.createAuraPackage(names, repoDir);
		}else if (compType.equals(DOCUMENT)){
			return Document.createDocumentPackage(names, repoDir);
		}else if (compType.equals(CUSTOM_LABELS)){
			return CustomLabels.createLabelsPackage(names, repoDir);
		}else if (compType.equals(EMAIL_TEMPLATE)){
			return EmailTemplate.createEmailPackage(names, repoDir);
		}else if (compType.equals(TERRITORY2)){
			return Territory2.createTerritory2Package(names, repoDir);
		}else if (compType.equals(TERRITORY2MODEL)){
			return Territory2Model.createTerritory2ModelPackage(names, repoDir);
		}else if (compType.equals(TERRITORY2RULE)){
			return Territory2Rule.createTerritory2RulesPackage(names, repoDir);
		}else if (CompType.isInner(compType)){
			return InnerComponent.createInnerTypePackage(names, compType, repoDir);
		}else{ 
			return GeneralComponent.createStandartPackage(names, compType, repoDir);
		}
	}
	
	/**
	 * Unpack the zip package retrieved from Flosum
	 * @param zip	(in base64 encoding)
	 * @param compType
	 * @param repoDir (usually this var get from git descriptor)
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static void unpackPackage(String zip, String compType, File repoDir) 
			throws UnsupportedEncodingException, IOException{

		if (CompType.isInner(compType)){
			InnerComponent.unpackInnerTypes(zip, compType, repoDir);
		}else{
			GeneralComponent.unpackGeneralTypes(zip, compType, repoDir);
		}
		
	}
	
	public static void unpackOneComponent(String zip, String compName, String compType, File repoDir) 
			throws UnsupportedEncodingException, IOException{

		if (CompType.isInner(compType)){
			InnerComponent.unpackInnerType(zip, compName, compType, repoDir);
		}else{
			GeneralComponent.unpackGeneralTypes(zip, compType, repoDir);
		}
		
	}




}
