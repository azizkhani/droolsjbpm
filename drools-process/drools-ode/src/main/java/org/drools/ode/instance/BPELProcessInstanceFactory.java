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

package org.drools.ode.instance;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ode.utils.DOMUtils;
import org.drools.WorkingMemory;
import org.drools.common.InternalWorkingMemory;
import org.drools.definition.process.Process;
import org.drools.process.instance.ProcessInstance;
import org.drools.process.instance.ProcessInstanceFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class BPELProcessInstanceFactory implements ProcessInstanceFactory, Externalizable {

    private static final long serialVersionUID = 510l;

    private synchronized EmbeddedBPELServer initServer(WorkingMemory workingMemory) {
    	EmbeddedBPELServer server = (EmbeddedBPELServer)
    		workingMemory.getEnvironment().get("ODE-Server");
    	if (server == null) {
    		System.out.println("Initializing BPEL server");
    		server = new EmbeddedBPELServer(workingMemory);
    		BPELEventListener listener = new BPELEventListener(workingMemory);
    		server.addEventListener(listener);
    		workingMemory.getEnvironment().set("ODE-Server", server);
    	}
    	return server;
    }

	public ProcessInstance createProcessInstance(Process process,
			                                     WorkingMemory workingMemory,
			                                     Map<String, Object> parameters) {
		EmbeddedBPELServer server = initServer(workingMemory);
		BPELProcessInstance processInstance = new BPELProcessInstance();
		processInstance.setWorkingMemory( (InternalWorkingMemory) workingMemory );
		processInstance.setProcess( process );
		
		String operation = (String) parameters.get("Operation"); 
		String bodyString = (String) parameters.get("Body");
		System.out.println("Starting BPEL process "
			+ process.getName() + " [" + process.getPackageName() + "]");
		try {
			Element body = DOMUtils.stringToDOM(bodyString);
			QName processQName = new QName(process.getPackageName(),
				process.getName().substring(process.getPackageName().length() + 2));
	        try {
				server.invoke(processQName, operation, body);
				server.waitForBlocking();
			} catch (Exception e) {
				System.err.println("Could not invoke service " + processQName
					+ " -> " + operation + " with " + bodyString);
				e.printStackTrace();
			}
		} catch (SAXException e) {
			System.err.println("Could not parse 'Body' " + bodyString);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Could not parse 'Body' " + bodyString);
			e.printStackTrace();
		}
		return null;
	}

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
    }


}
