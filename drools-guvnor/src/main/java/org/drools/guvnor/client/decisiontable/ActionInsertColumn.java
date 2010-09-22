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

import java.util.HashSet;
import java.util.Set;

import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.ImageButton;
import org.drools.guvnor.client.common.InfoPopup;
import org.drools.guvnor.client.common.SmallLabel;
import org.drools.guvnor.client.messages.Constants;
import org.drools.ide.common.client.modeldriven.FieldAccessorsAndMutators;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.dt.ActionCol;
import org.drools.ide.common.client.modeldriven.dt.ActionInsertFactCol;
import org.drools.ide.common.client.modeldriven.dt.GuidedDecisionTable;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * This is an editor for columns that are for inserting facts.
 * @author Michael Neale
 *
 */
public class ActionInsertColumn extends FormStylePopup {

    private GuidedDecisionTable        dt;
    private SuggestionCompletionEngine sce;
    private ActionInsertFactCol        editingCol;
    private SmallLabel                 patternLabel = new SmallLabel();
    private TextBox                    fieldLabel   = getFieldLabel();
    private Constants                  constants    = GWT.create( Constants.class );

    public ActionInsertColumn(SuggestionCompletionEngine sce,
                              final GuidedDecisionTable dt,
                              final Command refreshGrid,
                              final ActionInsertFactCol col,
                              final boolean isNew) {
        this.setModal( false );
        this.dt = dt;
        this.sce = sce;
        this.editingCol = new ActionInsertFactCol();
        editingCol.boundName = col.boundName;
        editingCol.type = col.type;
        editingCol.factField = col.factField;
        editingCol.factType = col.factType;
        editingCol.header = col.header;
        editingCol.valueList = col.valueList;
        editingCol.defaultValue = col.defaultValue;
        editingCol.hideColumn = col.hideColumn;

        setTitle( constants.ActionColumnConfigurationInsertingANewFact() );

        HorizontalPanel pattern = new HorizontalPanel();
        pattern.add( patternLabel );
        doPatternLabel();

        Image changePattern = new ImageButton( "images/edit.gif",
                                               constants.ChooseAPatternThatThisColumnAddsDataTo(),
                                               new ClickHandler() {
                                                   public void onClick(ClickEvent w) {
                                                       showChangePattern( w );
                                                   }
                                               } ); //NON-NLS
        pattern.add( changePattern );
        addAttribute( constants.Pattern(),
                      pattern );

        HorizontalPanel field = new HorizontalPanel();
        field.add( fieldLabel );
        Image editField = new ImageButton( "images/edit.gif",
                                           constants.EditTheFieldThatThisColumnOperatesOn(),
                                           new ClickHandler() {
                                               public void onClick(ClickEvent w) {
                                                   showFieldChange();
                                               }
                                           } ); //NON-NLS
        field.add( editField );
        addAttribute( constants.Field(),
                      field );
        doFieldLabel();

        final TextBox valueList = new TextBox();
        valueList.setText( editingCol.valueList );
        valueList.addChangeListener( new ChangeListener() {
            public void onChange(Widget w) {
                editingCol.valueList = valueList.getText();
            }
        } );
        HorizontalPanel vl = new HorizontalPanel();
        vl.add( valueList );
        vl.add( new InfoPopup( constants.ValueList(),
                               constants.ValueListsExplanation() ) );
        addAttribute( constants.optionalValueList(),
                      vl );

        final TextBox header = new TextBox();
        header.setText( col.header );
        header.addChangeListener( new ChangeListener() {
            public void onChange(Widget w) {
                editingCol.header = header.getText();
            }
        } );
        addAttribute( constants.ColumnHeaderDescription(),
                      header );

        addAttribute( constants.DefaultValue(),
                      GuidedDTColumnConfig.getDefaultEditor( editingCol ) );

        Button apply = new Button( constants.ApplyChanges() );
        apply.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent w) {
                if ( null == editingCol.header || "".equals( editingCol.header ) ) {
                    Window.alert( constants.YouMustEnterAColumnHeaderValueDescription() );
                    return;
                }
                if ( isNew ) {
                    if ( !unique( editingCol.header ) ) {
                        Window.alert( constants.ThatColumnNameIsAlreadyInUsePleasePickAnother() );
                        return;
                    }
                    dt.actionCols.add( editingCol );
                } else {
                    if ( !col.header.equals( editingCol.header ) ) {
                        if ( !unique( editingCol.header ) ) {
                            Window.alert( constants.ThatColumnNameIsAlreadyInUsePleasePickAnother() );
                            return;
                        }
                    }
                    col.boundName = editingCol.boundName;
                    col.type = editingCol.type;
                    col.factField = editingCol.factField;
                    col.factType = editingCol.factType;
                    col.header = editingCol.header;
                    col.valueList = editingCol.valueList;
                    col.defaultValue = editingCol.defaultValue;
                    col.hideColumn = editingCol.hideColumn;
                }
                refreshGrid.execute();
                hide();
            }
        } );
        addAttribute( "",
                      apply );

    }

    private boolean unique(String header) {
        for ( ActionCol o : dt.actionCols ) {
            if ( o.header.equals( header ) ) return false;
        }
        return true;
    }

    private TextBox getFieldLabel() {
        final TextBox box = new TextBox();
        box.addChangeListener( new ChangeListener() {
            public void onChange(Widget w) {
                editingCol.factField = box.getText();
            }
        } );
        return box;
    }

    private void showFieldChange() {
        final FormStylePopup pop = new FormStylePopup();
        pop.setModal( false );
        String[] fields = this.sce.getFieldCompletions( FieldAccessorsAndMutators.MUTATOR,
                                                        this.editingCol.factType );
        final ListBox box = new ListBox();
        for ( int i = 0; i < fields.length; i++ ) {
            box.addItem( fields[i] );
        }
        pop.addAttribute( constants.Field(),
                          box );
        Button b = new Button( constants.OK() );
        pop.addAttribute( "",
                          b );
        b.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent w) {
                editingCol.factField = box.getItemText( box.getSelectedIndex() );
                editingCol.type = sce.getFieldType( editingCol.factType,
                                                    editingCol.factField );
                doFieldLabel();
                pop.hide();
            }
        } );
        pop.show();

    }

    private void doFieldLabel() {
        if ( nil( this.editingCol.factField ) ) {
            fieldLabel.setText( constants.pleaseChooseFactType() );
        } else {
            fieldLabel.setText( editingCol.factField );
        }

    }

    private boolean nil(String s) {
        return s == null || s.equals( "" );
    }

    private void doPatternLabel() {
        if ( this.editingCol.factType != null ) {
            this.patternLabel.setText( this.editingCol.factType + " [" + editingCol.boundName + "]" );
        }
    }

    protected void showChangePattern(ClickEvent w) {

        final ListBox pats = this.loadPatterns();
        if ( pats.getItemCount() == 0 ) {
            showNewPatternDialog();
            return;
        }
        final FormStylePopup pop = new FormStylePopup();
        Button ok = new Button( "OK" );
        HorizontalPanel hp = new HorizontalPanel();
        hp.add( pats );
        hp.add( ok );

        pop.addAttribute( constants.ChooseExistingPatternToAddColumnTo(),
                          hp );
        pop.addAttribute( "",
                          new HTML( constants.ORwithEmphasis() ) );

        Button createPattern = new Button( constants.CreateNewFactPattern() );
        createPattern.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent w) {
                pop.hide();
                showNewPatternDialog();
            }
        } );
        pop.addAttribute( "",
                          createPattern );

        ok.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent w) {
                String[] val = pats.getValue( pats.getSelectedIndex() ).split( "\\s" ); //NON-NLS
                editingCol.factType = val[0];
                editingCol.boundName = val[1];
                doPatternLabel();
                pop.hide();
            }
        } );

        pop.show();
    }

    protected void showNewPatternDialog() {
        final FormStylePopup pop = new FormStylePopup();
        pop.setTitle( constants.NewFactSelectTheType() );
        final ListBox types = new ListBox();
        for ( int i = 0; i < sce.getFactTypes().length; i++ ) {
            types.addItem( sce.getFactTypes()[i] );
        }
        pop.addAttribute( constants.FactType(),
                          types );
        final TextBox binding = new TextBox();
        pop.addAttribute( constants.name(),
                          binding );

        Button ok = new Button( constants.OK() );
        ok.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent w) {
                editingCol.boundName = binding.getText();
                editingCol.factType = types.getItemText( types.getSelectedIndex() );
                doPatternLabel();
                pop.hide();
            }
        } );
        pop.addAttribute( "",
                          ok );

        pop.show();
    }

    private ListBox loadPatterns() {
        Set vars = new HashSet();
        ListBox patterns = new ListBox();

        for ( Object o : dt.actionCols ) {
            ActionCol col = (ActionCol) o;
            if ( col instanceof ActionInsertFactCol ) {
                ActionInsertFactCol c = (ActionInsertFactCol) col;
                if ( !vars.contains( c.boundName ) ) {
                    patterns.addItem( c.factType + " [" + c.boundName + "]",
                                      c.factType + " " + c.boundName );
                    vars.add( c.boundName );
                }
            }

        }

        return patterns;

    }

}
