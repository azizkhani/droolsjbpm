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

/**
 *
 */
package org.drools.task.service.responsehandlers;

import org.drools.eventmessaging.EventResponseHandler;
import org.drools.eventmessaging.Payload;

public class BlockingEventResponseHandler extends AbstractBlockingResponseHandler implements EventResponseHandler {
    private static final int PAYLOAD_WAIT_TIME = 10000;

    private volatile Payload payload;

    public synchronized void execute(Payload payload) {
        this.payload = payload;
        setDone(true);
    }

    public Payload getPayload() {
        // note that this method doesn't need to be synced because if waitTillDone returns true,
        // it means payload is available 
        boolean done = waitTillDone(PAYLOAD_WAIT_TIME);

        if (!done) {
            throw new RuntimeException("Timeout : unable to retrieve event payload");
        }

        return payload;
    }
    
    public boolean isRemove() {
    	return true;
    }
}