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

package org.drools.osworkflow.xml;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.drools.osworkflow.core.node.StepNode;
import org.drools.workflow.core.Node;
import org.drools.xml.ExtensibleXmlParser;
import org.drools.xml.processes.AbstractNodeHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;

public class StepNodeHandler extends AbstractNodeHandler {

    protected Node createNode() {
        return new StepNode();
    }

    protected void handleNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        StepNode stepNode = (StepNode) node;
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            org.w3c.dom.Node subNode = nodeList.item(i);
            if (subNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element subElement = (Element) subNode;
                ActionDescriptor actionDescriptor = 
                    DescriptorFactory.getFactory().createActionDescriptor(subElement);
                stepNode.addAction(actionDescriptor);
            }
        }
    }
    
    public void writeNode(Node node, StringBuilder xmlDump, boolean includeMeta) {
        StepNode stepNode = (StepNode) node;
        writeNode("step", stepNode, xmlDump, includeMeta);
        xmlDump.append(">" + EOL);
        
        for (ActionDescriptor action: stepNode.getActions()) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            action.writeXML(writer, 3);
            writer.close();
            xmlDump.append(stringWriter.toString());
        }
        endNode("step", xmlDump);
    }

    public Class<?> generateNodeFor() {
        return StepNode.class;
    }

}
