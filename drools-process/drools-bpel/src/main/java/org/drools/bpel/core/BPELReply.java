package org.drools.bpel.core;

import org.drools.process.core.Work;
import org.drools.process.core.impl.WorkImpl;
import org.drools.workflow.core.node.WorkItemNode;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELReply extends WorkItemNode implements BPELActivity {

    private static final long serialVersionUID = 400L;
    
    private static final String PARTNER_LINK = "PartnerLink";
    private static final String PORT_TYPE = "PortType";
    private static final String OPERATION = "Operation";
    private static final String INPUT = "Message";
    private static final String FAULT_NAME = "FaultName";
    
    private SourceLink[] sourceLinks;
    private TargetLink[] targetLinks;
    private BPELCorrelation[] correlations;
    
	public BPELReply() {
        // TODO: a reply is not a simple web service invocation
        Work work = new WorkImpl();
        work.setName("WebServiceInvocation");
        setWork(work);
    }
    
    public String getPartnerLink() {
        return (String) getWork().getParameter(PARTNER_LINK);
    }

    public void setPartnerLink(String partnerLink) {
        getWork().setParameter(PARTNER_LINK, partnerLink);
    }

    public String getPortType() {
        return (String) getWork().getParameter(PORT_TYPE);
    }

    public void setPortType(String porttype) {
        getWork().setParameter(PORT_TYPE, porttype);
    }

    public String getOperation() {
        return (String) getWork().getParameter(OPERATION);
    }

    public void setOperation(String operation) {
        getWork().setParameter(OPERATION, operation);
    }

    public String getVariable() {
        return getInMapping(INPUT);
    }
    
    public void setFaultName(String faultName) {
        getWork().setParameter(FAULT_NAME, faultName);
    }
    
    public String getFaultName() {
        return (String) getWork().getParameter(FAULT_NAME);
    }

    public void setVariable(String variable) {
        addInMapping(INPUT, variable);
    }

    public SourceLink[] getSourceLinks() {
        return sourceLinks;
    }

    public void setSourceLinks(SourceLink[] sourceLinks) {
        this.sourceLinks = sourceLinks;
    }

    public TargetLink[] getTargetLinks() {
        return targetLinks;
    }

    public void setTargetLinks(TargetLink[] targetLinks) {
        this.targetLinks = targetLinks;
    }

    public BPELCorrelation[] getCorrelations() {
		return correlations;
	}

	public void setCorrelations(BPELCorrelation[] correlations) {
		this.correlations = correlations;
	}

}
