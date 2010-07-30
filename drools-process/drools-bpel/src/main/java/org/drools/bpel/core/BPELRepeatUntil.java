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

import org.drools.workflow.core.Node;
import org.drools.workflow.core.impl.ConnectionImpl;
import org.drools.workflow.core.impl.ConstraintImpl;
import org.drools.workflow.core.node.CompositeNode;
import org.drools.workflow.core.node.Join;
import org.drools.workflow.core.node.Split;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELRepeatUntil extends CompositeNode implements BPELActivity {

    private static final long serialVersionUID = 510l;

    private Join join;
    private Split split;
    private SourceLink[] sourceLinks;
    private TargetLink[] targetLinks;

    public BPELRepeatUntil() {
        join = new Join();
        join.setType(Join.TYPE_XOR);
        join.setMetaData("hidden", true);
        addNode(join);
    	split = new Split();
        split.setType(Split.TYPE_XOR);
        split.setMetaData("hidden", true);
        addNode(split);
        linkIncomingConnections(
            Node.CONNECTION_DEFAULT_TYPE,
            new CompositeNode.NodeAndType(
                join, Node.CONNECTION_DEFAULT_TYPE));
    }
    
    public void setCondition(String condition) {
    	BPELEmpty empty = new BPELEmpty();
        empty.setMetaData("hidden", true);
        addNode(empty);
        linkOutgoingConnections(
            new CompositeNode.NodeAndType(
                empty, Node.CONNECTION_DEFAULT_TYPE),
            Node.CONNECTION_DEFAULT_TYPE);
        ConnectionImpl connection = new ConnectionImpl(
            split, Node.CONNECTION_DEFAULT_TYPE,
            empty, Node.CONNECTION_DEFAULT_TYPE);
        ConstraintImpl constraint = new ConstraintImpl();
        constraint.setConstraint(condition);
        constraint.setType("code");
        constraint.setDialect("XPath2.0");
        constraint.setPriority(1);
        split.setConstraint(connection, constraint);
        connection = new ConnectionImpl(
            split, Node.CONNECTION_DEFAULT_TYPE,
            join, Node.CONNECTION_DEFAULT_TYPE);
        constraint = new ConstraintImpl();
        constraint.setConstraint("true");
        constraint.setType("code");
        constraint.setDialect("mvel");
        constraint.setPriority(2);
        split.setConstraint(connection, constraint);
    }
    
    public void setActivity(BPELActivity activity) {
    	addNode(activity);
        new ConnectionImpl(
            join, Node.CONNECTION_DEFAULT_TYPE,
            activity, Node.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(
            activity, Node.CONNECTION_DEFAULT_TYPE,
            split, Node.CONNECTION_DEFAULT_TYPE);
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
