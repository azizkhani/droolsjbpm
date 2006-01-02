package org.drools.reteoo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.drools.FactException;
import org.drools.rule.LiteralConstraint;
import org.drools.spi.PropagationContext;

public class AlphaNode extends ObjectSource
    implements
    ObjectSink,
    NodeMemory {

    private final LiteralConstraint constraint;

    private final ObjectSource      objectSource;

    AlphaNode(int id,
              LiteralConstraint constraint,
              boolean hasMemory,
              ObjectSource objectSource) {
        super( id );
        this.constraint = constraint;
        this.objectSource = objectSource;
        setHasMemory( hasMemory );
    }

    public LiteralConstraint getConstraint() {
        return this.constraint;
    }

    public void attach() {
        this.objectSource.addObjectSink( this );
    }

    public void assertObject(Object object,
                             FactHandleImpl handle,
                             PropagationContext context,
                             WorkingMemoryImpl workingMemory) throws FactException {
        if ( hasMemory() ) {
            Set memory = (Set) workingMemory.getNodeMemory( this );
            if ( !memory.contains( handle ) ) {
                if ( this.constraint.isAllowed( object ) ) {
                    memory.add( handle );
                    propagateAssertObject( object,
                                           handle,
                                           context,
                                           workingMemory );
                }
            }
        } else {
            if ( this.constraint.isAllowed( object ) ) {
                propagateAssertObject( object,
                                       handle,
                                       context,
                                       workingMemory );
            }
        }
    }

    public void retractObject(FactHandleImpl handle,
                              PropagationContext context,
                              WorkingMemoryImpl workingMemory) throws FactException {
        if ( hasMemory() ) {
            Set memory = (Set) workingMemory.getNodeMemory( this );
            if ( memory.remove( handle ) ) {
                memory.remove( handle );
                propagateRetractObject( handle,
                                        context,
                                        workingMemory );
            }
        } else {
            propagateRetractObject( handle,
                                    context,
                                    workingMemory );
        }
    }

    public void updateNewNode(WorkingMemoryImpl workingMemory,
                              PropagationContext context) throws FactException {
        this.attachingNewNode = true;

        if ( hasMemory() ) {
            Set memory = (Set) workingMemory.getNodeMemory( this );

            for ( Iterator it = memory.iterator(); it.hasNext(); ) {
                FactHandleImpl handle = (FactHandleImpl) it.next();
                Object object = workingMemory.getObject( handle );
                propagateAssertObject( object,
                                       handle,
                                       context,
                                       workingMemory );
            }
        } else {
            // We need to detach and re-attach to make sure the node is at the
            // top
            // for the propagation
            this.objectSource.removeObjectSink( this );
            this.objectSource.addObjectSink( this );
            this.objectSource.updateNewNode( workingMemory,
                                             context );
        }

        this.attachingNewNode = false;
    }

    public Object createMemory() {
        return new HashSet();
    }

    public boolean equals(Object object) {
        if ( this == object ) {
            return true;
        }

        if ( object == null || getClass() != object.getClass() ) {
            return false;
        }

        AlphaNode other = (AlphaNode) object;

        return this.objectSource.equals( other.objectSource ) && this.constraint.equals( other.constraint );
    }

    public void remove() {
        // TODO Auto-generated method stub

    }
}
