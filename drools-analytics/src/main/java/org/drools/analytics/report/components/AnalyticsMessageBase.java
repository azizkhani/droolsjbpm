package org.drools.analytics.report.components;

import java.io.Serializable;

/**
 * 
 * @author Toni Rikkola
 */
abstract public class AnalyticsMessageBase implements Serializable,
		Comparable<AnalyticsMessageBase> {
	private static final long serialVersionUID = 9190003495068712452L;

	private static int index = 0;

	public static enum Severity {
		NOTE("Note", "Notes"), WARNING("Warning", "Warnings"), ERROR("Error",
				"Errors");

		private String singular;
		private String tuple;

		private Severity(String singular, String tuple) {
			this.singular = singular;
			this.tuple = tuple;
		}

		public String getSingular() {
			return singular;
		}

		public String getTuple() {
			return tuple;
		}

		@Override
		public String toString() {
			return singular;
		}
	}

	public static enum MessageType {
		NOT_SPECIFIED, RANGE_CHECK, MISSING_EQUALITY, REDUNDANCY, SUBSUMPTION, MISSING_COMPONENT, OPTIMISATION
	}

	protected Severity severity;
	protected MessageType messageType;

	protected int id = index++;
	protected Cause faulty;
	protected String message;

	public int compareTo(AnalyticsMessageBase o) {
		if (id == o.getId()) {
			return 0;
		}

		return (id > o.getId() ? 1 : -1);
	}

	protected AnalyticsMessageBase(Severity severity, MessageType messageType,
			Cause faulty, String message) {
		this.severity = severity;
		this.messageType = messageType;
		this.faulty = faulty;
		this.message = message;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Cause getFaulty() {
		return faulty;
	}

	public void setFaulty(Cause faulty) {
		this.faulty = faulty;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer(severity.singular);

		str.append(" id = ");
		str.append(id);
		str.append(":\n");

		if (faulty != null) {
			str.append("faulty : ");
			str.append(faulty);
			str.append(", ");
		}

		str.append(message);

		str.append("\t]");

		return str.toString();
	}
}
