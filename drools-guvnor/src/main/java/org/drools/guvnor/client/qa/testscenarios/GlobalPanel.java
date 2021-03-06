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

package org.drools.guvnor.client.qa.testscenarios;

import java.util.Map;

import org.drools.ide.common.client.modeldriven.testing.ExecutionTrace;
import org.drools.ide.common.client.modeldriven.testing.FixtureList;
import org.drools.ide.common.client.modeldriven.testing.Scenario;

import com.google.gwt.user.client.ui.VerticalPanel;

public class GlobalPanel extends VerticalPanel {
    public GlobalPanel(Map<String, FixtureList> globals,
                       Scenario scenario,
                       ExecutionTrace previousEx,
                       ScenarioWidget scenarioWidget) {
        for ( Map.Entry<String, FixtureList> e : globals.entrySet() ) {
            add( new GlobalFactWidget( e.getKey(),
                                       globals.get( e.getKey() ),
                                       scenario,
                                       scenarioWidget,
                                       previousEx ) );
        }
    }
}
