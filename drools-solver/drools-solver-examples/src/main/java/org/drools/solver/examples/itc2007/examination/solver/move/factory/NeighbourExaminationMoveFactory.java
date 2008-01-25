package org.drools.solver.examples.itc2007.examination.solver.move.factory;

import java.util.ArrayList;
import java.util.List;

import org.drools.solver.core.move.Move;
import org.drools.solver.core.move.factory.AbstractMoveFactory;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.itc2007.examination.domain.Exam;
import org.drools.solver.examples.itc2007.examination.domain.Examination;
import org.drools.solver.examples.itc2007.examination.domain.Period;
import org.drools.solver.examples.itc2007.examination.domain.Room;
import org.drools.solver.examples.itc2007.examination.solver.move.PeriodChangeMove;
import org.drools.solver.examples.itc2007.examination.solver.move.RoomChangeMove;

/**
 * @author Geoffrey De Smet
 */
public class NeighbourExaminationMoveFactory extends AbstractMoveFactory {

    private static final int PERIOD_JUMP = 2;
    private static final int ROOM_JUMP = 2;

    public List<Move> createMoveList(Solution solution) {
        Examination examination = (Examination) solution;
        List<Period> periodList = examination.getPeriodList();
        List<Room> roomList = examination.getRoomList();
        List<Move> moveList = new ArrayList<Move>();
        for (Exam exam : examination.getExamList()) {
            if (exam.isCoincidenceLeader()) {
                for (Period period : periodList) {
                    int distance = calculateShortestDistance(
                            period.getPeriodIndex(), exam.getPeriod().getPeriodIndex(), periodList.size());
                    if (distance <= PERIOD_JUMP) {
                        moveList.add(new PeriodChangeMove(exam, period));
                    }
                }
            }
            for (Room room : roomList) {
                long distance = calculateShortestDistance(
                        room.getId(), exam.getRoom().getId(), roomList.size());
                if (distance <= ROOM_JUMP) {
                    moveList.add(new RoomChangeMove(exam, room));
                }
            }
        }
        return moveList;
        // TODO re-enable this stuff as it's a lot faster
//        int maximumMoveSize = 100;
//        // TODO not fair for first and last moves in move list
//        int randomStart = localSearchSolver.getRandom().nextInt(moveList.size() - maximumMoveSize);
//        return moveList.subList(randomStart, randomStart + maximumMoveSize).iterator();
    }

    public int calculateShortestDistance(int a, int b, int size) {
        int innerDistance = Math.abs(a - b);
        int outerDistance = size - innerDistance;
        return Math.min(innerDistance, outerDistance);
    }

    public long calculateShortestDistance(long a, long b, long size) {
        long innerDistance = Math.abs(a - b);
        long outerDistance = size - innerDistance;
        return Math.min(innerDistance, outerDistance);
    }

}