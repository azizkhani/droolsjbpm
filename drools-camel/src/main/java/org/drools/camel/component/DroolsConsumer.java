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
package org.drools.camel.component;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.drools.runtime.Channel;
import org.drools.runtime.KnowledgeRuntime;

/**
 * A consumer that consumes objects sent into channels of a drools
 * session
 * 
 * @author etirelli
 *
 */
public class DroolsConsumer extends DefaultConsumer {

    private DroolsEndpoint de; 
    private KnowledgeRuntime krt;
    private String channelId;

    public DroolsConsumer(Endpoint endpoint,
                          Processor processor ) {
        super( endpoint, processor );
        de = (DroolsEndpoint) endpoint;
        krt = (KnowledgeRuntime) de.getExecutor();
        channelId = de.getChannel();
    }

    @Override
    protected void doStop() throws Exception {
        krt.unregisterChannel( channelId );
        super.doStop();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        DroolsCamelChannel channel = new DroolsCamelChannel();
        krt.registerChannel( channelId, channel );
    }

    class DroolsCamelChannel implements Channel {
        public void send(Object pojo) {
            Exchange exchange = de.createExchange( pojo );
            try {
                getProcessor().process(exchange);
            } catch (Exception e) {
                handleException(e);
            }
        }
    }
    
}
