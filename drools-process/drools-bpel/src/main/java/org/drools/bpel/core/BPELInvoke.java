/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.bpel.core;

import org.drools.process.core.Work;
import org.drools.process.core.impl.WorkImpl;
import org.drools.workflow.core.node.WorkItemNode;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELInvoke extends WorkItemNode implements BPELActivity, BPELFaultHandlerContainer {

    private static final long serialVersionUID = 510l;
    
    private static final String PARTNER_LINK = "PartnerLink";
    private static final String PORT_TYPE = "PortType";
    private static final String OPERATION = "Operation";
    private static final String INPUT = "Message";
    private static final String OUTPUT = "Result";
    
    private SourceLink[] sourceLinks;
    private TargetLink[] targetLinks;
    private BPELFaultHandler[] catches;
    private BPELFaultHandler catchAll;
    private BPELCorrelation[] correlations;
    private BPELCompensationHandler compensationHandler;
    
    public BPELInvoke() {
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

    public String getInputVariable() {
        return getInMapping(INPUT);
    }

    public void setInputVariable(String inputVariable) {
        addInMapping(INPUT, inputVariable);
    }

    public String getOutputVariable() {
        return getOutMapping(OUTPUT);
    }

    public void setOutputVariable(String outputVariable) {
        addOutMapping(OUTPUT, outputVariable);
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

	public BPELFaultHandler[] getCatches() {
		return catches;
	}

	public void setCatches(BPELFaultHandler[] catches) {
		this.catches = catches;
	}

	public BPELFaultHandler getCatchAll() {
		return catchAll;
	}

	public void setCatchAll(BPELFaultHandler catchAll) {
		this.catchAll = catchAll;
	}// TODO: BPELInvoke

	public BPELCorrelation[] getCorrelations() {
		return correlations;
	}

	public void setCorrelations(BPELCorrelation[] correlations) {
		this.correlations = correlations;
	}

	public BPELCompensationHandler getCompensationHandler() {
		return compensationHandler;
	}

	public void setCompensationHandler(BPELCompensationHandler compensationHandler) {
		this.compensationHandler = compensationHandler;
	}

}
