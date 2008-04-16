package org.drools.solver.core;

import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public interface Solver {

    void setStartingSolution(Solution startingSolution);

    double getBestScore();
    Solution getBestSolution();

    /**
     * @return the amount of millis spend since this solver started
     */
    long getTimeMillisSpend();

    void solve();
    
}
