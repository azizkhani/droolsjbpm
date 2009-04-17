package org.drools.reteoo.filters;


import org.drools.reteoo.Evaluation;

public class FuzzyFilterStrategy extends ImperfectFilterStrategy {

	private static FuzzyFilterStrategy singleton = null;
	
	public static FuzzyFilterStrategy getInstance() {
		if (singleton == null)
			singleton = new FuzzyFilterStrategy();
		return singleton;
	}
	
	public FuzzyFilterStrategy() {
		
	}

	@Override
	public int doTry(final Evaluation eval) {
		return (eval.getInfoRate() == 1.0) ? PASS : HOLD;
	}

	@Override
	public boolean isAllowed(final Evaluation eval) {
		return (eval.getInfoRate() == 1.0);  
	}

	

}
