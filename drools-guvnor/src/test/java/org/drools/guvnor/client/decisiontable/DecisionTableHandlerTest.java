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

import java.util.List;

import org.drools.ide.common.client.modeldriven.dt.ActionCol;
import org.drools.ide.common.client.modeldriven.dt.AttributeCol;
import org.drools.ide.common.client.modeldriven.dt.ConditionCol;
import org.drools.ide.common.client.modeldriven.dt.GuidedDecisionTable;
import org.junit.Test;

import static org.junit.Assert.*;

public class DecisionTableHandlerTest {

    @Test
    public void testMoveAttribute() {

        GuidedDecisionTable dt = new GuidedDecisionTable();

        dt.getAttributeCols().add( TestData.newAttributeCol( "date-effective" ) );
        dt.getAttributeCols().add( TestData.newAttributeCol( "date-expires" ) );
        dt.getConditionCols().add( TestData.newConditionCol( "amount max" ) );
        dt.getConditionCols().add( TestData.newConditionCol( "amount min" ) );
        dt.getConditionCols().add( TestData.newConditionCol( "period" ) );
        dt.getActionCols().add( TestData.newActionCol( "LMI1" ) );
        dt.getActionCols().add( TestData.newActionCol( "LMI2" ) );
        dt.getActionCols().add( TestData.newActionCol( "LMI3" ) );
        dt.setData( TestData.newData( dt.getAttributeCols().size(),
                                    dt.getConditionCols().size(),
                                    dt.getActionCols().size() ) );

        Order.assertAttributeOrder( dt.getAttributeCols(),
                                    new String[]{"date-effective", "date-expires"} );
        Order.assertConditionOrder( dt.getConditionCols(),
                                    new String[]{"amount max", "amount min", "period"} );
        Order.assertActionOrder( dt.getActionCols(),
                                 new String[]{"LMI1", "LMI2", "LMI3"} );
        Order.assertDataOrder( dt.getData(),
                               new String[][]{
                                              new String[]{"0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9" }, 
                                              new String[]{"1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9" }, 
                                              new String[]{"2.0", "2.1", "2.2", "2.3", "2.4", "2.5", "2.6", "2.7", "2.8", "2.9" }});

        // Move right
        int oldIndex = 2;
        int newIndex = 3;

        DecisionTableHandler.moveColumn( dt,
                                         oldIndex,
                                         newIndex );

        Order.assertAttributeOrder( dt.getAttributeCols(),
                                    new String[]{"date-expires", "date-effective"} );
        Order.assertConditionOrder( dt.getConditionCols(),
                                    new String[]{"amount max", "amount min", "period"} );
        Order.assertActionOrder( dt.getActionCols(),
                                 new String[]{"LMI1", "LMI2", "LMI3"} );
        
        Order.assertDataOrder( dt.getData(),
                               new String[][]{
                                              new String[]{"0.0", "0.1", "0.3", "0.2", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9" }, 
                                              new String[]{"1.0", "1.1", "1.3", "1.2", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9" }, 
                                              new String[]{"2.0", "2.1", "2.3", "2.2", "2.4", "2.5", "2.6", "2.7", "2.8", "2.9" }});

        // Move left
        oldIndex = 3;
        newIndex = 2;

        DecisionTableHandler.moveColumn( dt,
                                         oldIndex,
                                         newIndex );

        Order.assertAttributeOrder( dt.getAttributeCols(),
                                    new String[]{"date-effective", "date-expires"} );
        Order.assertConditionOrder( dt.getConditionCols(),
                                    new String[]{"amount max", "amount min", "period"} );
        Order.assertDataOrder( dt.getData(),
                               new String[][]{
                                              new String[]{"0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9" }, 
                                              new String[]{"1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9" }, 
                                              new String[]{"2.0", "2.1", "2.2", "2.3", "2.4", "2.5", "2.6", "2.7", "2.8", "2.9" }});

    }

    @Test
    public void testMoveCondition() {
        
        GuidedDecisionTable dt = new GuidedDecisionTable();
        
        dt.getAttributeCols().add( TestData.newAttributeCol( "date-effective" ) );
        dt.getAttributeCols().add( TestData.newAttributeCol( "date-expires" ) );
        dt.getConditionCols().add( TestData.newConditionCol( "amount max" ) );
        dt.getConditionCols().add( TestData.newConditionCol( "amount min" ) );
        dt.getConditionCols().add( TestData.newConditionCol( "period" ) );
        dt.getActionCols().add( TestData.newActionCol( "LMI1" ) );
        dt.getActionCols().add( TestData.newActionCol( "LMI2" ) );
        dt.getActionCols().add( TestData.newActionCol( "LMI3" ) );
        dt.setData( TestData.newData( dt.getAttributeCols().size(),
                                    dt.getConditionCols().size(),
                                    dt.getActionCols().size() ) );
        
        Order.assertAttributeOrder( dt.getAttributeCols(),
                                    new String[]{"date-effective", "date-expires"} );
        Order.assertConditionOrder( dt.getConditionCols(),
                                    new String[]{"amount max", "amount min", "period"} );
        Order.assertActionOrder( dt.getActionCols(),
                                 new String[]{"LMI1", "LMI2", "LMI3"} );
        Order.assertDataOrder( dt.getData(),
                               new String[][]{
                                              new String[]{"0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9" }, 
                                              new String[]{"1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9" }, 
                                              new String[]{"2.0", "2.1", "2.2", "2.3", "2.4", "2.5", "2.6", "2.7", "2.8", "2.9" }});
        
        // Move right
        int oldIndex = 4;
        int newIndex = 6;
        
        DecisionTableHandler.moveColumn( dt,
                                         oldIndex,
                                         newIndex );
        
        Order.assertAttributeOrder( dt.getAttributeCols(),
                                    new String[]{"date-effective", "date-expires"} );
        Order.assertConditionOrder( dt.getConditionCols(),
                                    new String[]{"amount min", "period", "amount max"} );
        Order.assertActionOrder( dt.getActionCols(),
                                 new String[]{"LMI1", "LMI2", "LMI3"} );
        Order.assertDataOrder( dt.getData(),
                               new String[][]{
                                              new String[]{"0.0", "0.1", "0.2", "0.3", "0.5", "0.6", "0.4", "0.7", "0.8", "0.9" }, 
                                              new String[]{"1.0", "1.1", "1.2", "1.3", "1.5", "1.6", "1.4", "1.7", "1.8", "1.9" }, 
                                              new String[]{"2.0", "2.1", "2.2", "2.3", "2.5", "2.6", "2.4", "2.7", "2.8", "2.9" }});
        
        // Move left
        oldIndex = 6;
        newIndex = 5;
        
        DecisionTableHandler.moveColumn( dt,
                                         oldIndex,
                                         newIndex );
        
        Order.assertAttributeOrder( dt.getAttributeCols(),
                                    new String[]{"date-effective", "date-expires"} );
        Order.assertConditionOrder( dt.getConditionCols(),
                                    new String[]{"amount min", "amount max", "period"} );
        Order.assertActionOrder( dt.getActionCols(),
                                 new String[]{"LMI1", "LMI2", "LMI3"} );

        Order.assertDataOrder( dt.getData(),
                               new String[][]{
                                              new String[]{"0.0", "0.1", "0.2", "0.3", "0.5", "0.4", "0.6", "0.7", "0.8", "0.9" }, 
                                              new String[]{"1.0", "1.1", "1.2", "1.3", "1.5", "1.4", "1.6", "1.7", "1.8", "1.9" }, 
                                              new String[]{"2.0", "2.1", "2.2", "2.3", "2.5", "2.4", "2.6", "2.7", "2.8", "2.9" }});
        
    }

    @Test
    public void testMoveAction() {
        
        GuidedDecisionTable dt = new GuidedDecisionTable();
        
        dt.getAttributeCols().add( TestData.newAttributeCol( "date-effective" ) );
        dt.getAttributeCols().add( TestData.newAttributeCol( "date-expires" ) );
        dt.getConditionCols().add( TestData.newConditionCol( "amount max" ) );
        dt.getConditionCols().add( TestData.newConditionCol( "amount min" ) );
        dt.getConditionCols().add( TestData.newConditionCol( "period" ) );
        dt.getActionCols().add( TestData.newActionCol( "LMI1" ) );
        dt.getActionCols().add( TestData.newActionCol( "LMI2" ) );
        dt.getActionCols().add( TestData.newActionCol( "LMI3" ) );
        dt.setData( TestData.newData( dt.getAttributeCols().size(),
                                    dt.getConditionCols().size(),
                                    dt.getActionCols().size() ) );
        
        Order.assertAttributeOrder( dt.getAttributeCols(),
                                    new String[]{"date-effective", "date-expires"} );
        Order.assertConditionOrder( dt.getConditionCols(),
                                    new String[]{"amount max", "amount min", "period"} );
        Order.assertActionOrder( dt.getActionCols(),
                                 new String[]{"LMI1", "LMI2", "LMI3"} );
        Order.assertDataOrder( dt.getData(),
                               new String[][]{
                                              new String[]{"0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9" }, 
                                              new String[]{"1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9" }, 
                                              new String[]{"2.0", "2.1", "2.2", "2.3", "2.4", "2.5", "2.6", "2.7", "2.8", "2.9" }});

        int oldIndex = 9;
        int newIndex = 10;
        DecisionTableHandler.moveColumn( dt,
                                         oldIndex,
                                         newIndex );
        
        Order.assertAttributeOrder( dt.getAttributeCols(),
                                    new String[]{"date-effective", "date-expires"} );
        Order.assertConditionOrder( dt.getConditionCols(),
                                    new String[]{"amount max", "amount min", "period"} );
        Order.assertActionOrder( dt.getActionCols(),
                                 new String[]{"LMI1", "LMI3", "LMI2"} );
        Order.assertDataOrder( dt.getData(),
                               new String[][]{
                                              new String[]{"0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.9", "0.8" }, 
                                              new String[]{"1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7", "1.9", "1.8" }, 
                                              new String[]{"2.0", "2.1", "2.2", "2.3", "2.4", "2.5", "2.6", "2.7", "2.9", "2.8" }});
        
        oldIndex = 10;
        newIndex = 8;
        DecisionTableHandler.moveColumn( dt,
                                         oldIndex,
                                         newIndex );
        
        Order.assertAttributeOrder( dt.getAttributeCols(),
                                    new String[]{"date-effective", "date-expires"} );
        Order.assertConditionOrder( dt.getConditionCols(),
                                    new String[]{"amount max", "amount min", "period"} );
        Order.assertActionOrder( dt.getActionCols(),
                                 new String[]{"LMI2", "LMI1", "LMI3"} );
        Order.assertDataOrder( dt.getData(),
                               new String[][]{
                                              new String[]{"0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.8", "0.7", "0.9" }, 
                                              new String[]{"1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.8", "1.7", "1.9" }, 
                                              new String[]{"2.0", "2.1", "2.2", "2.3", "2.4", "2.5", "2.6", "2.8", "2.7", "2.9" }});
    }
}

class TestData {
    static String[][] newData(int attributes,
                              int conditions,
                              int actions) {
        String[][] data = new String[3][attributes + conditions + actions + 2];

        for ( int i = 0; i < data.length; i++ ) {
            String[] row = data[i];
            for ( int j = 0; j < data[i].length; j++ ) {
                row[j] = i + "." + j;
            }
        }

        return data;
    }

    static AttributeCol newAttributeCol(String attr) {
        AttributeCol a = new AttributeCol();

        a.attr = attr;

        return a;
    }

    static ConditionCol newConditionCol(String header) {
        ConditionCol c = new ConditionCol();

        c.setHeader( header );

        return c;
    }

    static ActionCol newActionCol(String header) {
        ActionCol a = new ActionCol();

        a.setHeader( header );

        return a;
    }
}

class Order {

    static void assertActionOrder(List<ActionCol> cols,
                                  String[] list) {

        assertEquals( "Needs to be of the same size.",
                             list.length,
                             cols.size() );

        for ( int i = 0; i < list.length; i++ ) {
            assertEquals( cols.get( i ).getHeader(),
                                 list[i] );
        }
    }

    public static void assertDataOrder(String[][] data1,
                                       String[][] data2) {

        assertEquals( "Needs to be of the same size.",
                             data1.length,
                             data2.length );

        for ( int i = 0; i < data1.length; i++ ) {
            String[] row1 = data1[i];
            String[] row2 = data2[i];

            assertEquals( "Needs to be of the same size.",
                                 row1.length,
                                 row2.length );

            for ( int j = 0; j < row1.length; j++ ) {
                assertEquals( row2[j],
                                     row1[j] );
            }
        }

    }

    static void assertAttributeOrder(List<AttributeCol> cols,
                                     String[] list) {

        assertEquals( "Needs to be of the same size.",
                             list.length,
                             cols.size() );

        for ( int i = 0; i < list.length; i++ ) {
            assertEquals( cols.get( i ).attr,
                                 list[i] );
        }
    }

    static void assertConditionOrder(List<ConditionCol> cols,
                                     String[] list) {

        assertEquals( "Needs to be of the same size.",
                             list.length,
                             cols.size() );

        for ( int i = 0; i < list.length; i++ ) {
            assertEquals( cols.get( i ).getHeader(),
                                 list[i] );
        }
    }

}
