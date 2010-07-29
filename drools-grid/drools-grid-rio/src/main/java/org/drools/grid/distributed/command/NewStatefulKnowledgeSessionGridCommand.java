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

package org.drools.grid.distributed.command;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.KnowledgeBase;
import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.grid.DirectoryNodeService;


public class NewStatefulKnowledgeSessionGridCommand
    implements
    GenericCommand<StatefulKnowledgeSession> {

    private KnowledgeSessionConfiguration ksessionConf;
    private String kbaseId;
    
    public NewStatefulKnowledgeSessionGridCommand(KnowledgeSessionConfiguration ksessionConf) {
        this.ksessionConf = ksessionConf;
    }

    public NewStatefulKnowledgeSessionGridCommand(KnowledgeSessionConfiguration ksessionConf, String kbaseId) {
        this.ksessionConf = ksessionConf;
        this.kbaseId = kbaseId;
    }
    

    public StatefulKnowledgeSession execute(Context context) {

        KnowledgeBase kbase = ((KnowledgeCommandContext) context).getKnowledgeBase();
        DirectoryNodeService registry = (DirectoryNodeService)context.get("registry");
//        System.out.println("Inside Grid Command!!!!!!!!");
//        System.out.println("Registry = "+registry);
//        System.out.println("KbaseId = "+kbaseId);
        StatefulKnowledgeSession ksession;

        if( kbase == null){
            try {
                //@TODO: need to find the best way to injec the service in the command or the command executor.
                //lookup in the registry service.
                kbase = registry.lookupKBase(kbaseId);
            } catch (RemoteException ex) {
                Logger.getLogger(NewStatefulKnowledgeSessionGridCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if ( this.ksessionConf == null ) {
            System.out.println("Kbase inside the newStatefull Session = "+kbase);
            ksession = kbase.newStatefulKnowledgeSession();
            
        } else {
            ksession = kbase.newStatefulKnowledgeSession( this.ksessionConf, null );
        }

        return ksession;
    }

}
