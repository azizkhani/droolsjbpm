/*
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

package org.drools.guvnor.client.qa;

import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.LoadingPopup;
import org.drools.guvnor.client.common.PrettyFormLayout;
import org.drools.guvnor.client.rpc.AnalysisReport;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.rpc.VerificationService;
import org.drools.guvnor.client.rpc.VerificationServiceAsync;
import org.drools.guvnor.client.rulelist.EditItemEvent;
import org.drools.guvnor.client.util.Format;
import org.drools.guvnor.client.messages.Constants;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.GWT;

/**
 * Viewer for, well, analysis !
 *
 * @author Michael Neale
 */
public class AnalysisView extends Composite {

    private VerticalPanel layout;
    private String        packageUUID;
    private Constants     constants = GWT.create( Constants.class );
    private EditItemEvent edit;

    public AnalysisView(String packageUUID,
                        String packageName,
                        EditItemEvent edit) {
        this.layout = new VerticalPanel();
        this.packageUUID = packageUUID;
        this.edit = edit;

        PrettyFormLayout pf = new PrettyFormLayout();

        VerticalPanel vert = new VerticalPanel();
        String m = Format.format( constants.AnalysingPackage(),
                                  new String[]{packageName} );
        vert.add( new HTML( m ) );
        Button run = new Button( constants.RunAnalysis() );
        run.addClickListener( new ClickListener() {
            public void onClick(Widget w) {
                runAnalysis();
            }
        } );
        vert.add( run );

        pf.addHeader( "images/analyse_large.png",
                      vert );
        layout.add( pf );

        layout.add( new Label() );

        layout.setWidth( "100%" );

        initWidget( layout );
    }

    private void runAnalysis() {
        LoadingPopup.showMessage( constants.AnalysingPackageRunning() );
        VerificationServiceAsync verificationService = GWT.create( VerificationService.class );

        verificationService.analysePackage( packageUUID,
                                            new GenericCallback() {
                                                public void onSuccess(Object data) {
                                                    AnalysisReport rep = (AnalysisReport) data;
                                                    VerifierResultWidget w = new VerifierResultWidget( rep,
                                                                                                       true,
                                                                                                       edit );
                                                    w.setWidth( "100%" );
                                                    layout.remove( 1 );
                                                    layout.add( w );
                                                    LoadingPopup.close();
                                                }
                                            } );

    }

}
