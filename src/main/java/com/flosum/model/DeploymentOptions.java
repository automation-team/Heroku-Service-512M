package com.flosum.model;

import java.io.Serializable;

import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.TestLevel;

public class DeploymentOptions implements Serializable {

	private static final long serialVersionUID = 69L;
	
	private Boolean allowMissingFiles;
	private Boolean autoUpdatePackage;
	private Boolean checkOnly;
	private Boolean ignoreWarnings;
	private Boolean performRetrieve;
	private Boolean purgeOnDelete;
	private Boolean rollbackOnError;
	private String testLevel;
	private String[] runTests;
	private Boolean singlePackage;

	public DeploymentOptions() {
	}

	public DeploymentOptions(String RunTestValue) {
		allowMissingFiles = true;
		autoUpdatePackage = false;
		checkOnly = false;
		ignoreWarnings = false;
		performRetrieve = false;
		purgeOnDelete = false;
		rollbackOnError = true;
		testLevel = RunTestValue;
		runTests = null;
		singlePackage = true;
	}
	
	public void setDeploymentOptions(DeployOptions options){
		if (options == null) return;
		if (allowMissingFiles != null) options.setAllowMissingFiles(allowMissingFiles);
		if (autoUpdatePackage != null) options.setAutoUpdatePackage(autoUpdatePackage);
		if (checkOnly != null) options.setCheckOnly(checkOnly);
		if (ignoreWarnings != null) options.setIgnoreWarnings(ignoreWarnings);
		if (performRetrieve != null) options.setPerformRetrieve(performRetrieve);
		if (purgeOnDelete != null) options.setPurgeOnDelete(purgeOnDelete);
		if (rollbackOnError != null) options.setRollbackOnError(rollbackOnError);
		if (runTests != null) options.setRunTests(runTests);
		if (singlePackage != null) options.setSinglePackage(singlePackage);
		TestLevel tl  = null;
		if (testLevel != null) tl = TestLevel.valueOf(testLevel);
		if (tl != null){
			options.setTestLevel(tl);
		}else{
			options.setTestLevel(TestLevel.NoTestRun);
		}
	}

	public Boolean getAllowMissingFiles() {
		return allowMissingFiles;
	}

	public void setAllowMissingFiles(Boolean allowMissingFiles) {
		this.allowMissingFiles = allowMissingFiles;
	}

	public Boolean getAutoUpdatePackage() {
		return autoUpdatePackage;
	}

	public void setAutoUpdatePackage(Boolean autoUpdatePackage) {
		this.autoUpdatePackage = autoUpdatePackage;
	}

	public Boolean getCheckOnly() {
		return checkOnly;
	}

	public void setCheckOnly(Boolean checkOnly) {
		this.checkOnly = checkOnly;
	}

	public Boolean getIgnoreWarnings() {
		return ignoreWarnings;
	}

	public void setIgnoreWarnings(Boolean ignoreWarnings) {
		this.ignoreWarnings = ignoreWarnings;
	}

	public Boolean getPerformRetrieve() {
		return performRetrieve;
	}

	public void setPerformRetrieve(Boolean performRetrieve) {
		this.performRetrieve = performRetrieve;
	}

	public Boolean getPurgeOnDelete() {
		return purgeOnDelete;
	}

	public void setPurgeOnDelete(Boolean purgeOnDelete) {
		this.purgeOnDelete = purgeOnDelete;
	}

	public Boolean getRollbackOnError() {
		return rollbackOnError;
	}

	public void setRollbackOnError(Boolean rollbackOnError) {
		this.rollbackOnError = rollbackOnError;
	}

	public String getTestLevel() {
		return testLevel;
	}

	public void setTestLevel(String testLevel) {
		this.testLevel = testLevel;
	}

	public String[] getRunTests() {
		return runTests;
	}

	public void setRunTests(String[] runTests) {
		this.runTests = runTests;
	}

	public Boolean getSinglePackage() {
		return singlePackage;
	}

	public void setSinglePackage(Boolean singlePackage) {
		this.singlePackage = singlePackage;
	}

}
