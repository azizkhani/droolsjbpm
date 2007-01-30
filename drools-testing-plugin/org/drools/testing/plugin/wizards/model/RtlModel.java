package org.drools.testing.plugin.wizards.model;

import java.util.ArrayList;
import java.util.List;

import org.drools.lang.descr.PackageDescr;

public class RtlModel {

	private String fileName;
	private PackageDescr packageDescr;
	private List ruleDescrs = new ArrayList();
	
	public RtlModel () {
		
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public PackageDescr getPackageDescr() {
		return packageDescr;
	}

	public void setPackageDescr(PackageDescr packageDescr) {
		this.packageDescr = packageDescr;
	}

	public List getRuleDescrs() {
		return ruleDescrs;
	}

	public void setRuleDescrs(List ruleDescrs) {
		this.ruleDescrs = ruleDescrs;
	}
}
