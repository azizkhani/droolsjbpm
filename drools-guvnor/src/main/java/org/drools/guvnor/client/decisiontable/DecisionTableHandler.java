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

package org.drools.guvnor.client.decisiontable;

import java.util.ArrayList;
import java.util.List;

import org.drools.ide.common.client.modeldriven.dt.ActionCol;
import org.drools.ide.common.client.modeldriven.dt.AttributeCol;
import org.drools.ide.common.client.modeldriven.dt.ConditionCol;
import org.drools.ide.common.client.modeldriven.dt.GuidedDecisionTable;
import org.drools.ide.common.client.modeldriven.dt.MetadataCol;

/**
 * Helps to move rows and columns in Decision Table.
 *
 * @author Toni Rikkola
 *
 */
public class DecisionTableHandler {

    // counter and description data
    private static int COUNTER_AND_DESCRIPTION = 2;

    private static int ARROW                   = 1;

    enum DTColumnConfigType {
        METADATA, ATTRIBUTE, CONDITION, ACTION
    }

    /**
     * Checks if old and new column have the same type.
     *
     * @param dt
     * @param oldIndex
     * @param newIndex
     * @return
     */
    public static boolean validateMove(GuidedDecisionTable dt,
                                       int oldIndex,
                                       int newIndex) {

        DTColumnConfigType oldType = DecisionTableHandler.getType( dt,
                                                                   oldIndex );
        DTColumnConfigType newType = DecisionTableHandler.getType( dt,
                                                                   newIndex );

        return oldType.equals( newType );
    }

    /**
     * Moves the column if the old and new index columns have the same type.
     *
     * @param dt
     * @param oldIndex
     * @param newIndex
     */
    public static void moveColumn(GuidedDecisionTable dt,
                                  int oldIndex,
                                  int newIndex) {
        if ( !validateMove( dt,
                            oldIndex,
                            newIndex ) ) {
            return;
        }

        DTColumnConfigType type = getType( dt,
                                           newIndex );

        // Move headers.
        // its +2 as we have counter and description data
        int add = COUNTER_AND_DESCRIPTION;
        switch ( type ) {
            case METADATA :
                dt.setMetadataCols( moveColumnHeader( dt.getMetadataCols(),
                                                      oldIndex - add,
                                                      newIndex - add ) );
                break;
            case ATTRIBUTE :
                add = COUNTER_AND_DESCRIPTION + dt.getMetadataCols().size();
                dt.setAttributeCols( moveColumnHeader( dt.getAttributeCols(),
                                                     oldIndex - add,
                                                     newIndex - add ) );
                break;
            case CONDITION :
                add = COUNTER_AND_DESCRIPTION + dt.getMetadataCols().size() + dt.getAttributeCols().size();
                dt.setConditionCols( moveColumnHeader( dt.getConditionCols(),
                                                     oldIndex - add,
                                                     newIndex - add ) );
                break;
            case ACTION :
                // +1 as we have an arrow between conditions and actions.
                add = COUNTER_AND_DESCRIPTION + dt.getMetadataCols().size() + dt.getAttributeCols().size() + ARROW + dt.getConditionCols().size();
                dt.setActionCols( moveColumnHeader( dt.getActionCols(),
                                                  oldIndex - add,
                                                  newIndex - add ) );
                break;
            default :
                break;
        }

        // Move data.
        switch ( type ) {
            case ACTION :
                // If we are moving action parts we need to jump over the arrow separator. 
                moveData( dt.getData(),
                          oldIndex - 1,
                          newIndex - 1 );
                break;
            default :
                moveData( dt.getData(),
                          oldIndex,
                          newIndex );
                break;

        }
    }

    public static DTColumnConfigType getType(GuidedDecisionTable dt,
                                             int index) {

        if ( index < (COUNTER_AND_DESCRIPTION + dt.getMetadataCols().size()) ) {
            return DTColumnConfigType.METADATA;
        } else if ( index < (COUNTER_AND_DESCRIPTION + dt.getMetadataCols().size() + dt.getAttributeCols().size()) ) {
            return DTColumnConfigType.ATTRIBUTE;
        } else if ( index < (COUNTER_AND_DESCRIPTION + dt.getMetadataCols().size() + dt.getAttributeCols().size() + dt.getConditionCols().size() + ARROW) ) {
            return DTColumnConfigType.CONDITION;
        } else {
            return DTColumnConfigType.ACTION;
        }
    }

    public static void moveData(String[][] data,
                                int oldIndex,
                                int newIndex) {
        if ( newIndex > oldIndex ) {
            moveDataRight( data,
                           oldIndex,
                           newIndex );
        } else {
            moveDataLeft( data,
                          oldIndex,
                          newIndex );
        }
    }

    public static void moveDataRight(String[][] data,
                                     int oldIndex,
                                     int newIndex) {

        for ( int i = 0; i < data.length; i++ ) {
            String[] row = data[i];

            // Get the value that needs to be moved.
            String value = row[oldIndex];

            for ( int j = oldIndex; j <= newIndex; j++ ) {
                if ( j < newIndex && j >= oldIndex ) {
                    // Move everything between to the left.
                    row[j] = row[j + 1];
                } else if ( j == newIndex ) {
                    row[j] = value;
                }
            }
        }
    }

    public static void moveDataLeft(String[][] data,
                                    int oldIndex,
                                    int newIndex) {

        for ( int i = 0; i < data.length; i++ ) {
            String[] row = data[i];

            // Get the value that needs to be moved.
            String value = row[oldIndex];

            for ( int j = oldIndex; j >= newIndex; j-- ) {
                if ( j > newIndex && j <= oldIndex ) {
                    // Move everything between to the left.
                    row[j] = row[j - 1];
                } else if ( j == newIndex ) {
                    row[j] = value;
                }
            }
        }
    }

    public static <T> List <T>moveColumnHeader(List<T> list,
                                    int oldIndex,
                                    int newIndex) {
        T from = list.get( oldIndex );
        T to = list.get( newIndex );

        // Create new list.
        List<T> newList = new ArrayList<T>();
        for ( T item : list ) {
            if ( item.equals( to ) ) {
                if ( newIndex > oldIndex ) {
                    newList.add( item );
                    newList.add( from );
                } else {
                    newList.add( from );
                    newList.add( item );
                }
            } else if ( !item.equals( from ) ) {
                newList.add( item );
            }
        }

        return newList;
    }

}
