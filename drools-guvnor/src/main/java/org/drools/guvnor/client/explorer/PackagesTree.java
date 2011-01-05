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

import java.util.Arrays;
import java.util.Map;

import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.common.RulePackageSelector;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.resources.Images;
import org.drools.guvnor.client.rpc.PackageConfigData;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.util.TabOpener;
import org.drools.guvnor.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class PackagesTree extends AbstractTree implements OpenHandler<TreeItem> {
    private static Constants constants      = GWT.create( Constants.class );
    private static Images    images         = (Images) GWT.create( Images.class );

    private boolean          packagesLoaded = false;

    public PackagesTree() {
        this.name = constants.KnowledgeBases();
        this.image = images.packages();

        mainTree.setAnimationEnabled( true );
        //lazy loaded to easy startup wait time.
        //setupPackagesTree(this.centertabbedPanel);
        mainTree.addSelectionHandler( this );
        mainTree.addOpenHandler( (OpenHandler<TreeItem>) this );

        //these panels are lazy loaded to easy startup wait time.
        /*        addListener(new PanelListenerAdapter() {
                	public void onExpand(Panel panel) {
                        loadPackageList();
                    }
                });

                add(packagesPanel);
                */
    }

    @Override
    protected Tree createTree() {
        return new Tree();
    }

    public void loadPackageList() {
        if ( !packagesLoaded ) {
            setupPackagesTree();
            packagesLoaded = true;
        }
    }

    public void refreshTree() {
        mainTree.clear();
        itemWidgets.clear();

        setupPackagesTree();
    }

    private void setupPackagesTree() {
		TreeItem packageRootNode = new TreeItem(Util.getHeader(
				images.chartOrganisation(), constants.Packages()));
		setupPackageNode(packageRootNode);
		mainTree.addItem(packageRootNode);

		setupGlobalNode( mainTree, itemWidgets );
    }
    
	private void setupPackageNode(final TreeItem packageRootNode) {
		packageRootNode.setState(true);
		packageRootNode.setUserObject(new String("rootNode"));
		
        RepositoryServiceFactory.getService().listPackages( new GenericCallback<PackageConfigData[]>() {
            public void onSuccess(PackageConfigData[] value) {
                PackageHierarchy ph = new PackageHierarchy();

                for ( PackageConfigData val : value ) {
                    ph.addPackage( val );
                }

                for ( PackageHierarchy.Folder hf : ph.root.children ) {
                    buildPkgTree( packageRootNode, hf );
                }

                //root.expand();
            }
        } );		
	}

    private void setupGlobalNode(final Tree root,
                            final Map<TreeItem, String> itemWidgets) {
        RepositoryServiceFactory.getService().loadGlobalPackage( new GenericCallback<PackageConfigData>() {
            public void onSuccess(PackageConfigData value) {
                TreeItem globalRootNode = ExplorerNodeConfig.getPackageItemStructure( constants.GlobalArea(),
                                                                                      value.uuid,
                                                                                      itemWidgets );
                globalRootNode.setHTML( Util.getHeader( images.chartOrganisation(),
                                                        constants.GlobalArea() ) );
                globalRootNode.setUserObject( value );

                root.addItem( globalRootNode );
            }
        } );
    }

    private void buildPkgTree(TreeItem root,
                              PackageHierarchy.Folder fldr) {
        if ( fldr.conf != null ) {
            root.addItem( loadPackage( fldr.name,
                                       fldr.conf ) );
        } else {
            TreeItem tn = new TreeItem( Util.getHeader( images.emptyPackage(),
                                                        fldr.name ) );
            //itemWidgets.put(item, AssetFormats.BUSINESS_RULE_FORMATS[0]);
            root.addItem( tn );

            for ( PackageHierarchy.Folder c : fldr.children ) {
                buildPkgTree( tn,
                              c );
            }
        }
    }

    private TreeItem loadPackage(String name,
                                 PackageConfigData conf) {
        TreeItem pn = ExplorerNodeConfig.getPackageItemStructure( name,
                                                                  conf.uuid,
                                                                  itemWidgets );
        pn.setUserObject( conf );
        return pn;
    }

    public static String key(String[] fmts,
                             PackageConfigData userObject) {
        String key = userObject.uuid;
        for ( String fmt : fmts ) {
            key = key + fmt;
        }
        if ( fmts.length == 0 ) {
            key = key + "[0]";
        }
        return key;
    }

    // Show the associated widget in the deck panel
    public void onSelection(SelectionEvent<TreeItem> event) {
        TreeItem node = event.getSelectedItem();
        Object userObject = node.getUserObject();

        TabOpener opener = TabOpener.getInstance();

        if ( userObject != null) {
            if ( userObject instanceof PackageConfigData && !((PackageConfigData) userObject).isGlobal() ) {
                PackageConfigData pc = (PackageConfigData) userObject;
                RulePackageSelector.currentlySelectedPackage = pc.name;

                String uuid = pc.uuid;
                opener.openPackageEditor( uuid,
                                          new Command() {
                                              public void execute() {
                                                  // refresh the package tree.
                                                  refreshTree();
                                              }
                                          } );
            } else if ( userObject instanceof String[] ) {
                final String[] formats = (String[]) userObject;
                final PackageConfigData packageConfigData = (PackageConfigData) node.getParentItem().getUserObject();
                RulePackageSelector.currentlySelectedPackage = packageConfigData.name;
                String key = key( formats,
                                  packageConfigData );
                opener.openPackageViewAssets( packageConfigData.uuid,
                                              packageConfigData.name,
                                              key,
                                              formats.length == 0 ? null : Arrays.asList(formats),
                                              formats.length == 0 ? Boolean.TRUE : null,
                                              node.getText() );
            } else {
                throw new IllegalArgumentException("The userObject (" + userObject + ") is not supported.");
            }
        }
    }    

    public void onOpen(OpenEvent<TreeItem> event) {
        TreeItem node = event.getTarget();
        Object userObject = node.getUserObject();

        if ( userObject != null && userObject instanceof String && "rootNode".equals((String)userObject)) {
        	node.removeItems();
        	setupPackageNode(node);
        }
    }
}
