package org.drools.eclipse.flow.ruleflow.view.property.action;
/*
 * Copyright 2005 JBoss Inc
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

import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.drools.eclipse.flow.common.view.property.EditListDialog;
import org.drools.workflow.core.DroolsAction;
import org.drools.workflow.core.WorkflowProcess;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for editing actions.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ActionsDialog extends EditListDialog {
    
	private WorkflowProcess process;

	protected ActionsDialog(Shell parentShell, WorkflowProcess process) {
		super(parentShell, "Edit Actions", ActionDialog.class);
		this.process = process;
	}

    protected Object createItem() {
        return new DroolsAction();
    }

    protected EditBeanDialog createEditItemDialog() {
    	return new ActionDialog(getShell(), process);
    }
}
