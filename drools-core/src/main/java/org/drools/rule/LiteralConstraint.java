package org.drools.rule;

/*
 * Copyright 2005 JBoss Inc
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.drools.common.InternalFactHandle;
import org.drools.common.InternalWorkingMemory;
import org.drools.degrees.IDegree;
import org.drools.degrees.factory.IDegreeFactory;
import org.drools.reteoo.ArgList;
import org.drools.reteoo.ConstraintKey;
import org.drools.reteoo.Evaluation;
import org.drools.reteoo.EvaluationTemplate;
import org.drools.reteoo.SingleEvaluationTemplate;
import org.drools.spi.AcceptsReadAccessor;
import org.drools.spi.AlphaNodeFieldConstraint;
import org.drools.spi.Constraint;
import org.drools.spi.Evaluator;
import org.drools.spi.FieldValue;
import org.drools.spi.InternalReadAccessor;

public class LiteralConstraint
    implements
    AlphaNodeFieldConstraint,
    AcceptsReadAccessor,
    Externalizable {

    private static final long    serialVersionUID = 400L;

    private InternalReadAccessor readAccesor;
    private LiteralRestriction   restriction;
    

	
    private EvaluationTemplate template;
   
    private String label;
    
    
    public LiteralConstraint() {
        this( null,
              null );
    }

    public LiteralConstraint(final InternalReadAccessor extractor,
                             final Evaluator evaluator,
                             final FieldValue field) {
        this.readAccesor = extractor;
        this.restriction = new LiteralRestriction( field,
                                                   evaluator,
                                                   extractor );
    }

    public LiteralConstraint(final InternalReadAccessor extractor,
                             final LiteralRestriction restriction) {
        this.readAccesor = extractor;
        this.restriction = restriction;
    }

    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        readAccesor = (InternalReadAccessor) in.readObject();
        restriction = (LiteralRestriction) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject( readAccesor );
        out.writeObject( restriction );
    }

    public void setReadAccessor(InternalReadAccessor readAccessor) {
        this.readAccesor = readAccessor;
    }

    public Evaluator getEvaluator() {
        return this.restriction.getEvaluator();
    }

    public FieldValue getField() {
        return this.restriction.getField();
    }

    public InternalReadAccessor getFieldExtractor() {
        return this.readAccesor;
    }

    /**
     * Literal constraints cannot have required declarations, so always return an empty array.
     * @return
     *      Return an empty <code>Declaration[]</code>
     */
    public Declaration[] getRequiredDeclarations() {
        return this.restriction.getRequiredDeclarations();
    }

    public void replaceDeclaration(Declaration oldDecl,
                                   Declaration newDecl) {
        this.restriction.replaceDeclaration( oldDecl,
                                             newDecl );
    }

    public boolean isAllowed(final InternalFactHandle handle,
                             final InternalWorkingMemory workingMemory,
                             final ContextEntry ctx) {
        return this.restriction.isAllowed( this.readAccesor,
                                           handle,
                                           workingMemory,
                                           ctx );
    }
    
    public Evaluation isSatisfied(InternalFactHandle handle,
			InternalWorkingMemory workingMemory, ContextEntry context,
			IDegreeFactory factory) {
    	IDegree deg =  this.restriction.isSatisfied( this.readAccesor,
    												handle,
    												workingMemory,
    												context,
    												factory);
    	return template.spawn(deg,new ArgList(handle.getObject()));
	}

    public String toString() {
        return "[LiteralConstraint fieldExtractor=" + this.readAccesor + " evaluator=" + getEvaluator() + " value=" + getField() + "]";
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + this.readAccesor.hashCode();
        result = PRIME * result + this.restriction.hashCode();
        return result;
    }

    public boolean equals(final Object object) {
        if ( this == object ) {
            return true;
        }
        if ( object == null || object.getClass() != LiteralConstraint.class ) {
            return false;
        }
        final LiteralConstraint other = (LiteralConstraint) object;

        return this.readAccesor.equals( other.readAccesor ) && this.restriction.equals( other.restriction ) && (this.isCutter() == other.isCutter());
    }

    public Object clone() {
        return new LiteralConstraint( this.readAccesor,
                                      this.getEvaluator(),
                                      this.getField() );
    }

    public ContextEntry createContextEntry() {
        // no need for context info
        return null;
    }

    public ConstraintType getType() {
        return Constraint.ConstraintType.ALPHA;
    }

    public boolean isTemporal() {
        return this.restriction.isTemporal();
    }

	public ConstraintKey getConstraintKey() {
		return restriction.getConstraintKey();
	}
	
	public Collection<ConstraintKey> getAllConstraintKeys() {
		Collection<ConstraintKey> ans = new LinkedList<ConstraintKey>();
									
		ans.add(this.getConstraintKey());
		return ans;
	}

	
	
	
	public EvaluationTemplate buildEvaluationTemplate(int id, Map<ConstraintKey, Set<String>> dependencies, IDegreeFactory factory) {
		 Set<String> deps;
		 
		 String label = this.restriction.getLabel();		 
		 Set<String> aliasedDeps = null;
		 	if (dependencies != null && label != null) {
		 		ConstraintKey tester = new ConstraintKey();
		 			tester.setAlias(label);
		 		aliasedDeps = dependencies.remove(tester);
		 		
		 		if (aliasedDeps != null) {
		 			ConstraintKey properKey = this.getConstraintKey();		 						 				
		 			Set<String> previousDeps = dependencies.remove(properKey);
		 			
		 			if (previousDeps != null)
		 				aliasedDeps.addAll(previousDeps);
		 			
		 			properKey.setAlias(label);
		 			
		 			dependencies.put(properKey, aliasedDeps);		 		
		 		}
		 	}
		 	
		 
		 if (dependencies == null)
			 deps = Collections.emptySet();
		 else 
			 deps = dependencies.get(this.getConstraintKey());
		 
		
		 
	   	this.template = new SingleEvaluationTemplate(id,this.getConstraintKey(),deps,factory.getMergeStrategy(),factory.getNullHandlingStrategy(),factory);
	   	return template;
	}

	public EvaluationTemplate getEvalTemplate(ConstraintKey key) {
		if (this.template.getConstraintKey().equals(key))
			return this.template;
		else return null;
	}
	
	
	public boolean isCutter() {
		return restriction.isCutter();
	}
	
	public void setCutter(boolean cut) {
		restriction.setCutter(cut);
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	
}