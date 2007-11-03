package org.drools.analytics.components;

import org.drools.analytics.report.components.Cause;
import org.drools.base.evaluators.Operator;

/**
 * 
 * @author Toni Rikkola
 */
public abstract class Restriction extends AnalyticsComponent implements Cause {

	public static enum RestrictionType {
		LITERAL, VARIABLE, QUALIFIED_IDENTIFIER, RETURN_VALUE_RESTRICTION
	}

	private static int index = 0;

	private int patternId;
	private boolean patternIsNot;
	private int constraintId;

	// Id of the field that this restriction is related to.
	private int fieldId;

	protected Operator operator;

	public Restriction() {
		super(index++);
	}

	@Override
	public AnalyticsComponentType getComponentType() {
		return AnalyticsComponentType.RESTRICTION;
	}

	public CauseType getCauseType() {
		return Cause.CauseType.RESTRICTION;
	}

	public abstract RestrictionType getRestrictionType();

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public int getConstraintId() {
		return constraintId;
	}

	public void setConstraintId(int constraintId) {
		this.constraintId = constraintId;
	}

	public int getRuleId() {
		return ruleId;
	}

	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}

	public int getPatternId() {
		return patternId;
	}

	public void setPatternId(int patternId) {
		this.patternId = patternId;
	}

	public int getFieldId() {
		return fieldId;
	}

	public void setFieldId(int fieldId) {
		this.fieldId = fieldId;
	}

	public boolean isPatternIsNot() {
		return patternIsNot;
	}

	public void setPatternIsNot(boolean patternIsNot) {
		this.patternIsNot = patternIsNot;
	}
}
