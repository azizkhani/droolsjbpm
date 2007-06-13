package org.drools.rule.builder.dialect.java;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KnowledgeHelperFixer {

    static String  KNOWLEDGE_HELPER_PFX = "";                               //could also be: "drools\\." for "classic" mode.
    static Pattern UPDATE               = Pattern.compile( "(.*)\\b" + KnowledgeHelperFixer.KNOWLEDGE_HELPER_PFX + "update\\s*\\(([^)]+)\\)(\\s*;.*)",
                                                           Pattern.DOTALL );
    static Pattern INSERT               = Pattern.compile( "(.*)\\b" + KnowledgeHelperFixer.KNOWLEDGE_HELPER_PFX + "insert\\s*\\((.*)\\)(\\s*;.*)",
                                                           Pattern.DOTALL );
    static Pattern INSERT_LOGICAL       = Pattern.compile( "(.*)\\b" + KnowledgeHelperFixer.KNOWLEDGE_HELPER_PFX + "insertLogical\\s*\\((.*)\\)(\\s*;.*)",
                                                           Pattern.DOTALL );
    static Pattern RETRACT              = Pattern.compile( "(.*)\\b" + KnowledgeHelperFixer.KNOWLEDGE_HELPER_PFX + "retract\\s*\\((.+)\\)(\\s*;.*)",
                                                           Pattern.DOTALL );

    /**
     * This takes a raw consequence, and fixes up the KnowledegeHelper references 
     * to be what SMF requires.
     *
     * eg: modify( myObject ); --> drools.modify( myObjectHandle, myObject );
     * refer to the Replacer implementation classes below for the specific replacement patterns.
     * 
     * (can adjust the PREFIX if needed).
     * 
     * Uses some non-tail recursion to ensure that all parts are "expanded". 
     */
    public String fix(final String raw) {
        String result = fix( raw,
                             UpdateReplacer.INSTANCE );
        result = fix( result,
                      InsertReplacer.INSTANCE );
        result = fix( result,
                      InsertLogicalReplacer.INSTANCE );
        result = fix( result,
                      RetractReplacer.INSTANCE );
        return result;
    }

    /**
     * Recursively apply the pattern, replace the guts of what is matched.
     */
    public String fix(final String raw,
                      final Replacer replacer) {
        if ( raw == null ) {
            return null;
        }
        final Matcher matcher = replacer.getPattern().matcher( raw );

        if ( matcher.matches() ) {
            String pre = matcher.group( 1 );
            if ( matcher.group( 1 ) != null ) {
                pre = fix( pre,
                           replacer );
            }
            final String obj = matcher.group( 2 ).trim();
            String post = matcher.group( 3 );
            if ( post != null ) {
                post = fix( post,
                            replacer );
            }

            final String replacement = escapeDollarSigns( replacer,
                                                          obj );
            return pre + matcher.replaceAll( replacement ) + post;

        } else {
            return raw;
        }
    }

    /** 
     * This is needed to escape "$" so that matches doesn't try and pull out groups that don't exist.
     * "$" may just be used in variable name etc... 
     */
    private String escapeDollarSigns(final Replacer replacer,
                                     final String obj) {
        return KnowledgeHelperFixer.replace( replacer.getReplacement( obj ),
                                             "$",
                                             "\\$",
                                             256 );
    }

    static interface Replacer {
        Pattern getPattern();

        String getReplacement(String guts);
    }

    static class InsertReplacer
        implements
        Replacer {

        static Replacer INSTANCE = new InsertReplacer();

        public Pattern getPattern() {
            return KnowledgeHelperFixer.INSERT;
        }

        public String getReplacement(final String guts) {
            return "drools.insert(" + guts + ")";
        }

    }

    static class InsertLogicalReplacer
        implements
        Replacer {

        static Replacer INSTANCE = new InsertLogicalReplacer();

        public Pattern getPattern() {
            return KnowledgeHelperFixer.INSERT_LOGICAL;
        }

        public String getReplacement(final String guts) {
            return "drools.insertLogical(" + guts + ")";
        }

    }

    static class UpdateReplacer
        implements
        Replacer {

        static Replacer INSTANCE = new UpdateReplacer();

        public Pattern getPattern() {
            return KnowledgeHelperFixer.UPDATE;
        }

        public String getReplacement(final String guts) {
            return "drools.update( " + guts + " )";
        }

    }

    static class RetractReplacer
        implements
        Replacer {

        static Replacer INSTANCE = new RetractReplacer();

        public Pattern getPattern() {
            return KnowledgeHelperFixer.RETRACT;
        }

        public String getReplacement(final String guts) {
            return "drools.retract( " + guts.trim() + " )";
        }

    }

    /**
     * Simple non regex replacer. 
     * jakarta commons provided the inspiration for this.
     */
    static String replace(final String text,
                          final String repl,
                          final String with,
                          int max) {
        if ( text == null || repl == null || repl.equals( "" ) || with == null || max == 0 ) {
            return text;
        }

        final StringBuffer buf = new StringBuffer( text.length() );
        int start = 0, end = 0;
        while ( (end = text.indexOf( repl,
                                     start )) != -1 ) {
            buf.append( text.substring( start,
                                        end ) ).append( with );
            start = end + repl.length();

            if ( --max == 0 ) {
                break;
            }
        }
        buf.append( text.substring( start ) );
        return buf.toString();
    }

}