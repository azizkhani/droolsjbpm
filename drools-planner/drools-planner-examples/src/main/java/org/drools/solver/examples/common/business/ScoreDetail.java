package org.drools.solver.examples.common.business;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.solver.core.score.constraint.ConstraintType;

/**
 * @author Geoffrey De Smet
 */
public class ScoreDetail implements Comparable<ScoreDetail> {

    private String ruleId;
    private ConstraintType constraintType;
    private double scoreTotal = 0.0;
    private int occurenceSize = 0;

    public ScoreDetail(String ruleId, ConstraintType constraintType) {
        this.ruleId = ruleId;
        this.constraintType = constraintType;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public ConstraintType getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(ConstraintType constraintType) {
        this.constraintType = constraintType;
    }

    public double getScoreTotal() {
        return scoreTotal;
    }

    public void setScoreTotal(double scoreTotal) {
        this.scoreTotal = scoreTotal;
    }

    public int getOccurenceSize() {
        return occurenceSize;
    }

    public void setOccurenceSize(int occurenceSize) {
        this.occurenceSize = occurenceSize;
    }


    public void addOccurenceScore(double occurenceScore) {
        scoreTotal += occurenceScore;
        occurenceSize++;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ScoreDetail) {
            ScoreDetail other = (ScoreDetail) o;
            return new EqualsBuilder()
                    .append(ruleId, other.ruleId)
                    .append(constraintType, other.constraintType)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(ruleId)
                .append(constraintType)
                .toHashCode();
    }

    public int compareTo(ScoreDetail other) {
        return new CompareToBuilder()
                .append(constraintType, other.constraintType)
                .append(ruleId, other.ruleId)
                .toComparison();
    }

    public String toString() {
        return ruleId + "/" + constraintType + "=" + scoreTotal + "(" + occurenceSize + ")";
    }

}
