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

package org.drools.bpel.instance;

import org.drools.bpel.core.BPELProcess;
import org.drools.runtime.process.NodeInstance;
import org.drools.workflow.core.Node;
import org.drools.workflow.instance.impl.WorkflowProcessInstanceImpl;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELProcessInstance extends WorkflowProcessInstanceImpl {
    
    private static final long serialVersionUID = 510l;

    public BPELProcess getBPELProcess() {
        return (BPELProcess) getProcess();
    }
    
    // TODO: unification with signalEvent
//    public void acceptMessage(String partnerLink, String portType, String operation, String message) {
//        if (getState() == STATE_PENDING) {
//            setState(STATE_ACTIVE);
//        }
//        BPELReceive receive = findBPELReceive(partnerLink, portType, operation);
//        if (receive == null) {
//            throw new IllegalArgumentException(
//                "Could not find BPELReceive for " + partnerLink + ", " + portType + ", " + operation);
//        }
//        BPELActivity activity = receive;
//        List<BPELActivity> parents = new ArrayList<BPELActivity>(); 
//        while (!activity.getNodeContainer().equals(getBPELProcess())) {
//            activity = (BPELActivity) activity.getNodeContainer();
//            parents.add(0, activity);
//        }
//        NodeInstanceContainer nodeInstanceContainer = this;
//        for (Iterator<BPELActivity> iterator = parents.iterator(); iterator.hasNext(); ) {
//            BPELActivity parent = iterator.next();
//            NodeInstance nodeInstance = nodeInstanceContainer.getFirstNodeInstance(parent.getId());
//            if (nodeInstance != null) {
//                nodeInstanceContainer = (NodeInstanceContainer) nodeInstance;
//            } else if (receive.isCreateInstance()) {
//                nodeInstanceContainer = (NodeInstanceContainer) nodeInstanceContainer.getNodeInstance(parent);
//            } else {
//                // TODO: store message in cache of accepted messages
//                return;
//            }
//        }
//        BPELReceiveInstance bpelReceiveInstance = (BPELReceiveInstance) nodeInstanceContainer.getNodeInstance(receive);
//        ((EventSupport) getWorkingMemory()).getRuleFlowEventSupport().fireBeforeRuleFlowNodeTriggered(bpelReceiveInstance, (InternalWorkingMemory) getWorkingMemory());
//        bpelReceiveInstance.triggerCompleted(message);
//        ((EventSupport) getWorkingMemory()).getRuleFlowEventSupport().fireAfterRuleFlowNodeTriggered(bpelReceiveInstance, (InternalWorkingMemory) getWorkingMemory());
//    }
//    
//    private BPELReceive findBPELReceive(String partnerLink, String portType, String operation) {
//        return findBPELReceive(partnerLink, portType, operation, getBPELProcess().getActivity());
//    }
//    
//    private BPELReceive findBPELReceive(String partnerLink, String portType, String operation, Node node) {
//        if (node instanceof BPELReceive) {
//            BPELReceive receive = (BPELReceive) node;
//            if (receive.getPartnerLink().equals(partnerLink)
//                    && receive.getPortType().equals(portType)
//                    && receive.getOperation().equals(operation)) {
//                return receive;
//            }
//            return null;
//        }
//        if (node instanceof NodeContainer) {
//            Node[] nodes = ((NodeContainer) node).getNodes();
//            for (int i = 0; i < nodes.length; i++) {
//                BPELReceive result = findBPELReceive(partnerLink, portType, operation, nodes[i]);
//                if (result != null) {
//                    return result;
//                }
//            }
//        }
//        return null;
//    }

    @Override
    protected void internalStart() {
    	NodeInstance nodeInstance = getNodeInstance(getBPELProcess().getActivity());
        ((org.drools.workflow.instance.NodeInstance) nodeInstance)
        	.trigger(null, Node.CONNECTION_DEFAULT_TYPE);
    }

}
