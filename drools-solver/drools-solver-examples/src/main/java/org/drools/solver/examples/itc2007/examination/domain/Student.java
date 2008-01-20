package org.drools.solver.examples.itc2007.examination.domain;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * Not used during score calculation, so not inserted into the working memory.
 * @author Geoffrey De Smet
 */
public class Student extends AbstractPersistable implements Comparable<Student> {
    
    public int compareTo(Student other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

}
