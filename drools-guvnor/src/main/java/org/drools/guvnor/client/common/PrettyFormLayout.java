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

package org.drools.guvnor.client.common;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Uses ext forms to do a prettier layout.
 *
 * @author Michael Neale
 */
public class PrettyFormLayout extends Composite {

    private VerticalPanel layout = new VerticalPanel();
    private FlexTable     currentTable;
    private String        sectionName;

    public PrettyFormLayout() {
        layout.setWidth( "100%" );
        initWidget( layout );
    }

    public void startSection() {
        this.currentTable = new FlexTable();
    }

    public void startSection(String title) {
        startSection();
        this.sectionName = title;
    }

    public void clear() {
        this.layout.clear();
    }

    public void addHeader(ImageResource img,
                          String name,
                          Image edit) {
        HorizontalPanel h = new HorizontalPanel();
        h.add( new Image( img ) );
        h.add( new HTML( "&nbsp;" ) );
        h.add( new Label( name ) );
        if ( edit != null ) h.add( edit );

        FormPanel f = newForm( null );

        f.add( h );
        layout.add( f );
    }

    public void addHeader(ImageResource img,
                          Widget content) {
        HorizontalPanel h = new HorizontalPanel();
        h.add( new Image( img ) );
        h.add( new HTML( "&nbsp;" ) );
        h.add( content );
        FormPanel f = newForm( null );
        f.add( h );
        layout.add( f );
    }

    private FormPanel newForm(final String hdr) {
        FormPanel fp = new FormPanel();
        fp.setWidth( "100%" );
        fp.addStyleName( "guvnor-FormPanel" );
        if ( hdr != null ) {
            fp.setTitle( hdr );
        }
        return fp;
    }

    public void endSection() {

        FormPanel f = newForm( this.sectionName );

        f.add( this.currentTable );

        this.layout.add( f );
        this.sectionName = null;
    }

    public void addRow(final Widget versionBrowser) {
        int i = currentTable.getRowCount();
        currentTable.setWidget( i,
                                0,
                                versionBrowser );
        currentTable.getFlexCellFormatter().setColSpan( i,
                                                        0,
                                                        2 );
    }

    public void addAttribute(String lbl,
                             final Widget categories) {
        int i = this.currentTable.getRowCount();
        currentTable.setWidget( i,
                                0,
                                new Label( lbl ) );
        currentTable.setWidget( i,
                                1,
                                categories );
        currentTable.getFlexCellFormatter().setHorizontalAlignment( i,
                                                                    0,
                                                                    HasHorizontalAlignment.ALIGN_RIGHT );
        currentTable.getFlexCellFormatter().setVerticalAlignment( i,
                                                                  0,
                                                                  HasVerticalAlignment.ALIGN_TOP );
    }

}
