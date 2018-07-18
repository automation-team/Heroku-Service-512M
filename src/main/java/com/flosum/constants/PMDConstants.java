package com.flosum.constants;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PMDConstants {
	
	public static final String APEXCOMPLEXITY = "rulesets/apex/complexity.xml";
	public static final String APEXPERFORMANCE = "rulesets/apex/performance.xml";
	public static final String APEXSTYLE = "rulesets/apex/style.xml";
	public static final String APEXUNIT = "rulesets/apex/apexunit.xml";
	public static final String APEXSECURITY = "rulesets/apex/security.xml";
	public static final String APEXBRACES = "rulesets/apex/braces.xml";

	public static final String VFSECURITY = "rulesets/vf/security.xml";
	
	
	private static final Set<String> APEX_RULESETS = Collections.unmodifiableSet(new HashSet<String>() {
		private static final long serialVersionUID = -6463535784824857735L;
		{
			add(APEXCOMPLEXITY);
			add(APEXPERFORMANCE);
			add(APEXSTYLE);
			add(APEXUNIT);
			add(APEXSECURITY);
			add(APEXBRACES);
		}
	});

	private static final Set<String> VF_RULESETS = Collections.unmodifiableSet(new HashSet<String>() {
		private static final long serialVersionUID = -6463535784824857735L;
		{
			add(VFSECURITY);
		}
	});

	public static Set<String> getApexRulesets() {
		return APEX_RULESETS;
	}

	public static Set<String> getVfRulesets() {
		return VF_RULESETS;
	}
	

}
