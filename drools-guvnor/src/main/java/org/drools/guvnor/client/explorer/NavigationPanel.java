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
package org.drools.guvnor.client.explorer;

import org.drools.guvnor.client.security.Capabilities;
import org.drools.guvnor.client.security.CapabilitiesManager;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;

/**
 * Navigation panel for the west area.
 * 
 * @author rikkola
 *
 */
public class NavigationPanel extends StackLayoutPanel {

    public NavigationPanel() {
        super( Unit.EM );

        addCategoriesPanel();

        if ( CapabilitiesManager.getInstance().shouldShow( Capabilities.SHOW_PACKAGE_VIEW ) ) {
            addPackagesPanel();
        }

        if ( CapabilitiesManager.getInstance().shouldShow( Capabilities.SHOW_QA ) ) {
            addQAPanel();
        }

        if ( CapabilitiesManager.getInstance().shouldShow( Capabilities.SHOW_DEPLOYMENT,
                                               Capabilities.SHOW_DEPLOYMENT_NEW ) ) {
            addDeploymentPanel();
        }

        if ( CapabilitiesManager.getInstance().shouldShow( Capabilities.SHOW_ADMIN ) ) {
            addAdminPanel();
        }

    }

    private void addAdminPanel() {
        DockLayoutPanel adminDockLayoutPanel = new DockLayoutPanel( Unit.EM );
        AdministrationTree deploymentTreeItem = new AdministrationTree();
        ScrollPanel adminTreeItemPanel = new ScrollPanel( deploymentTreeItem );

        adminDockLayoutPanel.add( adminTreeItemPanel );

        add( adminDockLayoutPanel,
             deploymentTreeItem.getHeaderHTML(),
             2 );
    }

    private void addDeploymentPanel() {
        DockLayoutPanel deploymentDockLayoutPanel = new DockLayoutPanel( Unit.EM );
        DeploymentTree deploymentTreeItem = new DeploymentTree();
        ScrollPanel deploymentTreeItemPanel = new ScrollPanel( deploymentTreeItem );

        if ( CapabilitiesManager.getInstance().shouldShow( Capabilities.SHOW_CREATE_NEW_ASSET ) ) {
            deploymentDockLayoutPanel.addNorth( DeploymentNewMenu.getMenu( deploymentTreeItem ),
                                                2 );
        }

        deploymentDockLayoutPanel.add( deploymentTreeItemPanel );

        add( deploymentDockLayoutPanel,
             deploymentTreeItem.getHeaderHTML(),
             2 );
    }

    private void addQAPanel() {
        DockLayoutPanel qaDockLayoutPanel = new DockLayoutPanel( Unit.EM );
        QATree qaTreeItem = new QATree();
        ScrollPanel qaTreeItemPanel = new ScrollPanel( qaTreeItem );

        qaDockLayoutPanel.add( qaTreeItemPanel );

        add( qaDockLayoutPanel,
             qaTreeItem.getHeaderHTML(),
             2 );
    }

    private void addPackagesPanel() {
        DockLayoutPanel packageDockLayoutPanel = new DockLayoutPanel( Unit.EM );
        final PackagesTree packagesTreeItem = new PackagesTree();
        ScrollPanel packagesTreeItemPanel = new ScrollPanel( packagesTreeItem );

        if ( CapabilitiesManager.getInstance().shouldShow( Capabilities.SHOW_CREATE_NEW_ASSET ) ) {
            packageDockLayoutPanel.addNorth( PackagesNewMenu.getMenu( packagesTreeItem ),
                                             2 );
        }
        packageDockLayoutPanel.add( packagesTreeItemPanel );

        add( packageDockLayoutPanel,
             packagesTreeItem.getHeaderHTML(),
             2 );

        //lazy loaded to easy startup wait time.
        DeferredCommand.addCommand( new Command() {
            public void execute() {
                packagesTreeItem.loadPackageList();
            }
        } );
    }

    private void addCategoriesPanel() {
        DockLayoutPanel browseDockLayoutPanel = new DockLayoutPanel( Unit.EM );
        BrowseTree categoriesTreeItem = new BrowseTree();
        ScrollPanel categoriesTreeItemPanel = new ScrollPanel( categoriesTreeItem );

        if ( CapabilitiesManager.getInstance().shouldShow( Capabilities.SHOW_CREATE_NEW_ASSET ) ) {
            browseDockLayoutPanel.addNorth( RulesNewMenu.getMenu( categoriesTreeItem ),
                                            2 );
        }
        browseDockLayoutPanel.add( categoriesTreeItemPanel );

        add( browseDockLayoutPanel,
             categoriesTreeItem.getHeaderHTML(),
             2 );
    }

}
