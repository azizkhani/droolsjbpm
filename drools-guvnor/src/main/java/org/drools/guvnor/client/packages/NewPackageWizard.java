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

package org.drools.guvnor.client.packages;

import org.drools.guvnor.client.common.ErrorPopup;
import org.drools.guvnor.client.common.FormStyleLayout;
import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.HTMLFileManagerFields;
import org.drools.guvnor.client.common.LoadingPopup;
import org.drools.guvnor.client.common.RulePackageSelector;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.resources.Images;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.util.Format;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

/**
 * This is the wizard used when creating new packages or importing them.
 *
 * @author Michael Neale
 */
public class NewPackageWizard extends FormStylePopup {

    private static Constants      constants        = GWT.create( Constants.class );
    private static Images         images           = GWT.create( Images.class );

    private TextBox               nameBox;
    private TextBox               descBox;
    private final FormStyleLayout importLayout     = new FormStyleLayout();
    private final FormStyleLayout newPackageLayout = new FormStyleLayout();

    public NewPackageWizard(final Command afterCreatedEvent) {
        super( images.newexWiz(),
               constants.CreateANewPackage() );
        nameBox = new TextBox();
        descBox = new TextBox();

        newPackageLayout.addAttribute( constants.NameColon(),
                                       nameBox );
        newPackageLayout.addAttribute( constants.DescriptionColon(),
                                       descBox );

        nameBox.setTitle( constants.PackageNameTip() );

        RadioButton newPackage = new RadioButton( "action",
                                                  constants.CreateNewPackageRadio() );
        RadioButton importPackage = new RadioButton( "action",
                                                     constants.ImportFromDrlRadio() );

        newPackage.setValue( true );
        newPackageLayout.setVisible( true );

        newPackage.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                newPackageLayout.setVisible( true );
                importLayout.setVisible( false );
            }
        } );

        this.setAfterShow( new Command() {
            public void execute() {
                nameBox.setFocus( true );
            }
        } );

        importLayout.setVisible( false );
        importPackage.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                newPackageLayout.setVisible( false );
                importLayout.setVisible( true );
            }
        } );

        VerticalPanel ab = new VerticalPanel();
        ab.add( newPackage );
        ab.add( importPackage );
        addAttribute( "",
                      ab );

        addRow( newPackageLayout );
        addRow( importLayout );

        importLayout.addAttribute( constants.DRLFileToImport(),
                                   newImportWidget( afterCreatedEvent,
                                                    this ) );

        importLayout.addRow( new HTML( "<br/><b>" + constants.NoteNewPackageDrlImportWarning() + "</b>" ) );
        importLayout.addRow( new HTML( constants.ImportDRLDesc1() ) );
        importLayout.addRow( new HTML( constants.ImportDRLDesc2() ) );
        importLayout.addRow( new HTML( constants.ImportDRLDesc3() ) );

        HorizontalPanel hp = new HorizontalPanel();        
        Button create = new Button( constants.CreatePackage() );
        create.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                if ( PackageNameValidator.validatePackageName( nameBox.getText() ) ) {
                    createPackageAction( nameBox.getText(),
                                         descBox.getText(),
                                         afterCreatedEvent );
                    hide();
                } else {
                    nameBox.setText( "" );
                    Window.alert( constants.PackageNameCorrectHint() );
                }
            }
        } );
        hp.add(create);

        Button cancel = new Button( constants.Cancel() );
        cancel.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent arg0) {
            	hide();
            }
        } );
        hp.add(cancel);
        
        newPackageLayout.addAttribute( "",
        		hp );

    }

    private void createPackageAction(final String name,
                                     final String descr,
                                     final Command refresh) {
        LoadingPopup.showMessage( constants.CreatingPackagePleaseWait() );
        RepositoryServiceFactory.getService().createPackage( name,
                                                             descr,
                                                             new GenericCallback<java.lang.String>() {
                                                                 public void onSuccess(String uuid) {
                                                                     RulePackageSelector.currentlySelectedPackage = name;
                                                                     LoadingPopup.close();
                                                                     refresh.execute();
                                                                 }
                                                             } );
    }

    public static Widget newImportWidget(final Command afterCreatedEvent,
                                         final FormStylePopup parent) {

        final FormPanel uploadFormPanel = new FormPanel();
        uploadFormPanel.setAction( GWT.getModuleBaseURL() + "package" );
        uploadFormPanel.setEncoding( FormPanel.ENCODING_MULTIPART );
        uploadFormPanel.setMethod( FormPanel.METHOD_POST );

        VerticalPanel panel = new VerticalPanel();
        uploadFormPanel.setWidget( panel );

        final FileUpload upload = new FileUpload();
        upload.setName( HTMLFileManagerFields.CLASSIC_DRL_IMPORT );
        panel.add( upload );


        HorizontalPanel hp = new HorizontalPanel();        
        Button create = new Button( constants.Import() );
        ClickHandler okClickHandler = new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                if ( Window.confirm( constants.ImportMergeWarning() ) ) {
                    uploadFormPanel.submit();
                }
            }
        };
        create.addClickHandler(okClickHandler);
        hp.add(create);

        Button cancel = new Button( constants.Cancel() );        
        cancel.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent arg0) {
            	parent.hide();
            }
        } );
        hp.add(cancel);
        panel.add( hp );


        final FormStylePopup packageNamePopup = new FormStylePopup( images.packageLarge(),
                                                                    constants.PackageName() );
        HorizontalPanel packageNamePanel = new HorizontalPanel();
        packageNamePopup.addRow( new Label( constants.ImportedDRLContainsNoNameForThePackage() ) );

        final TextBox packageName = new TextBox();
        packageNamePanel.add( new Label( constants.PackageName() + ":" ) );
        packageNamePanel.add( packageName );
        Button uploadWithNameButton = new Button( constants.OK() );
        uploadWithNameButton.addClickHandler( okClickHandler );

        packageNamePanel.add( uploadWithNameButton );
        packageNamePopup.addRow( packageNamePanel );
        uploadFormPanel.addSubmitCompleteHandler( new SubmitCompleteHandler() {
            public void onSubmitComplete(SubmitCompleteEvent event) {
                if ( event.getResults().indexOf( "OK" ) > -1 ) { //NON-NLS
                    Window.alert( constants.PackageWasImportedSuccessfully() );
                    afterCreatedEvent.execute();
                    parent.hide();
                    if ( packageNamePopup != null ) {
                        packageNamePopup.hide();
                    }
                } else if ( event.getResults().indexOf( "Missing package name." ) > -1 ) {
                    LoadingPopup.close();
                    packageNamePopup.show();
                } else {
                    ErrorPopup.showMessage( Format.format( constants.UnableToImportIntoThePackage0(),
                                                           event.getResults() ) );
                }
                LoadingPopup.close();
            }
        } );

        uploadFormPanel.addSubmitHandler( new SubmitHandler() {
            public void onSubmit(SubmitEvent event) {
                if ( upload.getFilename().length() == 0 ) {
                    LoadingPopup.close();
                    Window.alert( constants.YouDidNotChooseADrlFileToImport() );
                    event.cancel();
                } else if ( !upload.getFilename().endsWith( ".drl" ) ) { //NON-NLS
                    LoadingPopup.close();
                    Window.alert( constants.YouCanOnlyImportDrlFiles() );
                    event.cancel();
                } else if ( packageName.getText() != null && !packageName.getText().equals( "" ) ) {
                    LoadingPopup.showMessage( constants.ImportingDRLPleaseWait() );
                    uploadFormPanel.setAction( uploadFormPanel.getAction() + "?packageName=" + packageName.getText() );
                }
            }
        } );

        return uploadFormPanel;
    }

}