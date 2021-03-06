/*
	Milyn - Copyright (C) 2006

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.event.report;

import org.milyn.SmooksException;
import org.milyn.payload.JavaResult;
import org.milyn.payload.StringResult;
import org.milyn.payload.FilterResult;
import org.milyn.container.ExecutionContext;
import org.milyn.assertion.AssertArgument;
import org.milyn.delivery.*;
import org.milyn.delivery.dom.DOMContentDeliveryConfig;
import org.milyn.delivery.dom.serialize.DefaultSerializationUnit;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.event.BasicExecutionEventListener;
import org.milyn.event.ElementProcessingEvent;
import org.milyn.event.ExecutionEvent;
import org.milyn.event.ResourceBasedEvent;
import org.milyn.event.report.model.*;
import org.milyn.event.types.*;
import org.milyn.xml.DomUtils;
import org.w3c.dom.Element;

import javax.xml.transform.Result;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Abstract execution report generator.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class AbstractReportGenerator extends BasicExecutionEventListener {

    private ReportConfiguration reportConfiguration;

    private Report report;

    private ExecutionContext executionContext;
    private int messageNodeCounter = 0;
    private int reportInfoNodeCounter = 0;
    private List<ExecutionEvent> preProcessingEvents = new ArrayList<ExecutionEvent>();
    private List<ExecutionEvent> processingEvents = new ArrayList<ExecutionEvent>();
    private Stack<ReportNode> reportNodeStack = new Stack<ReportNode>();
    private List<ReportNode> allNodes = new ArrayList<ReportNode>();
    protected static final DefaultSerializationUnit domSerializer = new DefaultSerializationUnit();

    protected AbstractReportGenerator(ReportConfiguration reportConfiguration) {
        AssertArgument.isNotNull(reportConfiguration, "reportConfiguration");
        this.reportConfiguration = reportConfiguration;
        setFilterEvents(reportConfiguration.getFilterEvents());
    }

    public ReportConfiguration getReportConfiguration() {
        return reportConfiguration;
    }

    public Writer getOutputWriter() {
        return reportConfiguration.getOutputWriter();
    }

    /**
     * Process the {@link org.milyn.event.ExecutionEvent}.
     *
     * @param event The {@link org.milyn.event.ExecutionEvent}.
     */
    public void onEvent(ExecutionEvent event) {
        AssertArgument.isNotNull(event, "event");

        if(ignoreEvent(event)) {
            // Don't capture this event...
            return;
        }

        if (event instanceof FilterLifecycleEvent) {
            processLifecycleEvent((FilterLifecycleEvent) event);
        } else if (event instanceof ElementPresentEvent) {
            ReportNode node = new ReportNode((ElementPresentEvent) event);
            allNodes.add(node);
            processNewElementEvent(node);
        } else {
            if (reportNodeStack.isEmpty()) {
                // We haven't started to process the message/phase yet....
                preProcessingEvents.add(event);
            } else if (event instanceof ElementProcessingEvent) {
                // We have started processing the message/phase, so attach the event to the ReportNode
                // associated with the event element...
                ReportNode reportNode = getReportNode(((ElementProcessingEvent) event).getElement());

                if (reportNode != null) {
                    reportNode.elementProcessingEvents.add(event);
                }
            } else {
                processingEvents.add(event);
            }
        }
    }

    protected boolean ignoreEvent(ExecutionEvent event) {
        if(!super.ignoreEvent(event)) {
            if (event instanceof ResourceBasedEvent) {
                if (!reportConfiguration.showDefaultAppliedResources()) {
                    return ((ResourceBasedEvent) event).getResourceConfig().isDefaultResource();
                }
            }

            return false;
        }
        
        return true;
    }

    private void processLifecycleEvent(FilterLifecycleEvent event) {

        try {
            if (event.getEventType() != FilterLifecycleEvent.EventType.FINISHED) {
                ContentDeliveryConfig deliveryConfig = Filter.getCurrentExecutionContext().getDeliveryConfig();
                if (event instanceof DOMFilterLifecycleEvent) {
                    DOMFilterLifecycleEvent domEvent = (DOMFilterLifecycleEvent) event;
                    if (domEvent.getDOMEventType() == DOMFilterLifecycleEvent.DOMEventType.PROCESSING_STARTED) {
                        // Assembly phase is done... output assembly report just at the start of the
                        // processing phase...
                        mapMessageNodeVists(((DOMReport)report).getAssemblies());
                    } else if (domEvent.getDOMEventType() == DOMFilterLifecycleEvent.DOMEventType.SERIALIZATION_STARTED) {
                        // Processing phase is done (if it was)... output processing report just at the start of the
                        // serialization phase...
                        mapMessageNodeVists(report.getProcessings());
                    }
                } else if (event.getEventType() == FilterLifecycleEvent.EventType.STARTED) {
                    executionContext = Filter.getCurrentExecutionContext();

                    if(deliveryConfig instanceof DOMContentDeliveryConfig) {
                        report = new DOMReport();
                    } else {
                        report = new Report();
                    }
                    // Output the configuration builder events...
                    mapConfigBuilderEvents(deliveryConfig.getConfigBuilderEvents());
                }
            } else {
                processFinishEvent();
            }
        } catch (IOException e) {
            throw new SmooksException("Failed to write report.", e);
        }
    }

    private void processFinishEvent() throws IOException {
        if(report instanceof DOMReport) {
            if(report.getProcessings().isEmpty()) {
                mapMessageNodeVists(report.getProcessings());
            } else {
                mapMessageNodeVists(((DOMReport)report).getSerializations());
            }
        } else {
            mapMessageNodeVists(report.getProcessings());
        }

        List<ResultNode> resultNodes = new ArrayList<ResultNode>();
        Result[] results = FilterResult.getResults(executionContext);
        report.setResults(resultNodes);
        if(results != null) {
            for(int i = 0; i < results.length; i++) {
                ResultNode resultNode = new ResultNode();

                resultNodes.add(resultNode);
                if(results[i] instanceof JavaResult) {
                    resultNode.setSummary("This Smooks Filtering operation produced a JavaResult.  The following is an XML serialization of the JavaResult bean Map entries.");
                } else if(results[i]instanceof StringResult) {
                    resultNode.setSummary("This Smooks Filtering operation produced the following StreamResult.");
                } else {
                    resultNode.setSummary("Cannot show Smooks Filtering Result.  Modify the code and use a '" + StringResult.class.getName() + "' Result in the call to the Smooks.filter() method.");
                }
                resultNode.setDetail(results[i].toString());
            }
        }

        try {
            applyTemplate(report);
        } finally {
            Writer writer = reportConfiguration.getOutputWriter();
            try {
                writer.flush();
            } finally {
                if(reportConfiguration.autoCloseWriter()) {
                    writer.close();
                }
            }
        }
    }

    private void processNewElementEvent(ReportNode node) {
        if (reportNodeStack.isEmpty()) {
            reportNodeStack.push(node);
        } else {
            ReportNode head = reportNodeStack.peek();

            while (head != null && node.depth <= head.depth) {
                // element associated with the current head node on the stack is closed. Drop back
                // a level in the report model before adding the new node...
                reportNodeStack.pop();
                if (!reportNodeStack.isEmpty()) {
                    head = reportNodeStack.peek();
                } else {
                    head = null;
                }
            }

            node.parent = head;
            if (node.parent != null) {
                node.parent.children.add(node);
            }
            reportNodeStack.push(node);
        }
    }

    private void mapConfigBuilderEvents(List<ConfigBuilderEvent> configBuilderEvents) {
    }

    private void mapMessageNodeVists(List<MessageNode> visits) throws IOException {
        if (!allNodes.isEmpty()) {
            mapNode(reportNodeStack.elementAt(0), visits);
        }

        // And clear everything...
        preProcessingEvents.clear();
        processingEvents.clear();
        reportNodeStack.clear();
        allNodes.clear();
    }

    private void mapNode(ReportNode reportNode, List<MessageNode> visits) throws IOException {
        List<ReportNode> children;
        MessageNode messageNode;

        messageNode = new MessageNode();
        messageNode.setNodeId(messageNodeCounter);
        messageNode.setElementName(reportNode.getElementName());
        messageNode.setVisitBefore(true);
        messageNode.setDepth(reportNode.getDepth());
        mapNodeEvents(VisitSequence.BEFORE, reportNode, messageNode);
        visits.add(messageNode);
        messageNodeCounter++;

        children = reportNode.children;
        for (ReportNode child : children) {
            mapNode(child, visits);
        }

        messageNode = new MessageNode();
        messageNode.setNodeId(messageNodeCounter);
        messageNode.setElementName(reportNode.getElementName());
        messageNode.setVisitBefore(false);
        messageNode.setDepth(reportNode.getDepth());
        mapNodeEvents(VisitSequence.AFTER, reportNode, messageNode);
        visits.add(messageNode);
        messageNodeCounter++;
    }

    private void mapNodeEvents(VisitSequence visitSequence, ReportNode reportNode, MessageNode messageNode) {
        List<ExecutionEvent> events = reportNode.getElementProcessingEvents();

        for (ExecutionEvent event : events) {
            if (event instanceof ElementVisitEvent) {
                ElementVisitEvent visitEvent = (ElementVisitEvent) event;

                if (visitEvent.getSequence() == visitSequence) {
                    ReportInfoNode reportInfoNode = new ReportInfoNode();
                    ContentHandlerConfigMap configMapping = ((ElementVisitEvent) event).getConfigMapping();

                    messageNode.addExecInfoNode(reportInfoNode);

                    reportInfoNode.setNodeId(reportInfoNodeCounter);
                    reportInfoNode.setSummary(configMapping.getContentHandler().getClass().getSimpleName() + ": " + visitEvent.getReportSummary());
                    reportInfoNode.setDetail(visitEvent.getReportDetail());
                    reportInfoNode.setResourceXML(configMapping.getResourceConfig().toXML());
                    reportInfoNode.setContextState(visitEvent.getExecutionContextState());

                    reportInfoNodeCounter++;
                }
            }
        }
    }

    public abstract void applyTemplate(Report report) throws IOException;

    private ReportNode getReportNode(Object element) {
        for (ReportNode node : allNodes) {
            if (node.element == element) {
                return node;
            }
        }

        return null;
    }

    public class ReportNode {

        private ReportNode parent;
        private List<ReportNode> children = new ArrayList<ReportNode>();
        private Object element;
        private int depth;
        private List<ExecutionEvent> elementProcessingEvents = new ArrayList<ExecutionEvent>();

        public ReportNode(ElementPresentEvent eventPresentEvent) {
            this.element = eventPresentEvent.getElement();
            this.depth = eventPresentEvent.getDepth();
        }

        public String getElementName() {
            if(element instanceof SAXElement) {
                return ((SAXElement)element).getName().getLocalPart();
            } else {
                return DomUtils.getName((Element)element);
            }
        }

        public String toString() {
            return (element + " (depth " + depth + ")");
        }

        public ReportNode getParent() {
            return parent;
        }

        public List<ReportNode> getChildren() {
            return children;
        }

        public Object getElement() {
            return element;
        }

        public int getDepth() {
            return depth;
        }

        public List<ExecutionEvent> getElementProcessingEvents() {
            return elementProcessingEvents;
        }
    }
}
