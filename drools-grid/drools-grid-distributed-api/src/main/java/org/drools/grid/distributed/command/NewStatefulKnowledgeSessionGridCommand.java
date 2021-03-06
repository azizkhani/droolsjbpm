package org.drools.grid.distributed.command;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.KnowledgeBase;
import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;

public class NewStatefulKnowledgeSessionGridCommand
    implements
    GenericCommand<StatefulKnowledgeSession> {

    private KnowledgeSessionConfiguration ksessionConf;
    private String                        kbaseId;

    public NewStatefulKnowledgeSessionGridCommand(KnowledgeSessionConfiguration ksessionConf) {
        this.ksessionConf = ksessionConf;
    }

    public NewStatefulKnowledgeSessionGridCommand(KnowledgeSessionConfiguration ksessionConf,
                                                  String kbaseId) {
        this.ksessionConf = ksessionConf;
        this.kbaseId = kbaseId;
    }

    public StatefulKnowledgeSession execute(Context context) {

        KnowledgeBase kbase = ((KnowledgeCommandContext) context).getKnowledgeBase();
        DirectoryNodeService registry = (DirectoryNodeService) context.get( "registry" );
        StatefulKnowledgeSession ksession;

        if ( kbase == null ) {

            try {
                //@TODO: need to find the best way to injec the service in the command or the command executor.
                //lookup in the registry service.
                kbase = registry.lookupKBase( this.kbaseId );
            } catch ( RemoteException ex ) {
                Logger.getLogger( NewStatefulKnowledgeSessionGridCommand.class.getName() ).log( Level.SEVERE,
                                                                                                null,
                                                                                                ex );

            } catch ( ConnectorException ex ) {
                Logger.getLogger( NewStatefulKnowledgeSessionGridCommand.class.getName() ).log( Level.SEVERE,
                                                                                                null,
                                                                                                ex );
            }

        }
        if ( this.ksessionConf == null ) {
            ksession = kbase.newStatefulKnowledgeSession();

        } else {
            ksession = kbase.newStatefulKnowledgeSession( this.ksessionConf,
                                                          null );
        }

        return ksession;
    }

}
