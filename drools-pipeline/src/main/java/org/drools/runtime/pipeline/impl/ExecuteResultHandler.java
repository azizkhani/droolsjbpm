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

package org.drools.runtime.pipeline.impl;

import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.PipelineContext;

public class ExecuteResultHandler extends BaseEmitter
    implements
    Action {

    public void handleResult(PipelineContext context,
                             Object object) {
        try {
            context.getResultHandler().handleResult( context.getResult() );
        } catch ( Exception e ) {
            handleException( this,
                             object,
                             e );
        }
    }

    public void receive(Object object,
                        PipelineContext context) {
        handleResult( context,
                      object );
        emit( object,
              context );
    }

}
