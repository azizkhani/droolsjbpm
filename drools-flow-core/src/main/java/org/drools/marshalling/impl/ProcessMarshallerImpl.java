package org.drools.marshalling.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.drools.common.InternalWorkingMemory;
import org.drools.process.instance.InternalProcessRuntime;
import org.drools.process.instance.WorkItem;
import org.drools.process.instance.WorkItemManager;
import org.drools.process.instance.impl.WorkItemImpl;
import org.drools.process.instance.timer.TimerInstance;
import org.drools.process.instance.timer.TimerManager;

public class ProcessMarshallerImpl implements ProcessMarshaller {

    public void writeProcessInstances(MarshallerWriteContext context) throws IOException {
        ObjectOutputStream stream = context.stream;
        List<org.drools.runtime.process.ProcessInstance> processInstances = new ArrayList<org.drools.runtime.process.ProcessInstance>( context.wm.getProcessInstances() );
        Collections.sort( processInstances,
                          new Comparator<org.drools.runtime.process.ProcessInstance>() {
                              public int compare(org.drools.runtime.process.ProcessInstance o1,
                            		  org.drools.runtime.process.ProcessInstance o2) {
                                  return (int) (o1.getId() - o2.getId());
                              }
                          } );
        for ( org.drools.runtime.process.ProcessInstance processInstance : processInstances ) {
            stream.writeShort(PersisterEnums.PROCESS_INSTANCE);
            String processType = processInstance.getProcess().getType();
            stream.writeUTF(processType);
            ProcessMarshallerRegistry.INSTANCE.getMarshaller(processType)
            	.writeProcessInstance(context, processInstance);
        }
        stream.writeShort( PersisterEnums.END );
    }

    public void writeProcessTimers(MarshallerWriteContext context) throws IOException {
        ObjectOutputStream stream = context.stream;

        TimerManager timerManager = ((InternalProcessRuntime) ((InternalWorkingMemory) context.wm).getProcessRuntime()).getTimerManager();
        long timerId = timerManager.internalGetTimerId();
        stream.writeLong( timerId );
        
        // need to think on how to fix this
        // stream.writeObject( timerManager.getTimerService() );
        
        List<TimerInstance> timers = new ArrayList<TimerInstance>( timerManager.getTimers() );
        Collections.sort( timers,
                          new Comparator<TimerInstance>() {
                              public int compare(TimerInstance o1,
                                                 TimerInstance o2) {
                                  return (int) (o2.getId() - o1.getId());
                              }
                          } );
        for ( TimerInstance timer : timers ) {
            stream.writeShort( PersisterEnums.TIMER );
            writeTimer( context,
                        timer );
        }
        stream.writeShort( PersisterEnums.END );
    }

    public void writeTimer(MarshallerWriteContext context,
                           TimerInstance timer) throws IOException {
        ObjectOutputStream stream = context.stream;
        stream.writeLong( timer.getId() );
        stream.writeLong( timer.getTimerId() );
        stream.writeLong( timer.getDelay() );
        stream.writeLong( timer.getPeriod() );
        stream.writeLong( timer.getProcessInstanceId() );
        stream.writeLong( timer.getActivated().getTime() );
        Date lastTriggered = timer.getLastTriggered();
        if ( lastTriggered != null ) {
            stream.writeBoolean( true );
            stream.writeLong( timer.getLastTriggered().getTime() );
        } else {
            stream.writeBoolean( false );
        }
    }

    public void writeWorkItems(MarshallerWriteContext context) throws IOException {
        ObjectOutputStream stream = context.stream;

        List<WorkItem> workItems = new ArrayList<WorkItem>(
    		((WorkItemManager) context.wm.getWorkItemManager()).getWorkItems() );
        Collections.sort( workItems,
                          new Comparator<WorkItem>() {
                              public int compare(WorkItem o1,
                                                 WorkItem o2) {
                                  return (int) (o2.getId() - o1.getId());
                              }
                          } );
        for ( WorkItem workItem : workItems ) {
            stream.writeShort( PersisterEnums.WORK_ITEM );
            writeWorkItem( context,
                           workItem );
        }
        stream.writeShort( PersisterEnums.END );
    }

    public static void writeWorkItem(MarshallerWriteContext context,
                                     WorkItem workItem) throws IOException {
         writeWorkItem(context, workItem, true);
    }

    public static void writeWorkItem(MarshallerWriteContext context,
                                     WorkItem workItem, boolean includeVariables) throws IOException {
        ObjectOutputStream stream = context.stream;
        stream.writeLong( workItem.getId() );
        stream.writeLong( workItem.getProcessInstanceId() );
        stream.writeUTF( workItem.getName() );
        stream.writeInt( workItem.getState() );

        if(includeVariables){
	        Map<String, Object> parameters = workItem.getParameters();
	        stream.writeInt( parameters.size() );
	        for ( Map.Entry<String, Object> entry : parameters.entrySet() ) {
	            stream.writeUTF( entry.getKey() );
	            stream.writeObject( entry.getValue() );
	        }
	    }
    }

    public void readProcessInstances(MarshallerReaderContext context) throws IOException {
        ObjectInputStream stream = context.stream;
        while ( stream.readShort() == PersisterEnums.PROCESS_INSTANCE ) {
        	String processType = stream.readUTF();
        	ProcessMarshallerRegistry.INSTANCE.getMarshaller(processType).readProcessInstance(context);
        }
    }

    public void readWorkItems(MarshallerReaderContext context) throws IOException {
        InternalWorkingMemory wm = context.wm;
        ObjectInputStream stream = context.stream;
        while ( stream.readShort() == PersisterEnums.WORK_ITEM ) {
            WorkItem workItem = readWorkItem( context );
            ((WorkItemManager) wm.getWorkItemManager()).internalAddWorkItem( workItem );
        }
    }

    public static WorkItem readWorkItem(MarshallerReaderContext context) throws IOException {
       return readWorkItem(context, true);
    }

    public static WorkItem readWorkItem(MarshallerReaderContext context, boolean includeVariables) throws IOException {
        ObjectInputStream stream = context.stream;

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setId( stream.readLong() );
        workItem.setProcessInstanceId( stream.readLong() );
        workItem.setName( stream.readUTF() );
        workItem.setState( stream.readInt() );

        if(includeVariables){
        int nbParameters = stream.readInt();

        for ( int i = 0; i < nbParameters; i++ ) {
            String name = stream.readUTF();
            try {
                Object value = stream.readObject();
                workItem.setParameter( name,
                                       value );
            } catch ( ClassNotFoundException e ) {
                throw new IllegalArgumentException( "Could not reload parameter " + name );
            }
        }
        }

        return workItem;
    }

    public void readProcessTimers(MarshallerReaderContext context) throws IOException {
        ObjectInputStream stream = context.stream;

        TimerManager timerManager = ((InternalProcessRuntime) ((InternalWorkingMemory) context.wm).getProcessRuntime()).getTimerManager();
        timerManager.internalSetTimerId( stream.readLong() );
        
        // still need to think on how to fix this.
//        TimerService service = (TimerService) stream.readObject();
//        timerManager.setTimerService( service );

        while ( stream.readShort() == PersisterEnums.TIMER ) {
            TimerInstance timer = readTimer( context );
            timerManager.internalAddTimer( timer );
        }
    }

    public TimerInstance readTimer(MarshallerReaderContext context) throws IOException {
        ObjectInputStream stream = context.stream;

        TimerInstance timer = new TimerInstance();
        timer.setId( stream.readLong() );
        timer.setTimerId( stream.readLong() );
        timer.setDelay( stream.readLong() );
        timer.setPeriod( stream.readLong() );
        timer.setProcessInstanceId( stream.readLong() );
        timer.setActivated( new Date( stream.readLong() ) );
        if ( stream.readBoolean() ) {
            timer.setLastTriggered( new Date( stream.readLong() ) );
        }
        return timer;
    }

}
