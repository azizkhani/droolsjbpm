package org.drools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.drools.osworkflow.DroolsWorkflow;

import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.config.DefaultConfiguration;
import com.opensymphony.workflow.spi.Step;

public class Simple2ProcessTest extends TestCase {
	
	public void testSimpleProcess() {
		Workflow workflow = new DroolsWorkflow();
		DefaultConfiguration config = new DefaultConfiguration();
		workflow.setConfiguration(config);
		try {
			long workflowId = workflow.initialize("simple2", 1, null);
			
			Collection<Step> currentSteps = workflow.getCurrentSteps(workflowId);
			//verify we only have one current step
			assertEquals("Unexpected number of current steps", 1, currentSteps.size());
			//verify it's step 1
			Step currentStep = currentSteps.iterator().next();
			assertEquals("Unexpected current step", 1, currentStep.getStepId());

			int[] availableActions = workflow.getAvailableActions(workflowId, Collections.EMPTY_MAP);
			//verify we only have one available action
			assertEquals("Unexpected number of available actions", 1, availableActions.length);
			//verify it's action 2
			assertEquals("Unexpected available action", 2, availableActions[0]);
			
			workflow.doAction(workflowId, 2, null);
			
			currentSteps = workflow.getCurrentSteps(workflowId);
            //verify we have two current steps
            assertEquals("Unexpected number of current steps", 2, currentSteps.size());
            //verify it's step 2 and 3
            List<Integer> currentStepIds = new ArrayList<Integer>();
            for (Step step: currentSteps) {
                currentStepIds.add((int) step.getId());
            }
            assertTrue("Current step 2 not found", currentStepIds.contains(2));
            assertTrue("Current step 3 not found", currentStepIds.contains(3));
            
			availableActions = workflow.getAvailableActions(workflowId, Collections.EMPTY_MAP);
			//verify we have two available actions
			assertEquals("Unexpected number of available actions", 2, availableActions.length);
			//verify it's action 3 and 4
			List<Integer> availableActionIds = new ArrayList<Integer>();
            for (int availableAction: availableActions) {
                availableActionIds.add(availableAction);
            }
			assertTrue("Available action 3 not found", availableActionIds.contains(3));
            assertTrue("Available action 4 not found", availableActionIds.contains(4));
			
			workflow.doAction(workflowId, 3, null);
			
			currentSteps = workflow.getCurrentSteps(workflowId);
            //verify we only have one current step
            assertEquals("Unexpected number of current steps", 1, currentSteps.size());
            //verify it's step 3
            currentStep = currentSteps.iterator().next();
            assertEquals("Unexpected current step", 3, currentStep.getStepId());
            
            availableActions = workflow.getAvailableActions(workflowId, Collections.EMPTY_MAP);
            //verify we only have one available action
            assertEquals("Unexpected number of available actions", 1, availableActions.length);
            //verify it's action 4
            assertEquals("Unexpected available action", 4, availableActions[0]);
            
            workflow.doAction(workflowId, 4, null);
            
            currentSteps = workflow.getCurrentSteps(workflowId);
            //verify we only have one current step
            assertEquals("Unexpected number of current steps", 1, currentSteps.size());
            //verify it's step 4
            currentStep = currentSteps.iterator().next();
            assertEquals("Unexpected current step", 4, currentStep.getStepId());

            availableActions = workflow.getAvailableActions(workflowId, Collections.EMPTY_MAP);
			//verify we only have one available action
			assertEquals("Unexpected number of available actions", 0, availableActions.length);
			
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

}
