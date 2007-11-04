package org.drools.solver.examples.itc2007.examination.domain;

import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.drools.solver.examples.common.domain.AbstractPersistable;

/**
 * @author Geoffrey De Smet
 */
public class Topic extends AbstractPersistable implements Comparable<Topic> {

    private int duration;
    private List<Student> studentList;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }

    public int getStudentListSize() {
        return studentList.size();
    }

    public int compareTo(Topic other) {
        return new CompareToBuilder()
                .append(id, other.id)
                .toComparison();
    }

    public String toString() {
        return id + " {" + duration + ", " + getStudentListSize() + "}";
    }

}
