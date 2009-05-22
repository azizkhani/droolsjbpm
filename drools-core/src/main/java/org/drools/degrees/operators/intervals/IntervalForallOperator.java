package org.drools.degrees.operators.intervals;

import java.util.Collection;

import org.drools.degrees.IDegree;
import org.drools.degrees.IntervalDegree;
import org.drools.degrees.factory.IDegreeFactory;
import org.drools.degrees.factory.IntervalDegreeFactory;
import org.drools.degrees.operators.IDegreeCombiner;

public class IntervalForallOperator implements IDegreeCombiner {

	public IDegree eval(IDegree[] args, IDegreeFactory factory) {
		if (args == null || args.length == 0)
			return factory == null ? IntervalDegreeFactory.UNKNOWN : factory.Unknown();		
		float tau = 1.0f;
		float phi = 0.0f;
		for (IDegree deg : args) {
			IntervalDegree ival = (IntervalDegree) deg;
			if (ival == null) 
				continue;
			tau = Math.min(tau,ival.getTau());
			phi = Math.max(phi,ival.getPhi());
		}
		return new IntervalDegree(tau,1.0f-phi);
	}

	public IDegree eval(Collection<? extends IDegree> args, IDegreeFactory factory) {
		if (args == null || args.size() == 0)
			return factory == null ? IntervalDegreeFactory.UNKNOWN : factory.Unknown();		
		float tau = 1.0f;
		float phi = 0.0f;
		for (IDegree deg : args) {
			IntervalDegree ival = (IntervalDegree) deg;
			if (ival == null) 
				continue;
			tau = Math.min(tau,ival.getTau());
			phi = Math.max(phi,ival.getPhi());
		}
		return new IntervalDegree(tau,1.0f-phi);
	}

	public String getName() {
		return "i_forall";
	}

}
