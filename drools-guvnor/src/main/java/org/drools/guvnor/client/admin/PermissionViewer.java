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

package org.drools.guvnor.client.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.guvnor.client.categorynav.CategoryExplorerWidget;
import org.drools.guvnor.client.categorynav.CategorySelectHandler;
import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.ImageButton;
import org.drools.guvnor.client.common.InfoPopup;
import org.drools.guvnor.client.common.LoadingPopup;
import org.drools.guvnor.client.common.RulePackageSelector;
import org.drools.guvnor.client.common.SmallLabel;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.resources.Images;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.util.Format;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.SortDir;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.GroupingStore;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.SortState;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.ToolbarSeparator;
import com.gwtext.client.widgets.ToolbarTextItem;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.event.GridRowListenerAdapter;

public class PermissionViewer extends Composite {

    private static Images images    = (Images) GWT.create( Images.class );
    private Constants     constants = ((Constants) GWT.create( Constants.class ));

    private VerticalPanel layout;
    private GridPanel     grid;

    public PermissionViewer() {
        layout = new VerticalPanel();
        layout.setHeight( "100%" );
        layout.setWidth( "100%" );

        layout.add( howToTurnOn() );

        refresh();
        initWidget( layout );
    }

    private Widget howToTurnOn() {
        HorizontalPanel hp = new HorizontalPanel();
        hp.add( new HTML( "<small><i>" + constants.TipAuthEnable() + "</i></small>" ) );
        InfoPopup pop = new InfoPopup( constants.EnablingAuthorization(), constants.EnablingAuthPopupTip() );
        hp.add( pop );
        return hp;
    }

    private void refresh() {
        LoadingPopup.showMessage( constants.LoadingUserPermissions() );
        RepositoryServiceFactory.getService().listUserPermissions( new GenericCallback<Map<String, List<String>>>() {
            public void onSuccess(Map<String, List<String>> list) {
                if ( grid != null ) {
                    layout.clear();
                    //layout.remove(grid);
                    grid.destroy();
                }
                showUsers( list );
                LoadingPopup.close();
            }
        } );
    }

    private void showUsers(Map<String, List<String>> users) {

        //showing a grid - the cols are:
        //userName, hasAdmin, hasPackage, hasCategory permissions...
        //double click to edit
        Object[][] data = new Object[users.size()][4];

        int row = 0;
        for ( Map.Entry<String, List<String>> userRow : users.entrySet() ) {
            data[row][0] = userRow.getKey();
            List<String> permTypes = userRow.getValue();
            data[row][1] = isAdmin( permTypes );
            data[row][2] = isPackage( permTypes );
            data[row][3] = isCategory( permTypes );

            row++;
        }

        MemoryProxy proxy = new MemoryProxy( data );
        RecordDef recordDef = new RecordDef( new FieldDef[]{new StringFieldDef( "userName" ), //NON-NLS
                new StringFieldDef( "isAdmin" ), //NON-NLS
                new StringFieldDef( "isPackage" ), //NON-NLS
                new StringFieldDef( "isCategory" ), //NON-NLS
        } );

        ArrayReader reader = new ArrayReader( recordDef );
        GroupingStore store = new GroupingStore();
        store.setReader( reader );
        store.setDataProxy( proxy );
        store.setGroupField( "isAdmin" ); //NON-NLS
        store.setSortInfo( new SortState( "userName", SortDir.ASC ) ); //NON-NLS
        store.load();

        ColumnModel cm = new ColumnModel( new ColumnConfig[]{new ColumnConfig() {
            {
                setDataIndex( "userName" ); //NON-NLS
                setSortable( true );
                setHeader( constants.UserName1() );
            }
        }, new ColumnConfig() {
            {
                setHeader( constants.Administrator() );
                setSortable( true );
                setDataIndex( "isAdmin" ); //NON-NLS
            }
        }, new ColumnConfig() {
            {
                setHeader( constants.HasPackagePermissions() );
                setSortable( true );
                setDataIndex( "isPackage" ); //NON-NLS
            }
        }, new ColumnConfig() {
            {
                setHeader( constants.HasCategoryPermissions() );
                setSortable( true );
                setDataIndex( "isCategory" ); //NON-NLS
            }
        }

        } );

        grid = new GridPanel();
        grid.setColumnModel( cm );
        grid.setStore( store );
        grid.setWidth( 550 );
        grid.setHeight( 600 );

        /* MN Don't want grouping.
        GroupingView gv = new GroupingView();
        //to stretch it out
        gv.setForceFit(true);
        gv.setGroupTextTpl("{text} ({[values.rs.length]} {[values.rs.length > 1 ? \"Items\" : \"Item\"]})");
        grid.setView(gv);
        */
        Toolbar tb = new Toolbar();
        grid.setTopToolbar( tb );

        tb.addItem( new ToolbarTextItem( constants.CurrentlyConfiguredUsers() ) );
        tb.addItem( new ToolbarSeparator() );

        layout.add( grid );
        ToolbarButton reload = new ToolbarButton( constants.Reload1() );
        reload.addListener( new ButtonListenerAdapter() {
            public void onClick(Button button, EventObject e) {
                refresh();
            }
        } );
        tb.addButton( reload );

        grid.addGridRowListener( new GridRowListenerAdapter() {
            @Override
            public void onRowDblClick(GridPanel grid, int rowIndex, EventObject e) {
                String userName = grid.getSelectionModel().getSelected().getAsString( "userName" ); //NON-NLS
                showEditor( userName );
            }
        } );

        ToolbarButton create = new ToolbarButton( constants.CreateNewUserMapping() );
        create.addListener( new ButtonListenerAdapter() {
            public void onClick(Button button, EventObject e) {
                final FormStylePopup form = new FormStylePopup( images.snapshot(), constants.EnterNewUserName() );
                final TextBox userName = new TextBox();
                form.addAttribute( constants.NewUserName(), userName );

                com.google.gwt.user.client.ui.Button create = new com.google.gwt.user.client.ui.Button( constants.OK() );
                form.addAttribute( "", create );
                create.addClickHandler( new ClickHandler() {
                    public void onClick(ClickEvent w) {
                        if ( userName.getText() != null && userName.getText().length() != 0 ) {
                            RepositoryServiceFactory.getService().createUser( userName.getText(), new GenericCallback<java.lang.Void>() {
                                public void onSuccess(Void a) {
                                    refresh();
                                    showEditor( userName.getText() );
                                }

                                public void onFailure(Throwable t) {
                                    super.onFailure( t );
                                }
                            } );
                            form.hide();
                        }
                    }
                } );

                form.show();
            }

        } );
        tb.addButton( create );

        ToolbarButton delete = new ToolbarButton( constants.DeleteSelectedUser() );
        delete.addListener( new ButtonListenerAdapter() {
            public void onClick(Button button, EventObject e) {
                final String userName = grid.getSelectionModel().getSelected().getAsString( "userName" ); //NON-NLS
                if ( userName != null && Window.confirm( Format.format( constants.AreYouSureYouWantToDeleteUser0(), userName ) ) ) {
                    RepositoryServiceFactory.getService().deleteUser( userName, new GenericCallback<java.lang.Void>() {
                        public void onSuccess(Void a) {
                            refresh();
                        }
                    } );
                }
            }
        } );
        tb.addButton( delete );

    }

    private void showEditor(final String userName) {
        LoadingPopup.showMessage( constants.LoadingUsersPermissions() );
        RepositoryServiceFactory.getService().retrieveUserPermissions( userName, new GenericCallback<Map<String, List<String>>>() {
            public void onSuccess(final Map<String, List<String>> perms) {
                final FormStylePopup editor = new FormStylePopup( images.management(), Format.format( constants.EditUser0(), userName ) );
                editor.addRow( new HTML( "<i>" + constants.UserAuthenticationTip() + "</i>" ) );
                //now render the actual permissions...
                VerticalPanel vp = new VerticalPanel();
                editor.addAttribute( "", doPermsPanel( perms, vp ) );

                HorizontalPanel hp = new HorizontalPanel();
                com.google.gwt.user.client.ui.Button save = new com.google.gwt.user.client.ui.Button( constants.SaveChanges() );
                hp.add( save );
                editor.addAttribute( "", hp );
                save.addClickHandler( createClickHandlerForSaveButton( userName, perms, editor ) );

                com.google.gwt.user.client.ui.Button cancel = new com.google.gwt.user.client.ui.Button( constants.Cancel() );
                hp.add( cancel );
                cancel.addClickHandler( new ClickHandler() {
                    public void onClick(ClickEvent w) {
                        editor.hide();
                    }
                } );

                editor.show();
                LoadingPopup.close();
            }

            private ClickHandler createClickHandlerForSaveButton(final String userName, final Map<String, List<String>> perms, final FormStylePopup editor) {
                return new ClickHandler() {
                    public void onClick(ClickEvent w) {
                        LoadingPopup.showMessage( constants.Updating() );
                        RepositoryServiceFactory.getService().updateUserPermissions( userName, perms, new GenericCallback<java.lang.Void>() {
                            public void onSuccess(Void a) {
                                LoadingPopup.close();
                                refresh();
                                editor.hide();
                            }
                        } );

                    }
                };
            }
        } );
    }

    /**
     * The permissions panel.
     */
    private Widget doPermsPanel(final Map<String, List<String>> perms, final Panel vp) {
        vp.clear();

        for ( Map.Entry<String, List<String>> perm : perms.entrySet() ) {
            if ( perm.getKey().equals( "admin" ) ) { //NON-NLS
                HorizontalPanel h = new HorizontalPanel();
                h.add( new HTML( "<b>" + constants.ThisUserIsAnAdministrator() + "</b>" ) ); //NON-NLS
                com.google.gwt.user.client.ui.Button del = new com.google.gwt.user.client.ui.Button( constants.RemoveAdminRights() );

                del.addClickHandler( new ClickHandler() {
                    public void onClick(ClickEvent w) {
                        if ( Window.confirm( constants.AreYouSureYouWantToRemoveAdministratorPermissions() ) ) {
                            perms.remove( "admin" ); //NON-NLS
                            doPermsPanel( perms, vp );
                        }
                    }
                } );
                h.add( del );
                vp.add( h );
            } else {
                final String permType = perm.getKey();
                final List<String> permList = perm.getValue();

                Grid g = new Grid( permList.size() + 1, 3 );
                g.setWidget( 0, 0, new HTML( "<b>[" + permType + "] for:</b>" ) ); //NON-NLS

                for ( int i = 0; i < permList.size(); i++ ) {
                    final String p = permList.get( i );
                    ImageButton del = new ImageButton( images.deleteItemSmall(), constants.RemovePermission(), new ClickHandler() {
                        public void onClick(ClickEvent w) {
                            if ( Window.confirm( Format.format( constants.AreYouSureYouWantToRemovePermission0(), p ) ) ) {
                                permList.remove( p );
                                if ( permList.size() == 0 ) {
                                    perms.remove( permType );
                                }
                                doPermsPanel( perms, vp );
                            }
                        }
                    } );

                    g.setWidget( i + 1, 1, new SmallLabel( p ) );
                    g.setWidget( i + 1, 2, del );
                }

                vp.add( g );
            }

        }

        //now to be able to add...
        ImageButton newPermission = new ImageButton( images.newItem(), constants.AddANewPermission(), createClickHandlerForNewPersmissionImageButton( perms, vp ) );
        vp.add( newPermission );
        return vp;
    }

    private ClickHandler createClickHandlerForNewPersmissionImageButton(final Map<String, List<String>> perms, final Panel vp) {
        return new ClickHandler() {
            public void onClick(ClickEvent w) {
                final FormStylePopup pop = new FormStylePopup();
                final ListBox permTypeBox = new ListBox();
                permTypeBox.addItem( constants.Loading() );

                HorizontalPanel hp = new HorizontalPanel();
                hp.add( permTypeBox );
                hp.add( new InfoPopup( constants.PermissionDetails(), constants.PermissionDetailsTip() ) );
                pop.addAttribute( constants.PermissionType(), hp );

                RepositoryServiceFactory.getService().listAvailablePermissionTypes( new GenericCallback<String[]>() {
                    public void onSuccess(String[] items) {
                        permTypeBox.clear();
                        permTypeBox.addItem( constants.pleaseChoose1() );
                        for ( String s : items ) {
                            permTypeBox.addItem( s );
                        }
                    }
                } );

                permTypeBox.addChangeHandler( createChangeHandlerForPermTypeBox( perms, vp, pop, permTypeBox ) );

                pop.show();
            }

            private ChangeHandler createChangeHandlerForPermTypeBox(final Map<String, List<String>> perms, final Panel vp, final FormStylePopup pop, final ListBox permTypeBox) {
                return new ChangeHandler() {
                    public void onChange(ChangeEvent event) {
                        pop.clear();
                        final String sel = permTypeBox.getItemText( permTypeBox.getSelectedIndex() );
                        if ( sel.equals( "admin" ) ) { //NON-NLS
                            createButtonsAndHandlersForAdmin( perms, vp, pop );
                        } else if ( sel.startsWith( "analyst" ) ) { //NON-NLS
                            CategoryExplorerWidget cat = createCategoryExplorerWidget( perms, vp, pop, sel );
                            pop.addAttribute( constants.SelectCategoryToProvidePermissionFor(), cat );
                        } else if ( sel.startsWith( "package" ) ) {
                            createButtonsPanelsAndHandlersForPackage( perms, vp, pop, sel );
                        }
                    }

                    private void createButtonsPanelsAndHandlersForPackage(final Map<String, List<String>> perms, final Panel vp, final FormStylePopup pop, final String sel) {
                        final RulePackageSelector rps = new RulePackageSelector( true );
                        com.google.gwt.user.client.ui.Button ok = new com.google.gwt.user.client.ui.Button( constants.OK() );
                        ok.addClickHandler( new ClickHandler() {
                            public void onClick(ClickEvent w) {
                                String pkName = rps.getSelectedPackage();
                                if ( perms.containsKey( sel ) ) {
                                    perms.get( sel ).add( "package=" + pkName ); //NON-NLS
                                } else {
                                    List<String> ls = new ArrayList<String>();
                                    ls.add( "package=" + pkName ); //NON-NLS
                                    perms.put( sel, ls );
                                }

                                doPermsPanel( perms, vp );
                                pop.hide();

                            }
                        } );

                        HorizontalPanel hp = new HorizontalPanel();
                        hp.add( rps );
                        hp.add( ok );
                        pop.addAttribute( constants.SelectPackageToApplyPermissionTo(), hp );
                    }

                    private CategoryExplorerWidget createCategoryExplorerWidget(final Map<String, List<String>> perms, final Panel vp, final FormStylePopup pop, final String sel) {
                        CategoryExplorerWidget cat = new CategoryExplorerWidget( new CategorySelectHandler() {
                            public void selected(String selectedPath) {
                                if ( perms.containsKey( sel ) ) {
                                    perms.get( sel ).add( "category=" + selectedPath ); //NON-NLS
                                } else {
                                    List<String> ls = new ArrayList<String>();
                                    ls.add( "category=" + selectedPath ); //NON-NLS
                                    perms.put( sel, ls );
                                }
                                doPermsPanel( perms, vp );
                                pop.hide();
                            }
                        } );
                        return cat;
                    }

                    private void createButtonsAndHandlersForAdmin(final Map<String, List<String>> perms, final Panel vp, final FormStylePopup pop) {
                        com.google.gwt.user.client.ui.Button ok = new com.google.gwt.user.client.ui.Button( constants.OK() );

                        pop.addAttribute( constants.MakeThisUserAdmin(), ok );
                        ok.addClickHandler( new ClickHandler() {
                            public void onClick(ClickEvent w) {
                                perms.put( "admin", new ArrayList<String>() ); //NON-NLS

                                doPermsPanel( perms, vp );
                                pop.hide();
                            }
                        } );
                        com.google.gwt.user.client.ui.Button cancel = new com.google.gwt.user.client.ui.Button( constants.Cancel() );

                        pop.addAttribute( "", cancel );
                        cancel.addClickHandler( new ClickHandler() {
                            public void onClick(ClickEvent w) {
                                pop.hide();
                            }
                        } );
                    }
                };
            }
        };
    }

    private Object isCategory(List<String> permTypes) {
        for ( String s : permTypes ) {
            if ( s.startsWith( "analyst" ) ) return constants.Yes(); //NON-NLS
        }
        return "";
    }

    private String isPackage(List<String> permTypes) {
        for ( String s : permTypes ) {
            if ( s.startsWith( "package" ) ) return constants.Yes();
        }
        return "";
    }

    private String isAdmin(List<String> permTypes) {
        if ( permTypes.contains( "admin" ) ) { //NON-NLS
            return constants.Yes();
        } else {
            return "";
        }
    }

}
