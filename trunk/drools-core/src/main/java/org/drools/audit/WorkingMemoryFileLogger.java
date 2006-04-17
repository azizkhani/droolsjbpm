package org.drools.audit;

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

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.drools.WorkingMemory;
import org.drools.audit.event.LogEvent;

import com.thoughtworks.xstream.XStream;

/**
 * A logger of events generated by a working memory.
 * It stores its information in a file that can be specified.
 * All the events logged are written to the file when the
 * writeToDisk() method is invoked.  The log will contain all
 * the events logged serialized to XML using XStream.
 * Every time a new logger is created, the old event log will
 * be overwritten.
 * 
 * TODO: make this class more scalable, for example
 *  - logging to several files if log becomes too large
 *  - automatically write updates to file at certain time intervals
 *  - ...
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen </a>
 */
public class WorkingMemoryFileLogger extends WorkingMemoryLogger {

	private List events = new ArrayList();
	private String fileName= "event";
	private int maxEventsInMemory = 1000;
	private int nbOfFile = 0;
	
	/**
	 * Creates a new WorkingMemoryFileLogger for the given working memory.
	 * @param workingMemory
	 */
	public WorkingMemoryFileLogger(WorkingMemory workingMemory) {
		super(workingMemory);
	}
	
	/**
	 * Sets the name of the file the events are logged in.
	 * No extensions should be given since .log is automatically appended
	 * to the file name.
	 * The default is an event.log file in the current working directory.
	 * This can be a path relative to the current working directory
	 * (e.g. "mydir/subDir/myLogFile"), or an absolute path 
	 * (e.g. "C:/myLogFile").
	 * 
	 * @param fileName The name of the file the events should be logged in.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * All events in the log are written to file.
	 * The log is automatically cleared afterwards.
	 */
	public void writeToDisk() {
		try {
			XStream xstream = new XStream();
			ObjectOutputStream out = xstream.createObjectOutputStream(
				new FileWriter(fileName + (nbOfFile == 0 ? ".log" : nbOfFile + ".log"), false));
			out.writeObject(events);
			out.close();
			nbOfFile++;
			clear();
		} catch (FileNotFoundException exc) {
			throw new RuntimeException("Could not create the log file.  Please make sure that directory that the log file should be placed in does exist.");
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}
	
	/**
	 * Clears all the events in the log.
	 */
	public void clear() {
		events.clear();
	}

	/**
	 * Sets the maximum number of log events that are allowed in memory.
	 * If this number is reached, all events are written to file.
	 * The default is 1000.
	 * 
	 * @param maxEventsInMemory The maximum number of events in memory.
	 */
	public void setMaxEventsInMemory(int maxEventsInMemory) {
		this.maxEventsInMemory = maxEventsInMemory;
	}

	/**
	 * @see org.drools.audit.WorkingMemoryLogger
	 */
	public void logEventCreated(LogEvent logEvent) {
		events.add(logEvent);
		if (events.size() > maxEventsInMemory) {
			writeToDisk();
		}
	}
	
	public void finalize() {
		writeToDisk();
	}
}
