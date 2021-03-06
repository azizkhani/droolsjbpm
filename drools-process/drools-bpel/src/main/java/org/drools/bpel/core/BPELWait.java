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

import org.drools.workflow.core.node.TimerNode;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELWait extends TimerNode implements BPELActivity {

	private static final long serialVersionUID = 510l;

	private String forExpression;
	private String untilExpression;
	private SourceLink[] sourceLinks;
    private TargetLink[] targetLinks;

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

	public void setForExpression(String forExpression) {
		this.forExpression = forExpression;
	}

	public void setUntilExpression(String untilExpression) {
		this.untilExpression = untilExpression;
	}

	public String getForExpression() {
		return forExpression;
	}

	public String getUntilExpression() {
		return untilExpression;
	}
	
}
