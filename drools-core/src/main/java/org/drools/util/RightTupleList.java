/**
 * 
 */
package org.drools.util;

import java.util.Collection;
import java.util.LinkedList;

import org.drools.common.InternalFactHandle;
import org.drools.reteoo.Evaluation;
import org.drools.reteoo.ImperfectRightTuple;
import org.drools.reteoo.LeftTuple;
import org.drools.reteoo.RightTuple;
import org.drools.reteoo.RightTupleMemory;
import org.drools.util.AbstractHashTable.Index;

public class RightTupleList
    implements
    RightTupleMemory,
    Entry {
    private static final long      serialVersionUID = 400L;

    public Entry                   previous;
    public Entry                   next;

    public RightTuple              first;
    public RightTuple              last;

    private int                    hashCode;
    private Index                  index;

    private TupleHashTableIterator iterator;

    public RightTupleList() {
        // this is not an index bucket
        this.hashCode = 0;
        this.index = null;
    }

    public RightTupleList(final Index index,
                          final int hashCode) {
        this.index = index;
        this.hashCode = hashCode;
    }

    public RightTuple getFirst(LeftTuple leftTuple) {
        return this.first;
    }

    public RightTuple getLast(LeftTuple leftTuple) {
        return this.last;
    }

    public void add(final RightTuple rightTuple) {
        if ( this.first != null ) {
            this.first.setPrevious( rightTuple );
            rightTuple.setNext( this.first );
            this.first = rightTuple;
        } else {
            this.first = rightTuple;
            this.last = rightTuple;;
        }
    }

    /**
     * We assume that this rightTuple is contained in this hash table
     */
    public void remove(final RightTuple rightTuple) {
        RightTuple previous = (RightTuple) rightTuple.getPrevious();
        RightTuple next = (RightTuple) rightTuple.getNext();

        if ( previous != null && next != null ) {
            // remove from middle
            previous.setNext( next );
            next.setPrevious( previous );
        } else if ( next != null ) {
            // remove from first
            this.first = next;
            next.setPrevious( null );
        } else if ( previous != null ) {
            // remove from end
            this.last = previous;
            previous.setNext( null );
        } else {
            // remove everything
            this.last = null;
            this.first = null;
        }
    }

    public RightTuple get(final InternalFactHandle handle) {
        RightTuple current = this.first;
        while ( current != null ) {
            if ( handle == current.getFactHandle() ) {
                return current;
            }
            current = (RightTuple) current.getNext();
        }
        return null;
    }

    public boolean contains(final InternalFactHandle handle) {
        return get( handle ) != null;
    }

    public RightTuple get(final RightTuple rightTuple) {
        InternalFactHandle handle = rightTuple.getFactHandle();
        RightTuple current = this.first;
        while ( current != null ) {
            if ( handle == current.getFactHandle() ) {
                return current;
            }
            current = (RightTuple) current.getNext();
        }
        return null;
    }

    public boolean contains(final RightTuple rightTuple) {
        return get( rightTuple ) != null;
    }

    public int size() {
        int i = 0;
        RightTuple current = this.first;
        while ( current != null ) {
            current = (RightTuple) current.getNext();
            i++;
        }
        return i;
    }

    public Iterator iterator() {
        if ( this.iterator == null ) {
            this.iterator = new TupleHashTableIterator();
        }
        this.iterator.reset( this.first );
        return this.iterator;
    }

    public static class TupleHashTableIterator
        implements
        Iterator {
        private RightTuple current;

        public void reset(RightTuple first) {
            this.current = first;
        }

        public Object next() {
            if ( this.current != null ) {
                RightTuple returnValue = this.current;
                this.current = (RightTuple) current.getNext();
                return returnValue;
            } else {
                return null;
            }
        }
        
        public boolean hasNext() {
        	return this.current != null;
        }

        public void remove() {
            // do nothing
        }
    }

    public boolean matches(final Object object,
                           final int objectHashCode) {
        return this.hashCode == objectHashCode && this.index.equal( this.first.getFactHandle().getObject(),
                                                                    object );
    }

    public boolean matches(final LeftTuple tuple,
                           final int tupleHashCode) {
        return this.hashCode == tupleHashCode && this.index.equal( this.first.getFactHandle().getObject(),
                                                                   tuple );
    }

    public int hashCode() {
        return this.hashCode;
    }

    public boolean equals(final Object object) {
        final RightTupleList other = (RightTupleList) object;
        return this.hashCode == other.hashCode && this.index == other.index;
    }

    public Entry getPrevious() {
        return null;
        //          return this.previous;            
    }

    public void setPrevious(Entry previous) {
        //          this.previous = previous;
    }

    public Entry getNext() {
        return this.next;
    }

    public void setNext(final Entry next) {
        this.next = next;
    }

    public boolean isIndexed() {
        return (this.index != null);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for ( RightTuple rightTuple = (RightTuple) this.first; rightTuple != null; rightTuple = (RightTuple) rightTuple.getNext() ) {
            builder.append( rightTuple );
        }

        return builder.toString();
    }

	public Collection<Evaluation> getArgs() {
		LinkedList<Evaluation> ans = new LinkedList<Evaluation>();
		Iterator iter = this.iterator();
		while (iter.hasNext()) {
			RightTuple rt = (RightTuple) iter.next();
			if (rt instanceof ImperfectRightTuple)
				ans.add(((ImperfectRightTuple) rt).getRecord());
		}
		return ans;
	}
}