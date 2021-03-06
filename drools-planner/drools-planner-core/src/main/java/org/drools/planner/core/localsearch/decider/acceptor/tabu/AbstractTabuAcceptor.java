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

package org.drools.planner.core.localsearch.decider.acceptor.tabu;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.MoveScope;
import org.drools.planner.core.localsearch.decider.acceptor.AbstractAcceptor;
import org.drools.planner.core.localsearch.decider.acceptor.Acceptor;

/**
 * Abstract superclass for all Tabu Acceptors.
 * @see Acceptor
 * @author Geoffrey De Smet
 */
public abstract class AbstractTabuAcceptor extends AbstractAcceptor {

    protected int completeTabuSize = -1;
    protected int partialTabuSize = 0;
    protected boolean aspirationEnabled = true;

    protected Map<Object, Integer> tabuToStepIndexMap;
    protected List<Object> tabuSequenceList;

    public int getCompleteTabuSize() {
        return completeTabuSize;
    }

    public void setCompleteTabuSize(int completeTabuSize) {
        this.completeTabuSize = completeTabuSize;
    }

    public void setPartialTabuSize(int partialTabuSize) {
        this.partialTabuSize = partialTabuSize;
    }

    public void setAspirationEnabled(boolean aspirationEnabled) {
        this.aspirationEnabled = aspirationEnabled;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        if (completeTabuSize < 0) {
            throw new IllegalArgumentException("The completeTabuSize (" + completeTabuSize
                    + ") cannot be negative.");
        }
        if (partialTabuSize < 0) {
            throw new IllegalArgumentException("The partialTabuSize (" + partialTabuSize
                    + ") cannot be negative.");
        }
        if (completeTabuSize + partialTabuSize == 0) {
            throw new IllegalArgumentException("The sum of completeTabuSize and partialTabuSize should be at least 1.");
        }
        tabuToStepIndexMap = new HashMap<Object, Integer>(completeTabuSize + partialTabuSize);
        tabuSequenceList = new LinkedList<Object>();
    }

    public double calculateAcceptChance(MoveScope moveScope) {
        Collection<? extends Object> tabus = findTabu(moveScope);
        int maximumTabuStepIndex = -1;
        for (Object tabu : tabus) {
            Integer tabuStepIndexInteger = tabuToStepIndexMap.get(tabu);
            if (tabuStepIndexInteger != null) {
                maximumTabuStepIndex = Math.max(tabuStepIndexInteger, maximumTabuStepIndex);
            }
        }
        if (maximumTabuStepIndex < 0) {
            // The move isn't tabu at all
            return 1.0;
        }
        if (aspirationEnabled) {
            // Doesn't use the deciderScoreComparator because shifting penalties don't apply
            if (moveScope.getScore().compareTo(
                    moveScope.getStepScope().getLocalSearchSolverScope().getBestScore()) > 0) {
                logger.debug("    Proposed move ({}) is tabu, but aspiration undoes its tabu.", moveScope.getMove());
                return 1.0;
            }
        }
        int tabuStepCount = moveScope.getStepScope().getStepIndex() - maximumTabuStepIndex - 1;
        if (tabuStepCount < completeTabuSize) {
            logger.debug("    Proposed move ({}) is complete tabu.", moveScope.getMove());
            return 0.0;
        }
        double acceptChance = calculatePartialTabuAcceptChance(tabuStepCount - completeTabuSize);
        logger.debug("    Proposed move ({}) is partially tabu with accept chance ({}).",
                moveScope.getMove(), acceptChance);
        return acceptChance;
    }

    protected double calculatePartialTabuAcceptChance(int partialTabuTime) {
        // The + 1's are because acceptChance should not be 0.0 or 1.0
        // when (partialTabuTime == 0) or (partialTabuTime + 1 == partialTabuSize)
        return ((double) (partialTabuTime + 1)) / ((double) (partialTabuSize + 1));
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        Collection<? extends Object> tabus = findNewTabu(stepScope);
        for (Object tabu : tabus) {
            // required to push tabu to the end of the line
            if (tabuToStepIndexMap.containsKey(tabu)) {
                tabuToStepIndexMap.remove(tabu);
                tabuSequenceList.remove(tabu);
            }
            int maximumTabuListSize = completeTabuSize + partialTabuSize; // is at least 1
            while (tabuSequenceList.size() >= maximumTabuListSize) {
                Iterator<Object> it = tabuSequenceList.iterator();
                Object removeTabu = it.next();
                it.remove();
                tabuToStepIndexMap.remove(removeTabu);
            }
            tabuToStepIndexMap.put(tabu, stepScope.getStepIndex());
            tabuSequenceList.add(tabu);
        }
    }

    protected abstract Collection<? extends Object> findTabu(MoveScope moveScope);

    protected abstract Collection<? extends Object> findNewTabu(StepScope stepScope);

}
