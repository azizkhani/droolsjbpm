/**
 * Copyright 2010 JBoss Inc
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

package org.drools.planner.examples.examination.solver.move;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.WorkingMemory;
import org.drools.planner.core.localsearch.decider.acceptor.tabu.TabuPropertyEnabled;
import org.drools.planner.core.move.Move;
import org.drools.planner.examples.examination.domain.Exam;
import org.drools.planner.examples.examination.domain.Room;

/**
 * @author Geoffrey De Smet
 */
public class RoomChangeMove implements Move, TabuPropertyEnabled {

    private Exam exam;
    private Room toRoom;

    public RoomChangeMove(Exam exam, Room toRoom) {
        this.exam = exam;
        this.toRoom = toRoom;
    }

    public boolean isMoveDoable(WorkingMemory workingMemory) {
        return !ObjectUtils.equals(exam.getRoom(), toRoom);
    }

    public Move createUndoMove(WorkingMemory workingMemory) {
        return new RoomChangeMove(exam, exam.getRoom());
    }

    public void doMove(WorkingMemory workingMemory) {
        ExaminationMoveHelper.moveRoom(workingMemory, exam, toRoom);
    }

    public Collection<? extends Object> getTabuProperties() {
        return Collections.singletonList(exam);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof RoomChangeMove) {
            RoomChangeMove other = (RoomChangeMove) o;
            return new EqualsBuilder()
                    .append(exam, other.exam)
                    .append(toRoom, other.toRoom)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(exam)
                .append(toRoom)
                .toHashCode();
    }

    public String toString() {
        return exam + " => " + toRoom;
    }

}