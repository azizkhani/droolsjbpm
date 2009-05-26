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

package org.drools.lang.descr;

import java.util.Collections;
import java.util.List;

/**
 * An AST class to describe "collect" conditional element
 * 
 * @author etirelli
 *
 */
public class CollectDescr extends PatternSourceDescr
    implements
    ConditionalElementDescr,
    PatternDestinationDescr
    {

    private static final long  serialVersionUID = 400L;

    private PatternDescr       inputPattern;
    private String             classMethodName;

    public int getLine() {
        return this.inputPattern.getLine();
    }

    public String getClassMethodName() {
        return this.classMethodName;
    }

    public void setClassMethodName(final String classMethodName) {
        this.classMethodName = classMethodName;
    }

    public String toString() {
        return "[Collect: input=" + this.inputPattern.getIdentifier() + "; objectType=" + this.inputPattern.getObjectType() + "]";
    }

    public void addDescr(final BaseDescr patternDescr) {
        throw new UnsupportedOperationException( "Can't add descriptors to " + this.getClass().getName() );
    }
    
    public void insertBeforeLast(final Class clazz ,final BaseDescr baseDescr ) { 
        throw new UnsupportedOperationException( "Can't add descriptors to " + this.getClass().getName() );
    }    

    public List getDescrs() {
        // nothing to do
        return Collections.EMPTY_LIST;
    }

    public void addOrMerge(BaseDescr baseDescr) {
    	throw new UnsupportedOperationException( "Can't add descriptors to " + this.getClass().getName() );
    }
    
    public void addDescrFirst(final BaseDescr baseDescr) {
    	throw new UnsupportedOperationException( "Can't add descriptors to " + this.getClass().getName() );
    }
    
    

    public PatternDescr getInputPattern() {
        return this.inputPattern;
    }

    public void setInputPattern(final PatternDescr inputPattern) {
        this.inputPattern = inputPattern;
    }

}
