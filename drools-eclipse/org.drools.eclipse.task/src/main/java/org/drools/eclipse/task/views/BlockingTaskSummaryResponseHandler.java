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

package org.drools.eclipse.task.views;

import java.util.List;

import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.TaskClientHandler.TaskSummaryResponseHandler;
import org.jbpm.task.service.responsehandlers.AbstractBlockingResponseHandler;

public class BlockingTaskSummaryResponseHandler extends AbstractBlockingResponseHandler implements TaskSummaryResponseHandler {
	private static final int DEFAULT_WAIT_TIME = 10000;

    private volatile List<TaskSummary> results;

    public synchronized void execute(List<TaskSummary> results) {
        this.results = results;
        setDone(true);
	}

	public synchronized List<TaskSummary> getResults() {
         // note that this method doesn't need to be synced because if waitTillDone returns true,
        // it means attachmentId is available
        boolean done = waitTillDone(DEFAULT_WAIT_TIME);

        if ( !done ) {
            throw new TimeoutException();
        }
        
        return results;
	}
}

