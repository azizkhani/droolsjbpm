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

package org.drools.guvnor.client.modeldriven.ui;

import org.drools.guvnor.client.common.ErrorPopup;
import org.drools.guvnor.client.common.ValueChanged;
import org.drools.guvnor.client.messages.Constants;
import org.drools.ide.common.client.modeldriven.brl.BaseSingleFieldConstraint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * Default editor for literal values, a text box. 
 * 
 * @author Toni Rikkola
 *
 */
public class DefaultLiteralEditor extends Composite {

    private Constants                 constants    = ((Constants) GWT.create( Constants.class ));
    private BaseSingleFieldConstraint constraint;
    private final boolean             numericValue;

    private Label                     textWidget   = new Label();

    private final Button              okButton     = new Button( constants.OK() );
    private final ValueChanged        valueChanged = new ValueChanged() {
                                                       public void valueChanged(String newValue) {
                                                           constraint.setValue( newValue );
                                                           if ( onValueChangeCommand != null ) {
                                                               onValueChangeCommand.execute();
                                                           }
                                                           okButton.click();
                                                       }
                                                   };

    private Command                   onValueChangeCommand;

    public DefaultLiteralEditor(BaseSingleFieldConstraint constraint,
                                boolean numericValue) {
        this.constraint = constraint;
        this.numericValue = numericValue;

        textWidget.setStyleName( "x-form-field" );
        textWidget.addClickHandler( new ClickHandler() {

            public void onClick(ClickEvent event) {
                showPopup();
            }
        } );

        if ( constraint.getValue() != null && !"".equals( constraint.getValue() ) ) {
            textWidget.setText( constraint.getValue() );
        } else {
            textWidget.setText( constants.Value() );
        }

        initWidget( textWidget );
    }

    private void showPopup() {
        final PopupPanel popup = new PopupPanel();
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        popup.setGlassEnabled( true );
        popup.setPopupPosition( this.getAbsoluteLeft(),
                                this.getAbsoluteTop() );

        okButton.addClickHandler( new ClickHandler() {

            public void onClick(ClickEvent event) {

                if ( !isValueEmpty( constraint.getValue() ) ) {
                    if ( onValueChangeCommand != null ) {
                        onValueChangeCommand.execute();
                    }
                    textWidget.setText( constraint.getValue() );

                    popup.hide();
                }
            }
        } );

        horizontalPanel.add( getTextBox() );
        horizontalPanel.add( okButton );

        popup.add( horizontalPanel );

        popup.show();

    }

    public TextBox getTextBox() {

        final TextBox box = new BoundTextBox( constraint );
        box.addKeyboardListener( new KeyboardListener() {

            public void onKeyDown(Widget arg0,
                                  char arg1,
                                  int arg2) {

            }

            public void onKeyPress(Widget w,
                                   char c,
                                   int i) {
                // all primitives will be "numeric" so this does not make sense for char as it will break the logic
            	// allow to enter values
            	//if ( numericValue && Character.isLetter( c ) ) {  	
                //    ((TextBox) w).cancelKey();
                //}
            }

            public void onKeyUp(Widget arg0,
                                char c,
                                int arg2) {
                if ( '\r' == c || '\n' == c ) {
                    valueChanged.valueChanged( box.getText() );
                } else {
                    constraint.setValue( box.getText() );
                }
            }

        } );

        box.setTitle( constants.LiteralValueTip() );

        return box;
    }

    private boolean isValueEmpty(String s) {
        if ( s == null || "".equals( s.trim() ) ) {
            ErrorPopup.showMessage( constants.ValueCanNotBeEmpty() );
            return true;
        } else {
            return false;
        }
    }

    private void executeOnValueChangeCommand() {
        if ( this.onValueChangeCommand != null ) {
            this.onValueChangeCommand.execute();
        }
    }

    public void setOnValueChangeCommand(Command onValueChangeCommand) {
        this.onValueChangeCommand = onValueChangeCommand;
    }
}
