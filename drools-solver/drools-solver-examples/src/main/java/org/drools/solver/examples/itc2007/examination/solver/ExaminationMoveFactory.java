package org.drools.solver.examples.itc2007.examination.solver;

import java.util.ArrayList;
import java.util.List;

import org.drools.solver.core.localsearch.decider.selector.CachedMoveListMoveFactory;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;
import org.drools.solver.examples.itc2007.examination.domain.Examination;

/**
 * @author Geoffrey De Smet
 */
public class ExaminationMoveFactory extends CachedMoveListMoveFactory {

    public List<Move> createMoveList(Solution solution) {
        Examination examination = (Examination) solution;
        List<Move> moveList = new ArrayList<Move>();
        // TODO FIXME

        return moveList;
    }

}