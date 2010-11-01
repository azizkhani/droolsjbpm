package org.drools.lang.dl.descr;

import java.util.Collection;

public class DL_RootNode extends DL_aCompositeDescr {

	private String type;
	
	public void addChild(DL_aBaseDescr child) {
		if (getChildren().size() == 0)
			super.addChild(child);
	}
	
	public void addChildren(Collection<DL_aBaseDescr> childs) {
		if (getChildren().size() == 0 && childs.size() == 1)
			super.addChildren(childs);
	}
		
	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
	
	 public String toString() {
	    	StringBuilder sb = new StringBuilder();
	    		sb.append(super.toString());
	    		sb.append("    >>>   ("+type+")\n");
	    	return sb.toString();
	    }
	
	
}
