/**
 *
 */
package org.drools.grid.internal.responsehandlers;

import java.util.List;
import java.util.Map;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageResponseHandler;
import org.drools.grid.internal.commands.SimpleCommand;

public class BlockingMessageDirectoryMapRequestResponseHandler extends AbstractBlockingResponseHandler
    implements
    MessageResponseHandler {
    private static final int ATTACHMENT_ID_WAIT_TIME = 100000;
    private static final int CONTENT_ID_WAIT_TIME    = 50000;
    private Map<String,String> directoryMap;
    private volatile Message message;

    public synchronized void receive(Message message) {
        this.message = message;
        
        setDone( true );
    }

   

    public Message getMessage() {
        boolean done = waitTillDone( CONTENT_ID_WAIT_TIME );

        if ( !done ) {
            throw new RuntimeException( "Timeout : unable to retrieve Object Id" );
        }

        return this.message;
    }

}