/*
 * Copyright 2008 JBoss Inc
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
 *
 */
package org.drools.time.impl;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.drools.process.instance.timer.TimerManager.ProcessJobContext;
import org.drools.time.Job;
import org.drools.time.JobContext;
import org.drools.time.JobHandle;
import org.drools.time.ProcessTimerPersistenceStrategy;
import org.drools.time.SessionClock;
import org.drools.time.TimerService;
import org.drools.time.Trigger;

/**
 * A default Scheduler implementation that uses the
 * JDK built-in ScheduledThreadPoolExecutor as the
 * scheduler and the system clock as the clock.
 * 
 */
public class JDKTimerService
    implements
    TimerService,
    SessionClock {
    
    private ScheduledThreadPoolExecutor scheduler;
    private ProcessTimerPersistenceStrategy processTimerPersistenceStrategy;

    public JDKTimerService() {
        this( null );
    }
    
    public JDKTimerService(ProcessTimerPersistenceStrategy processTimerPersistenceStrategy){
    	this(1, processTimerPersistenceStrategy);
    }

    public JDKTimerService(int size, ProcessTimerPersistenceStrategy processTimerPersistenceStrategy) {
        this.scheduler = new ScheduledThreadPoolExecutor( size );
        this.processTimerPersistenceStrategy = processTimerPersistenceStrategy;
    }

    /**
     * @inheritDoc
     */
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }
    
    public void shutdown() {
        // forcing a shutdownNow instead of a regular shutdown()
        // to avoid delays on shutdown. This is an irreversible 
        // operation anyway, called on session dispose.
        this.scheduler.shutdownNow();
    }

    public JobHandle scheduleJob(Job job,
                                 JobContext ctx,
                                 Trigger trigger) {

        Date date = trigger.nextFireTime();

        if ( date != null ) {
        	if( null != getProcessTimerPersistenceStrategy() 
        			&& ctx instanceof ProcessJobContext
        			&& trigger instanceof IntervalTrigger){
        		ProcessJobContext processContext = (ProcessJobContext) ctx;
        		IntervalTrigger intervalTrigger = (IntervalTrigger) trigger;
        		ProcessJobHandle jobHandle = new ProcessJobHandle(true, processContext.getProcessInstanceId());
        		getProcessTimerPersistenceStrategy().save(processContext, intervalTrigger);
        		return jobHandle;
        	}else {
                JDKJobHandle jobHandle = new JDKJobHandle();
	            JDKCallableJob callableJob = new JDKCallableJob( job,
                        ctx,
                        trigger,
                        jobHandle,
                        this.scheduler );
				ScheduledFuture future = schedule( date,
				          callableJob,
				          this.scheduler );
				jobHandle.setFuture( future );
				
				return jobHandle;
        	}
        } else {
            return null;
        }
    }

    public boolean removeJob(JobHandle jobHandle) {
    	if(getProcessTimerPersistenceStrategy() != null && jobHandle.isLongTermJob())
    		return true;
        return this.scheduler.remove( (Runnable) ((JDKJobHandle) jobHandle).getFuture() );
    }
    
    public boolean cancelJob(JobHandle jobHandle) {
    	if(getProcessTimerPersistenceStrategy() != null && jobHandle.isLongTermJob()) {
    		if(!(jobHandle instanceof ProcessJobHandle))
    			throw new IllegalArgumentException("Not yet implemented for handles of type " + jobHandle.getClass().getSimpleName());
			ProcessJobHandle processHandle = (ProcessJobHandle)jobHandle;
			return getProcessTimerPersistenceStrategy().remove(processHandle);
		}
    	return removeJob(jobHandle);
    }

    private static ScheduledFuture schedule(Date date,
                                            JDKCallableJob callableJob,
                                            ScheduledThreadPoolExecutor scheduler) {
        long then = date.getTime();
        long now = System.currentTimeMillis();
        ScheduledFuture<Void> future = null;
        if ( then >= now ) {
            future = scheduler.schedule( callableJob,
                                         then - now,
                                         TimeUnit.MILLISECONDS );
        } else {
            future = scheduler.schedule( callableJob,
                                         0,
                                         TimeUnit.MILLISECONDS );
        }
        return future;
    }

    public static class JDKCallableJob
        implements
        Callable<Void> {
        private final Job                         job;
        private final Trigger                     trigger;
        private final JobContext                  ctx;
        private final ScheduledThreadPoolExecutor scheduler;
        private final JDKJobHandle                handle;

        public JDKCallableJob(Job job,
                              JobContext ctx,
                              Trigger trigger,
                              JDKJobHandle handle,
                              ScheduledThreadPoolExecutor scheduler) {
            this.job = job;
            this.ctx = ctx;
            this.trigger = trigger;
            this.handle = handle;
            this.scheduler = scheduler;
        }

        public Void call() throws Exception {
            this.job.execute( this.ctx );

            // our triggers allow for flexible rescheduling
            Date date = this.trigger.nextFireTime();
            if ( date != null ) {
                ScheduledFuture<Void> future = schedule( date,
                                                         this,
                                                         this.scheduler );
                this.handle.setFuture( future );
            } 

            return null;
        }
    }

    public static class JDKJobHandle
        implements
        JobHandle {

        private static final long serialVersionUID = 6607880828232839927L;
        
        private ScheduledFuture<Void> future;

        public JDKJobHandle() {
        }

        public ScheduledFuture<Void> getFuture() {
            return future;
        }

        public void setFuture(ScheduledFuture<Void> future) {
            this.future = future;
        }

		public boolean isLongTermJob() {
			return false;
		}

    }

    public long getTimeToNextJob() {
        return 0;
    }

	public ProcessTimerPersistenceStrategy getProcessTimerPersistenceStrategy() {
		return processTimerPersistenceStrategy;
	}

	public void setProcessTimerPersistenceStrategy(
			ProcessTimerPersistenceStrategy processTimerPersistenceStrategy) {
		this.processTimerPersistenceStrategy = processTimerPersistenceStrategy;
	}

}
