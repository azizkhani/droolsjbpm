package org.drools.solver.examples.itc2007.examination.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.CachedMoveFactory;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.domain.Room;
import org.drools.solver.examples.itc2007.examination.solver.move.RoomChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class RoomChangeMoveFactory extends CachedMoveFactory {

    public List<Move> createCachedMoveList(Solution solution) {
        Examination examination = (Examination) solution;
        List<Room> roomList = examination.getRoomList();
        List<Move> moveList = new ArrayList<Move>();
        for (Exam exam : examination.getExamList()) {
            for (Room room : roomList) {
                moveList.add(new RoomChangeMove(exam, room));
            }
        }
        return moveList;
    }

}