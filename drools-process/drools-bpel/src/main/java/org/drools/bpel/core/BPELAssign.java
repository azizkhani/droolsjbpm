package org.drools.bpel.core;

import org.drools.workflow.core.impl.DroolsConsequenceAction;
import org.drools.workflow.core.node.ActionNode;


/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELAssign extends ActionNode implements BPELActivity {

    private static final long serialVersionUID = 400L;
    
    private SourceLink[] sourceLinks;
    private TargetLink[] targetLinks;
    
    public void setAction(String assign) {
        setAction(new DroolsConsequenceAction("mvel", assign));
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

}
