package com.flosum.constants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

public class CompType {
	
	//constructor
	public CompType() {
	}

	public static String getDir(String cName) {
		return getDirName(cName) + File.separatorChar;
	}

	public static String getDirName(String cName) {
		return NAME2DIR.containsKey(cName) ? NAME2DIR.get(cName) : "notSF";
	}

	public static String getExt(String cType) {
		return TYPE2EXT.containsKey(cType) ? TYPE2EXT.get(cType) : ".~";
	}

	public static String getMetaExt(String cName) {
		return getExt(cName) + "-meta.xml";
	}

	// reserved for more complex mapping
	public static String getName(String cName) {
		return cName;
	}

	public static String getType(String dirName) {
		return DIR2TYPENAME.containsKey(dirName) ? DIR2TYPENAME.get(dirName) : "notSF";
	}

	/**
	 * @return true, if component comes with a meta-file
	 */
	public static Boolean isPair(String cName) {
		return NEEDMETA.contains(cName);
	}

	/**
	 * @return true, if component can contain the inner types
	 */
	public static Boolean isWithInner(String cName) {
		return CONTAINSINNERTYPES.contains(cName);
	}

	/**
	 * @return true, if component can contain the inner types
	 */
	public static Boolean isInner(String cName) {
		return INNERTYPES.contains(cName);
	}
	
	/**
	 * @return true, if component can contain the inner types
	 */
	public static Boolean isMaster(String cName) {
		return PARENTTYPES.contains(cName);
	}
	

	/**
	 * @return true, if component can contain the inner types
	 */
	public static Boolean isSupported(String cName) {
		return SUPPORTED_SF_TYPES.contains(cName);
	}


	/**
	 * @return set from the inner components tags
	 */

	public static Set<String> getCustomObjectInnerSet(){
		return INNERTYPES_TAG2CUSTOMOBJECTNAME.keySet();
	}
	
	public static Set<String> getSharingRulesInnerSet(){
		return INNERTYPES_TAG2SHARINGRULESNAME.keySet();
	}

	public static Set<String> getWorkflowInnerSet(){
		return INNERTYPES_TAG2WORKFLOWNAME.keySet();
	}
	
	public static Set<String> getInnerSet(String type){
		if (type.equals("CustomObject")) return getCustomObjectInnerSet();
		if (type.equals("Workflow")) return getWorkflowInnerSet();
		if (type.equals("SharingRules")) return getSharingRulesInnerSet();
		return new HashSet<String>();
	}
	
	public static String getInnerType(String tag){
		
		return INNERTYPES_TAG2TYPE.containsKey(tag)?INNERTYPES_TAG2TYPE.get(tag):"";
	}

	public static String getInnerTag(String type){
		
		return INNERTYPES_TYPE2TAG.containsKey(type)?INNERTYPES_TYPE2TAG.get(type):"";
	}

	public static String getParentType(String innerType){
		
		return INNERTYPES_PARENTTYPE.containsKey(innerType)?INNERTYPES_PARENTTYPE.get(innerType):"";
	}
	
	public static String getParentExt(String innerType){
		return INNERTYPES_PARENTTYPE.containsKey(innerType)? TYPE2EXT.get(INNERTYPES_PARENTTYPE.get(innerType)):"";
	}
	
	public static String getTypebyExt(String ext){
		return INNERTYPES_EXT2TYPE.containsKey(ext)?INNERTYPES_EXT2TYPE.get(ext):"notSF";
	}

	private static final Set<String> SUPPORTED_SF_TYPES = Collections.unmodifiableSet(new HashSet<String>() {
		{
			add("ApexClass");
			add("ApexPage");
			add("ApexTrigger");
		}
	});


	/**
	 * 	SF component types description
	 * 	Note: only combination dir#extension is unique 
	 */

	private static final Map<String, String> NAME2DIR = Collections.unmodifiableMap(new HashMap<String, String>() {
		private static final long serialVersionUID = -4586416685777607631L;

		{
			// (107 types)
			put("ActionLinkGroupTemplate", "actionLinkGroupTemplates");
			put("AuraDefinitionBundle", "aura");
			put("ApexClass", "classes");
			put("ApexComponent", "components");
			put("ApexPage", "pages");
			put("ApexTrigger", "triggers");
			put("CustomLabels", "labels");
			put("CustomTab", "tabs");
			put("CustomObject", "objects");
			put("HomePageLayout", "homePageLayouts");
			put("RemoteSiteSetting", "remoteSiteSettings");
			put("StaticResource", "staticresources");
			put("AnalyticSnapshot", "analyticSnapshots");
			put("ArticleType Layout", "layouts");
			put("ChannelLayout", "channelLayouts");
			put("ApexTestSuite", "testSuites");
			put("AppMenu", "appMenus");
			put("ApprovalProcess", "approvalProcesses");
			put("AssignmentRules", "assignmentRules");
			put("AuthProvider", "authproviders");
			put("AutoResponseRules", "autoResponseRules");
			put("CallCenter", "callCenter");
			put("CampaignInfluenceModel", "campaignInfluenceModels");
			put("Certificate", "certs");
			put("CleanDataService", "cleanDataServices");
			put("Community", "communities");
			put("CommunityTemplateDefinition", "communityTemplateDefinitions");
			put("CommunityThemeDefinition", "communityThemeDefinitions");
			put("ConnectedApp", "connectedapps");
			put("ContentAsset", "contentassets");
			put("CorsWhitelistOrigin", "corswhitelistorigins");
			put("CustomApplication", "applications");
			put("CustomApplicationComponent", "customApplicationComponents");
			put("CustomFeedFilter", "feedFilters");
			put("CustomMetadata", "customMetadata");
			put("CustomObjectTranslation", "objectTranslations");
			put("CustomPageWebLink", "weblinks");
			put("CustomPermission", "customPermissions");
			put("CustomSite", "sites");
			put("CustomValue", "customValues");
			put("Dashboard", "dashboards");
			put("DashboardFolder", "dashboards");// folder types have the same dir
			put("DataCategoryGroup", "datacategorygroups");
			put("DelegateGroup", "delegateGroups");
			put("Document", "documents");
			put("DocumentFolder", "documents");
			put("DuplicateRule", "duplicateRules");
			put("EmailTemplate", "email");
			put("EmailFolder", "email");
			put("EntitlementProcess", "entitlementProcesses");
			put("EntitlementTemplate", "entitlementTemplates");
			put("EscalationRules", "escalationRules");
			put("ExternalDataSource", "dataSources");
			put("FlexiPage", "flexipages");
			put("Flow", "flow");
			put("FlowDefinition", "flowDefinitions");
			put("GlobalPicklist", "globalPicklists");
			put("GlobalValueSet", "globalValueSets");
			put("GlobalValueSetTranslation", "globalValueSetTranslations");
			put("Group", "groups");
			put("HomePageComponent", "homepagecomponents");
			put("HomePageLayout", "homePageLayouts");
			put("InstalledPackage", "installedPackages");
			put("KeywordList", "moderation");
			put("Layout", "layouts");
			put("Letterhead", "letterhead");
			put("LiveChatAgentConfig", "liveChatAgentConfigs");
			put("LiveChatButton", "liveChatButtons");
			put("LiveChatDeployment", "liveChatDeployments");
			put("LiveChatSensitiveDataRule", "liveChatSensitiveDataRule");
			put("ManagedTopics", "managedTopics");
			put("MatchingRules", "matchingRules");
			put("MilestoneType", "milestoneTypes");
			put("ModerationRule", "moderation");
			put("NamedCredential", "namedCredentials");
			put("Network", "networks");
			put("PathAssistant", "pathAssistants");
			put("PermissionSet", "permissionsets");
			put("PlatformCachePartition", "cachePartitions");
			put("Portal", "portals");
			put("PostTemplate", "postTemplates");
			put("Profile", "profiles");
			put("Queue", "queues");
			put("QuickAction", "quickActions");
			put("Report", "reports");
			put("ReportFolder", "reports");
			put("ReportType", "reportTypes");
			put("Role", "roles");
			put("SamlSsoConfig", "samlssoconfigs");
			put("Scontrol", "scontrols");
			put("Settings", "settings");
			put("SharingRules", "sharingRules");
			put("SharingSet", "sharingSets");
			put("SiteDotCom", "siteDotComSites");
			put("Skill", "skills");
			put("StandardValueSet", "standardValueSets");
			put("StandardValueSetTranslation", "standardValueSetTranslations");
			put("SynonymDictionary", "synonymDictionaries");
			put("Territory", "territories");
			put("Territory2", "territory2Models");
			put("Territory2Model", "territory2Models");
			put("Territory2Rule", "territory2Models");
			put("Territory2Type", "territory2Types");
			put("TransactionSecurityPolicy", "transactionSecurityPolicies");
			put("Translations", "translations");
			put("WaveDataset", "wave");
			put("Workflow", "workflows");
//			put("WaveApplication", "wave");
//			put("WaveDataflow", "wave");
//			put("WaveDashboard", "wave");
//			put("WaveLens", "wave");
		}
	});

	private static final Map<String, String> DIR2TYPENAME = Collections.unmodifiableMap(new HashMap<String, String>() {
		private static final long serialVersionUID = 1415799626503778274L;

		{
			// (107-4)
			put("aura", "AuraDefinitionBundle");
			put("classes", "ApexClass");
			put("components", "ApexComponent");
			put("pages", "ApexPage");
			put("triggers", "ApexTrigger");
			put("labels", "CustomLabels");
			put("tabs", "CustomTab");
			put("objects", "CustomObject");
			put("homePageLayouts", "HomePageLayout");
			put("homepagecomponents", "HomePageComponent");
			put("remoteSiteSettings", "RemoteSiteSetting");
			put("staticresources", "StaticResource");
			put("actionLinkGroupTemplates", "ActionLinkGroupTemplate");
			put("analyticSnapshots", "AnalyticSnapshot");
			put("layouts", "ArticleType Layout");
			put("channelLayouts", "ChannelLayout");
			put("testSuites", "ApexTestSuite");
			put("appMenus", "AppMenu");
			put("approvalProcesses", "ApprovalProcess");
			put("assignmentRules", "AssignmentRules");
			put("authproviders", "AuthProvider");
			put("autoResponseRules", "AutoResponseRules");
			put("callCenter", "CallCenter");
			put("campaignInfluenceModels", "CampaignInfluenceModel");
			put("certs", "Certificate");
			put("cleanDataServices", "CleanDataService");
			put("communities", "Community");
			put("communityTemplateDefinitions", "CommunityTemplateDefinition");
			put("communityThemeDefinitions", "CommunityThemeDefinition");
			put("connectedapps", "ConnectedApp");
			put("contentassets", "ContentAsset");
			put("corswhitelistorigins", "CorsWhitelistOrigin");
			put("applications", "CustomApplication");
			put("customApplicationComponents", "CustomApplicationComponent");
			put("feedFilters", "CustomFeedFilter");
			put("customMetadata", "CustomMetadata");
			put("objectTranslations", "CustomObjectTranslation");
			put("weblinks", "CustomPageWebLink");
			put("customPermissions", "CustomPermission");
			put("sites", "CustomSite");
			put("customValues", "CustomValue");
			put("dashboards", "Dashboard");
			put("datacategorygroups", "DataCategoryGroup");
			put("delegateGroups", "DelegateGroup");
			put("documents", "Document");
			put("duplicateRules", "DuplicateRule");
			put("email", "EmailTemplate");
			put("entitlementProcesses", "EntitlementProcess");
			put("entitlementTemplates", "EntitlementTemplate");
			put("escalationRules", "EscalationRules");
			put("dataSources", "ExternalDataSource");
			put("flexipages", "FlexiPage");
			put("flow", "Flow");
			put("flowDefinitions", "FlowDefinition");
			put("globalPicklists", "GlobalPicklist");
			put("globalValueSets", "GlobalValueSet");
			put("globalValueSetTranslations", "GlobalValueSetTranslation");
			put("groups", "Group");
			put("homePageLayouts", "HomePageLayout");
			put("installedPackages", "InstalledPackage");
			put("moderation", "KeywordList");
			put("layouts", "Layout");
			put("letterhead", "Letterhead");
			put("liveChatAgentConfigs", "LiveChatAgentConfig");
			put("liveChatButtons", "LiveChatButton");
			put("liveChatDeployments", "LiveChatDeployment");
			put("liveChatSensitiveDataRule", "LiveChatSensitiveDataRule");
			put("managedTopics", "ManagedTopics");
			put("matchingRules", "MatchingRules");
			put("milestoneTypes", "MilestoneType");
			put("moderation", "ModerationRule");
			put("namedCredentials", "NamedCredential");
			put("networks", "Network");
			put("pathAssistants", "PathAssistant");
			put("permissionsets", "PermissionSet");
			put("cachePartitions", "PlatformCachePartition");
			put("portals", "Portal");
			put("postTemplates", "PostTemplate");
			put("profiles", "Profile");
			put("queues", "Queue");
			put("quickActions", "QuickAction");
			put("reports", "Report");
			put("reportTypes", "ReportType");
			put("roles", "Role");
			put("samlssoconfigs", "SamlSsoConfig");
			put("scontrols", "Scontrol");
			put("settings", "Settings");
			put("sharingRules", "SharingRules");
			put("sharingSets", "SharingSet");
			put("siteDotComSites", "SiteDotCom");
			put("skills", "Skill");
			put("standardValueSets", "StandardValueSet");
			put("standardValueSetTranslations", "StandardValueSetTranslation");
			put("synonymDictionaries", "SynonymDictionary");
			put("territories", "Territory");
			put("territory2Models", "Territory2");
			put("territory2Models", "Territory2Model");
			put("territory2Models", "Territory2Rule");
			put("territory2Types", "Territory2Type");
			put("transactionSecurityPolicies", "TransactionSecurityPolicy");
			put("translations", "Translations");
			put("wave", "WaveDataset");
			put("workflows", "Workflow");
//			put("wave", "WaveApplication");
//			put("wave", "WaveDataflow");
//			put("wave", "WaveDashboard");
//			put("wave", "WaveLens");
		}
	});
	
	
	/**
	 * 		NOTE: for inner types artificial ext introduced
	 * 
	 */
	private static final Map<String, String> TYPE2EXT = Collections.unmodifiableMap(new HashMap<String, String>() {
		private static final long serialVersionUID = -5833553469935640980L;

		{
			put("AuraDefinitionBundle", "*");
			put("ApexClass", ".cls");
			put("ApexComponent", ".component");
			put("ApexPage", ".page");
			put("ApexTrigger", ".trigger");
			put("CustomLabels", ".labels");
			put("CustomTab", ".tab");
			put("CustomObject", ".object");
			put("HomePageLayout", ".homePageLayout");
			put("RemoteSiteSetting", ".remoteSite");
			put("StaticResource", ".resource");
			put("ActionLinkGroupTemplate", ".actionLinkGroupTemplate");
			put("AnalyticSnapshot", ".analyticsnapshot");
			put("ArticleType Layout", ".layout");
			put("ChannelLayout", ".channelLayout");
			put("ApexTestSuite", ".testSuite");
			put("AppMenu", ".appMenu");
			put("ApprovalProcess", ".approvalProcess");
			put("AssignmentRules", ".assignmentRules");
			put("AuthProvider", ".authprovider");
			put("AutoResponseRules", ".autoResponseRules");
			put("CallCenter", ".callCenter");
			put("CampaignInfluenceModel", ".campaignInfluenceModel");
			put("Certificate", ".crt");
			put("CleanDataService", ".cleanDataService");
			put("Community", ".community");
			put("CommunityTemplateDefinition", ".communityTemplateDefinition");
			put("CommunityThemeDefinition", ".communityThemeDefinition");
			put("ConnectedApp", ".connectedapp");
			put("ContentAsset", ".asset");
			put("CorsWhitelistOrigin", ".corswhitelistorigin");
			put("CustomApplication", ".app");
			put("CustomApplicationComponent", ".customApplicationComponent");
			put("CustomFeedFilter", ".feedFilter");
			put("CustomMetadata", ".md");
			put("CustomObjectTranslation", ".objectTranslation");
			put("CustomPageWebLink", ".weblink");
			put("CustomPermission", ".customPermission");
			put("CustomSite", ".site");
			put("CustomValue", ".customValue");
			put("Dashboard", ".dashboard");
			put("DashboardFolder", "");
			put("DataCategoryGroup", ".datacategorygroup");
			put("DelegateGroup", ".delegateGroup");
			put("Document", "*");
			put("DocumentFolder", "");
			put("DuplicateRule", ".duplicateRule");
			put("EmailTemplate", ".email");
			put("EmailFolder", "");
			put("EntitlementProcess", ".entitlementProcess");
			put("EntitlementTemplate", ".entitlementTemplate");
			put("EscalationRules", ".escalationRules");
			put("ExternalDataSource", ".dataSource");
			put("FlexiPage", ".flexipage");
			put("Flow", ".flow");
			put("FlowDefinition", ".flowDefinition");
			put("GlobalPicklist", ".globalPicklist");
			put("GlobalValueSet", ".globalValueSet");
			put("GlobalValueSetTranslation", ".globalValueSetTranslation");
			put("Group", ".group");
			put("HomePageComponent", ".homePageComponent");
			put("HomePageLayout", ".homePageLayout");
			put("InstalledPackage", ".installedPackage");
			put("KeywordList", ".keywords");
			put("Layout", ".layout");
			put("Letterhead", ".letter");
			put("LiveChatAgentConfig", ".liveChatAgentConfig");
			put("LiveChatButton", ".liveChatButton");
			put("LiveChatDeployment", ".liveChatDeployment");
			put("LiveChatSensitiveDataRule", ".liveChatSensitiveDataRule");
			put("ManagedTopics", ".managedTopics");
			put("MatchingRules", ".matchingRule");
			put("MilestoneType", ".milestoneType");
			put("ModerationRule", ".rule");
			put("NamedCredential", ".namedCredential");
			put("Network", ".network");
			put("PathAssistant", ".pathAssistant");
			put("PermissionSet", ".permissionset");
			put("PlatformCachePartition", ".cachePartition");
			put("Portal", ".portal");
			put("PostTemplate", ".postTemplate");
			put("Profile", ".profile");
			put("Queue", ".queue");
			put("QuickAction", ".quickAction");
			put("Report", ".report");
			put("ReportFolder", "");
			put("ReportType", ".reportType");
			put("Role", ".role");
			put("SamlSsoConfig", ".samlssoconfig");
			put("Scontrol", ".scf");
			put("Settings", ".settings");
			put("SharingRules", ".sharingRules");
			put("SharingSet", ".sharingSet");
			put("SiteDotCom", ".site");
			put("Skill", ".skill");
			put("StandardValueSet", ".standardValueSet");
			put("StandardValueSetTranslation", ".standardValueSetTranslation");
			put("SynonymDictionary", ".synonymDictionary");
			put("Territory", ".territory");
			put("Territory2", ".territory2");
			put("Territory2Model", ".territory2Model");
			put("Territory2Rule", ".territory2Rule");
			put("Territory2Type", ".territory2Type");
			put("TransactionSecurityPolicy", ".transactionSecurityPolicy");
			put("Translations", ".translation");
			put("WaveDataset", ".wds");
			put("Workflow", ".workflow");
//			put("WaveApplication", ".wapp");
//			put("WaveDataflow", ".wdf");
//			put("WaveDashboard", ".wdash");
//			put("WaveLens", ".wlens");
			
			// inner types 
			// (13)
			put("ActionOverride",".actionOverride");
			put("BusinessProcess",".businessProcess");
			put("CompactLayout",".compactLayout");
			put("CustomField",".field");
			put("FieldSet",".fieldSet");
			put("HistoryRetentionPolicy",".historyRetentionPolicy");
			put("ListView",".listView");
			put("RecordType",".recordType");
			put("SearchLayouts",".searchLayouts");
			put("SharingReason",".sharingReason");
			put("SharingRecalculation",".sharingRecalculation");
			put("ValidationRule",".validationRule");
			put("WebLink",".webLink");
			// (3)
			put("SharingCriteriaRule",".sharingCriteriaRule");
			put("SharingOwnerRule",".sharingOwnerRule");
			put("SharingTerritoryRule",".sharingTerritoryRule");
			// (5)
			put("WorkflowAlert",".alert");
			put("WorkflowFieldUpdate",".fieldUpdate");
			put("WorkflowOutboundMessage",".outboundMessage");
			put("WorkflowRule",".rule");
			put("WorkflowTask",".task");
			// (1)
			put("AssignmentRule",".assignmentRule");
			// (1)
			put("EscalationRule",".escalationRule");
			// (1)
			put("CustomLabel",".label");
			
		}
	});

	private static final Set<String> NEEDMETA = Collections.unmodifiableSet(new HashSet<String>() {
		private static final long serialVersionUID = -1756217790016537497L;

		{
			//(12)
			add("ApexClass");
			add("ApexPage");
			add("ApexTrigger");
			add("ApexComponent");
			add("Document");
			add("DocumentFolder");
			add("StaticResource");
			add("EmailTemplate");
			add("EmailFolder");
			add("SiteDotCom");
			add("DashboardFolder");
			add("ReportFolder");
		}
	});
	
	private static final Set<String> INNERTYPES = Collections.unmodifiableSet(new HashSet<String>() {
		private static final long serialVersionUID = -6805531737184933874L;

		{
			//(13)
			add("ActionOverride");
			add("BusinessProcess");
			add("CompactLayout");
			add("CustomField");
			add("FieldSet");
			add("HistoryRetentionPolicy");
			add("ListView");
			add("RecordType");
			add("SearchLayouts");
			add("SharingReason");
			add("SharingRecalculation");
			add("ValidationRule");
			add("WebLink");
			//(3)
			add("SharingCriteriaRule");
			add("SharingOwnerRule");
			add("SharingTerritoryRule");
			//(5)
			add("WorkflowAlert");
			add("WorkflowFieldUpdate");
			add("WorkflowOutboundMessage");
			add("WorkflowRule");
			add("WorkflowTask");
			//(1)
			add("AssignmentRule");
			//(1)
			add("EscalationRule");
			//(1)
			add("CustomLabel");
		}
	});
	
	private static final Map<String, String> INNERTYPES_TAG2TYPE = Collections.unmodifiableMap(new HashMap<String, String>() {
		private static final long serialVersionUID = -1018304490248603383L;

		{
			//(13)
			put("actionOverrides","ActionOverride");
			put("businessProcesses","BusinessProcess");
			put("compactLayouts","CompactLayout");
			put("fields","CustomField");
			put("fieldSets","FieldSet");
			put("historyRetentionPolicy","HistoryRetentionPolicy");
			put("listViews","ListView");
			put("recordTypes","RecordType");
			put("searchLayouts","SearchLayouts");
			put("sharingReasons","SharingReason");
			put("sharingRecalculations","SharingRecalculation");
			put("validationRules","ValidationRule");
			put("webLinks","WebLink");
			//(3)

			put("sharingCriteriaRules","SharingCriteriaRule");
			put("sharingOwnerRules","SharingOwnerRule");
			put("sharingTerritoryRules","SharingTerritoryRule");
			//(5)
			
			put("alerts","WorkflowAlert");
			put("fieldUpdates","WorkflowFieldUpdate");
			put("outboundMessages","WorkflowOutboundMessage");
			put("rules","WorkflowRule");
			put("tasks","WorkflowTask");
			//(1)
			
			put("assignmentRule","AssignmentRule");
			//(1)

			put("escalationRule","EscalationRule");
			//(1)

			put("labels","CustomLabel");
		}
	});

	private static final Map<String, String> INNERTYPES_EXT2TYPE = Collections.unmodifiableMap(new HashMap<String, String>() {
		private static final long serialVersionUID = 6492425426367868180L;

		{
			//(13)
			put(".action","ActionOverride");
			put(".businessProcess","BusinessProcess");
			put(".compactLayout","CompactLayout");
			put(".field","CustomField");
			put(".fieldSet","FieldSet");
			put(".historyRetentionPolicy","HistoryRetentionPolicy");
			put(".listView","ListView");
			put(".recordType","RecordType");
			put(".searchLayout","SearchLayouts");
			put(".sharingReason","SharingReason");
			put(".sharingRecalculation","SharingRecalculation");
			put(".validationRule","ValidationRule");
			put(".webLink","WebLink");

			//(3)
			put(".sharingCriteriaRule","SharingCriteriaRule");
			put(".sharingOwnerRule","SharingOwnerRule");
			put(".sharingTerritoryRule","SharingTerritoryRule");
			
			//(5)
			put(".alert","WorkflowAlert");
			put(".fieldUpdate","WorkflowFieldUpdate");
			put(".outboundMessage","WorkflowOutboundMessage");
			put(".rule","WorkflowRule");
			put(".task","WorkflowTask");

			//(1)
			put(".assignmentRules","AssignmentRule");

			//(1)
			put(".escalationRule", "EscalationRule");

			//(1)
			put(".label","CustomLabel");
		}
	});

	
	private static final Map<String, String> INNERTYPES_TYPE2TAG = Collections.unmodifiableMap(new HashMap<String, String>() {
		private static final long serialVersionUID = -965855247529989887L;

		{
			//(13)
			put("ActionOverride","actionOverrides");
			put("BusinessProcess","businessProcesses");
			put("CompactLayout","compactLayouts");
			put("CustomField","fields");
			put("FieldSet","fieldSets");
			put("HistoryRetentionPolicy","historyRetentionPolicy");
			put("ListView","listViews");
			put("RecordType","recordTypes");
			put("SearchLayouts","searchLayouts");
			put("SharingReason","sharingReasons");
			put("SharingRecalculation","sharingRecalculations");
			put("ValidationRule","validationRules");
			put("WebLink","webLinks");

			//(3)
			put("SharingCriteriaRule","sharingCriteriaRules");
			put("SharingOwnerRule","sharingOwnerRules");
			put("SharingTerritoryRule","sharingTerritoryRules");

			//(5)
			put("WorkflowAlert","alerts");
			put("WorkflowFieldUpdate","fieldUpdates");
			put("WorkflowOutboundMessage","outboundMessages");
			put("WorkflowRule","rules");
			put("WorkflowTask","tasks");

			//(1)
			put("AssignmentRule","assignmentRule");

			//(1)
			put("EscalationRule","escalationRule");

			//(1)
			put("CustomLabel","labels");
		}
	});
	
	private static final Set<String> PARENTTYPES = Collections.unmodifiableSet(new HashSet<String>() {
		private static final long serialVersionUID = -476720788347634598L;

		{
			add("CustomObject");
			add("SharingRules");
			add("Workflow");
			add("AssignmentRules");
			add("EscalationRules");
			add("CustomLabels");
		}
	});



	private static final Map<String, String> INNERTYPES_PARENTTYPE = Collections.unmodifiableMap(new HashMap<String, String>() {
		private static final long serialVersionUID = -476720488447734598L;

		{
			//(13)
			put("ActionOverride","CustomObject");
			put("BusinessProcess","CustomObject");
			put("CompactLayout","CustomObject");
			put("CustomField","CustomObject");
			put("FieldSet","CustomObject");
			put("HistoryRetentionPolicy","CustomObject");
			put("ListView","CustomObject");
			put("RecordType","CustomObject");
			put("SearchLayouts","CustomObject");
			put("SharingReason","CustomObject");
			put("SharingRecalculation","CustomObject");
			put("ValidationRule","CustomObject");
			put("WebLink","CustomObject");

			//(3)
			put("SharingCriteriaRule","SharingRules");
			put("SharingOwnerRule","SharingRules");
			put("SharingTerritoryRule","SharingRules");

			//(5)
			put("WorkflowAlert","Workflow");
			put("WorkflowFieldUpdate","Workflow");
			put("WorkflowOutboundMessage","Workflow");
			put("WorkflowRule","Workflow");
			put("WorkflowTask","Workflow");

			//(1)
			put("AssignmentRule","AssignmentRules");

			//(1)
			put("EscalationRule","EscalationRules");

			//(1)
			put("CustomLabel","CustomLabels");
		}
	});


	private static final Set<String> INNERTYPES_CUSTOMOBJECT = Collections.unmodifiableSet(new HashSet<String>() {
		private static final long serialVersionUID = 9196080889526510586L;

		{
			add("ActionOverride");
			add("BusinessProcess");
			add("CompactLayout");
			add("CustomField");
			add("FieldSet");
			add("HistoryRetentionPolicy");
			add("ListView");
			add("RecordType");
			add("SearchLayouts");
			add("SharingReason");
			add("SharingRecalculation");
			add("ValidationRule");
			add("WebLink");
		}
	});

	private static final Set<String> INNERTYPES_SHARINGRULES = Collections.unmodifiableSet(new HashSet<String>() {
		{
			add("SharingCriteriaRule");
			add("SharingOwnerRule");
			add("SharingTerritoryRule");
		}
	});

	private static final Set<String> INNERTYPES_WORKFLOW = Collections.unmodifiableSet(new HashSet<String>() {
		{
			add("WorkflowAlert");
			add("WorkflowFieldUpdate");
			add("WorkflowOutboundMessage");
			add("WorkflowRule");
			add("WorkflowTask");
		}
	});
	
	private static final Map<String, String> INNERTYPES_CUSTOMOBJECTNAME2TAG = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put("ActionOverride","actionOverrides");
			put("BusinessProcess","businessProcesses");
			put("CompactLayout","compactLayouts");
			put("CustomField","fields");
			put("FieldSet","fieldSets");
			put("HistoryRetentionPolicy","historyRetentionPolicy");
			put("ListView","listViews");
			put("RecordType","recordTypes");
			put("SearchLayouts","searchLayouts");
			put("SharingReason","sharingReasons");
			put("SharingRecalculation","sharingRecalculations");
			put("ValidationRule","validationRules");
			put("WebLink","webLinks");
		}
	});

	private static final Map<String, String> INNERTYPES_TAG2CUSTOMOBJECTNAME = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put("actionOverrides","ActionOverride");
			put("businessProcesses","BusinessProcess");
			put("compactLayouts","CompactLayout");
			put("fields","CustomField");
			put("fieldSets","FieldSet");
			put("historyRetentionPolicy","HistoryRetentionPolicy");
			put("listViews","ListView");
			put("recordTypes","RecordType");
			put("searchLayouts","SearchLayouts");
			put("sharingReasons","SharingReason");
			put("sharingRecalculations","SharingRecalculation");
			put("validationRules","ValidationRule");
			put("webLinks","WebLink");
		}
	});

	
	private static final Map<String, String> INNERTYPES_SHARINGRULESNAME2TAG = Collections.unmodifiableMap(new HashMap<String, String>() {
		{

			put("SharingCriteriaRule","sharingCriteriaRules");
			put("SharingOwnerRule","sharingOwnerRules");
			put("SharingTerritoryRule","sharingTerritoryRules");
		}
	});

	private static final Map<String, String> INNERTYPES_TAG2SHARINGRULESNAME = Collections.unmodifiableMap(new HashMap<String, String>() {
		{

			put("sharingCriteriaRules","SharingCriteriaRule");
			put("sharingOwnerRules","SharingOwnerRule");
			put("sharingTerritoryRules","SharingTerritoryRule");
		}
	});

	private static final Map<String, String> INNERTYPES_WORKFLOWNAME2TAG = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put("WorkflowAlert","alerts");
			put("WorkflowFieldUpdate","fieldUpdates");
			put("WorkflowOutboundMessage","outboundMessages");
			put("WorkflowRule","rules");
			put("WorkflowTask","tasks");
		}
	});

	private static final Map<String, String> INNERTYPES_TAG2WORKFLOWNAME = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put("alerts","WorkflowAlert");
			put("fieldUpdates","WorkflowFieldUpdate");
			put("outboundMessages","WorkflowOutboundMessage");
			put("rules","WorkflowRule");
			put("tasks","WorkflowTask");
		}
	});

	private static final Set<String> CONTAINSINNERTYPES = Collections.unmodifiableSet(new HashSet<String>() {
		private static final long serialVersionUID = 3608502786783564204L;

		{
			add("Workflow");
			add("CustomObject");
			add("SharingRules");
			add("AssignmentRules");
			add("EscalationRules");
		}
	});
	


}
