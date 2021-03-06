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

package org.drools.guvnor.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.Person;
import org.drools.RuleBase;
import org.drools.StatelessSession;
import org.drools.core.util.BinaryRuleBaseLoader;
import org.drools.core.util.DateUtils;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.client.explorer.ExplorerNodeConfig;
import org.drools.guvnor.client.rpc.BuilderResult;
import org.drools.guvnor.client.rpc.BulkTestRunResult;
import org.drools.guvnor.client.rpc.DetailedSerializationException;
import org.drools.guvnor.client.rpc.DiscussionRecord;
import org.drools.guvnor.client.rpc.MetaDataQuery;
import org.drools.guvnor.client.rpc.PackageConfigData;
import org.drools.guvnor.client.rpc.RepositoryService;
import org.drools.guvnor.client.rpc.RuleAsset;
import org.drools.guvnor.client.rpc.RuleContentText;
import org.drools.guvnor.client.rpc.ScenarioResultSummary;
import org.drools.guvnor.client.rpc.ScenarioRunResult;
import org.drools.guvnor.client.rpc.SingleScenarioResult;
import org.drools.guvnor.client.rpc.SnapshotDiff;
import org.drools.guvnor.client.rpc.SnapshotDiffs;
import org.drools.guvnor.client.rpc.SnapshotInfo;
import org.drools.guvnor.client.rpc.TableConfig;
import org.drools.guvnor.client.rpc.TableDataResult;
import org.drools.guvnor.client.rpc.TableDataRow;
import org.drools.guvnor.client.rpc.ValidatedResponse;
import org.drools.guvnor.client.rulelist.AssetItemGrid;
import org.drools.guvnor.server.repository.UserInbox;
import org.drools.guvnor.server.util.TableDisplayHandler;
import org.drools.guvnor.server.util.TestEnvironmentSessionHelper;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.brl.ActionFieldValue;
import org.drools.ide.common.client.modeldriven.brl.ActionSetField;
import org.drools.ide.common.client.modeldriven.brl.BaseSingleFieldConstraint;
import org.drools.ide.common.client.modeldriven.brl.FactPattern;
import org.drools.ide.common.client.modeldriven.brl.PortableObject;
import org.drools.ide.common.client.modeldriven.brl.RuleModel;
import org.drools.ide.common.client.modeldriven.brl.SingleFieldConstraint;
import org.drools.ide.common.client.modeldriven.dt.ActionSetFieldCol;
import org.drools.ide.common.client.modeldriven.dt.ConditionCol;
import org.drools.ide.common.client.modeldriven.dt.GuidedDecisionTable;
import org.drools.ide.common.client.modeldriven.testing.ExecutionTrace;
import org.drools.ide.common.client.modeldriven.testing.FactData;
import org.drools.ide.common.client.modeldriven.testing.FieldData;
import org.drools.ide.common.client.modeldriven.testing.Scenario;
import org.drools.ide.common.client.modeldriven.testing.VerifyFact;
import org.drools.ide.common.client.modeldriven.testing.VerifyField;
import org.drools.ide.common.client.modeldriven.testing.VerifyRuleFired;
import org.drools.ide.common.server.util.BRXMLPersistence;
import org.drools.ide.common.server.util.ScenarioXMLPersistence;
import org.drools.repository.AssetItem;
import org.drools.repository.AssetItemIterator;
import org.drools.repository.CategoryItem;
import org.drools.repository.PackageItem;
import org.drools.repository.RulesRepository;
import org.drools.repository.RulesRepositoryException;
import org.drools.repository.StateItem;
import org.drools.repository.UserInfo.InboxEntry;
import org.drools.rule.Package;
import org.drools.type.DateFormatsImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gwt.user.client.rpc.SerializationException;

/**
 * This is really a collection of integration tests.
 * @author Michael Neale
 */
public class ServiceImplementationTest extends GuvnorTestBase {

//    @Before
//    public void setUp() {
//        setUpSeam();
//        setUpMockIdentity();
//    }
//
//    @After
//    public void tearDown() {
//        tearAllDown();
//    }

    @Test
    @Ignore("this test fail intermittently")
    public void testInboxEvents() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        assertNotNull( impl.loadInbox( ExplorerNodeConfig.RECENT_EDITED_ID ) );

        //this should trigger the fact that the original user edited something
        AssetItem as = impl.repository.loadDefaultPackage().addAsset( "testLoadInbox",
                                                                      "" );
        as.checkin( "" );
        TableDataResult res = impl.loadInbox( ExplorerNodeConfig.RECENT_EDITED_ID );
        boolean found = false;
        for ( TableDataRow row : res.data ) {
            if ( row.id.equals( as.getUUID() ) ) found = true;
        }
        assertTrue( found );

        //but should not be in "incoming" yet
        found = false;
        res = impl.loadInbox( ExplorerNodeConfig.INCOMING_ID );
        for ( TableDataRow row : res.data ) {
            if ( row.id.equals( as.getUUID() ) ) found = true;
        }
        assertFalse( found );

        //Now, another user comes along, makes a change...
        RulesRepository repo2 = new RulesRepository( TestEnvironmentSessionHelper.getSessionFor( "thirdpartyuser" ) );
        AssetItem as2 = repo2.loadDefaultPackage().loadAsset( "testLoadInbox" );
        as2.updateContent( "hey" );
        as2.checkin( "here we go again !" );

        Thread.sleep( 200 );

        //now check that it is in the first users inbox
        TableDataRow rowMatch = null;
        res = impl.loadInbox( ExplorerNodeConfig.INCOMING_ID );
        for ( TableDataRow row : res.data ) {
            if ( row.id.equals( as.getUUID() ) ) {
                rowMatch = row;
                break;
            }
        }
        assertNotNull( rowMatch );
        assertEquals( as.getName(),
                      rowMatch.values[0] );
        assertEquals( "thirdpartyuser",
                      rowMatch.values[2] ); //should be "from" that user name...

        //shouldn't be in thirdpartyusers inbox
        UserInbox ib = new UserInbox( repo2 );
        ib.loadIncoming();
        assertEquals( 0,
                      ib.loadIncoming().size() );
        assertEquals( 1,
                      ib.loadRecentEdited().size() );

        //ok lets create another user...
        RulesRepository repo3 = new RulesRepository( TestEnvironmentSessionHelper.getSessionFor( "fourthuser" ) );
        AssetItem as3 = repo3.loadDefaultPackage().loadAsset( "testLoadInbox" );
        as3.updateContent( "hey22" );
        as3.checkin( "here we go again 22!" );

        Thread.sleep( 250 );

        //so should be in thirdpartyuser inbox
        assertEquals( 1,
                      ib.loadIncoming().size() );

        //and also still in the original user...
        found = false;
        res = impl.loadInbox( ExplorerNodeConfig.INCOMING_ID );
        for ( TableDataRow row : res.data ) {
            if ( row.id.equals( as.getUUID() ) ) found = true;
        }
        assertTrue( found );

        //now lets open it with first user, and check that it disappears from the incoming...
        impl.loadRuleAsset( as.getUUID() );
        found = false;
        res = impl.loadInbox( ExplorerNodeConfig.INCOMING_ID );
        for ( TableDataRow row : res.data ) {
            if ( row.id.equals( as.getUUID() ) ) found = true;
        }
        assertFalse( found );

    }

    @Ignore
    @Test
    public void testCategory() throws Exception {
        ServiceImplementation serviceImplementation = getServiceImplementation();

        String[] originalCats = serviceImplementation.loadChildCategories( "/" );

        Boolean result = serviceImplementation.createCategory( "/",
                                                               "TopLevel1",
                                                               "a description" );
        assertTrue( result.booleanValue() );

        result = serviceImplementation.createCategory( "/",
                                                       "TopLevel2",
                                                       "a description" );
        assertTrue( result.booleanValue() );

        String[] cats = serviceImplementation.loadChildCategories( "/" );
        assertTrue( cats.length == originalCats.length + 2 );

        result = serviceImplementation.createCategory( "",
                                                       "Top3",
                                                       "description" );
        assertTrue( result.booleanValue() );

        result = serviceImplementation.createCategory( null,
                                                       "Top4",
                                                       "description" );
        assertTrue( result.booleanValue() );

    }

    @Test
    @Ignore
    public void testCleanHTML() {
        ServiceImplementation impl = new ServiceImplementation();
        assertEquals( "&lt;script&gt;",
                      impl.cleanHTML( "<script>" ) );
    }

    @Ignore
    @Test
    public void testDeleteUnversionedRule() throws Exception {
        //        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repository = new RulesRepository( TestEnvironmentSessionHelper.getSession( true ) );
        ServiceImplementation impl = new ServiceImplementation();
        impl.repository = repository;

        impl.getRulesRepository().loadDefaultPackage();
        impl.getRulesRepository().createPackage( "anotherPackage",
                                                 "woot" );

        CategoryItem cat = impl.getRulesRepository().loadCategory( "/" );
        cat.addCategory( "testDeleteUnversioned",
                         "yeah" );

        String uuid = impl.createNewRule( "test Delete Unversioned",
                                          "a description",
                                          "testDeleteUnversioned",
                                          "anotherPackage",
                                          "txt" );
        assertNotNull( uuid );
        assertFalse( "".equals( uuid ) );

        AssetItem localItem = impl.getRulesRepository().loadAssetByUUID( uuid );

        // String drl = "package org.drools.repository\n\ndialect 'mvel'\n\n" +
        // "rule Rule1 \n when \n AssetItem(description != null) \n then \n
        // System.out.println(\"yeah\");\nend";
        // RuleBase rb = RuleBaseLoader.getInstance().loadFromReader(new
        // StringReader(drl));
        // rb.newStatelessSession().execute(localItem);

        assertEquals( "test Delete Unversioned",
                      localItem.getName() );

        localItem.remove();
        impl.getRulesRepository().save();

        try {
            localItem = impl.getRulesRepository().loadAssetByUUID( uuid );
            fail();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void testAddRuleAndListPackages() throws Exception {
        // ServiceImpl impl = new ServiceImpl(new
        // RulesRepository(SessionHelper.getSession()));

        ServiceImplementation impl = getServiceImplementation();

        impl.repository.loadDefaultPackage();
        impl.repository.createPackage( "another",
                                       "woot" );

        CategoryItem cat = impl.repository.loadCategory( "/" );
        cat.addCategory( "testAddRule",
                         "yeah" );

        String result = impl.createNewRule( "test AddRule",
                                            "a description",
                                            "testAddRule",
                                            "another",
                                            "txt" );
        assertNotNull( result );
        assertFalse( "".equals( result ) );

        PackageConfigData[] packages = impl.listPackages();
        assertTrue( packages.length > 0 );

        boolean found = false;
        for ( int i = 0; i < packages.length; i++ ) {
            if ( packages[i].name.equals( "another" ) ) {
                found = true;
            }
        }

        assertTrue( found );

        assertFalse( packages[0].uuid == null );
        assertFalse( packages[0].uuid.equals( "" ) );

        // just for performance testing with scaling up numbers of rules
        // for (int i=1; i <= 1000; i++) {
        // impl.createNewRule( "somerule_" + i, "description",
        // "testAddRule", "another", "drl" );
        // }

        result = impl.createNewRule( "testDTSample",
                                     "a description",
                                     "testAddRule",
                                     "another",
                                     AssetFormats.DECISION_SPREADSHEET_XLS );
        AssetItem dtItem = impl.repository.loadAssetByUUID( result );
        assertNotNull( dtItem.getBinaryContentAsBytes() );
        assertTrue( dtItem.getBinaryContentAttachmentFileName().endsWith( ".xls" ) );
    }

    @Test
    @Ignore
    public void testAttemptDupeRule() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        CategoryItem cat = impl.repository.loadCategory( "/" );
        cat.addCategory( "testAttemptDupeRule",
                         "yeah" );

        impl.repository.createPackage( "dupes",
                                       "yeah" );

        impl.createNewRule( "testAttemptDupeRule",
                            "ya",
                            "testAttemptDupeRule",
                            "dupes",
                            "rule" );

        String uuid = impl.createNewRule( "testAttemptDupeRule",
                                          "ya",
                                          "testAttemptDupeRule",
                                          "dupes",
                                          "rule" );
        assertEquals( "DUPLICATE",
                      uuid );

    }

    @Test
    @Ignore
    public void testCreateNewRule() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        impl.repository.createPackage( "testCreateNewRule",
                                       "desc" );
        impl.createCategory( "",
                             "testCreateNewRule",
                             "this is a cat" );

        String uuid = impl.createNewRule( "testCreateNewRuleName",
                                          "an initial desc",
                                          "testCreateNewRule",
                                          "testCreateNewRule",
                                          AssetFormats.DSL_TEMPLATE_RULE );
        assertNotNull( uuid );
        assertFalse( "".equals( uuid ) );

        AssetItem dtItem = impl.repository.loadAssetByUUID( uuid );
        assertEquals( dtItem.getDescription(),
                      "an initial desc" );
    }

    @Test
    @Ignore
    public void testCreateLinkedAssetItem() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        @SuppressWarnings("unused")
        PackageItem testCreateNewRuleAsLinkPackage1 = impl.repository.createPackage( "testCreateNewRuleAsLinkPackage1",
                                                                                     "desc" );
        impl.createCategory( "",
                             "testCreateNewRuleAsLinkCat1",
                             "this is a cat" );
        impl.createCategory( "",
                             "testCreateNewRuleAsLinkCat2",
                             "this is a cat" );

        //Create the shared asset.
        String uuid = impl.createNewRule( "testCreateLinkedAssetItemRule",
                                          "an initial desc",
                                          "testCreateNewRuleAsLinkCat1",
                                          "globalArea",
                                          AssetFormats.DSL_TEMPLATE_RULE );
        assertNotNull( uuid );
        assertFalse( "".equals( uuid ) );

        AssetItem dtItem = impl.repository.loadAssetByUUID( uuid );
        assertEquals( dtItem.getDescription(),
                      "an initial desc" );

        //create an asset which is imported from global area. 
        String uuidLink = impl.createNewImportedRule( "testCreateLinkedAssetItemRule",
                                                      "testCreateNewRuleAsLinkPackage1" );
        assertNotNull( uuidLink );
        assertFalse( "".equals( uuidLink ) );
        assertTrue( uuidLink.equals( uuid ) );

        //now verify the linked asset.
        AssetItem itemLink = impl.repository.loadAssetByUUID( uuidLink );
        assertEquals( itemLink.getName(),
                      "testCreateLinkedAssetItemRule" );
        assertEquals( itemLink.getDescription(),
                      "an initial desc" );
        assertEquals( itemLink.getFormat(),
                      AssetFormats.DSL_TEMPLATE_RULE );
        assertEquals( itemLink.getPackage().getName(),
                      "globalArea" );

        assertEquals( itemLink.getPackageName(),
                      "globalArea" );

        assertTrue( itemLink.getCategories().size() == 1 );
        assertTrue( itemLink.getCategorySummary().contains( "testCreateNewRuleAsLinkCat1" ) );

        //now verify the original asset.
        AssetItem referredItem = impl.repository.loadAssetByUUID( uuid );
        assertEquals( referredItem.getName(),
                      "testCreateLinkedAssetItemRule" );
        assertEquals( referredItem.getDescription(),
                      "an initial desc" );
        assertEquals( referredItem.getFormat(),
                      AssetFormats.DSL_TEMPLATE_RULE );
        assertEquals( referredItem.getPackage().getName(),
                      "globalArea" );

        assertTrue( referredItem.getCategories().size() == 1 );
        assertTrue( referredItem.getCategorySummary().contains( "testCreateNewRuleAsLinkCat1" ) );

        //now verify AssetItemIterator works by calling search
        AssetItemIterator it = impl.repository.findAssetsByName( "testCreateLinkedAssetItemRule%",
                                                                 true );
        //NOTE, getSize() may return -1
        /*       assertEquals( 1,
                             it.getSize() );*/
        int size = 0;
        while ( it.hasNext() ) {
            size++;
            AssetItem ai = it.next();
            if ( ai.getUUID().equals( uuid ) ) {
                assertEquals( ai.getPackage().getName(),
                              "globalArea" );
                assertEquals( ai.getDescription(),
                              "an initial desc" );
            } else {
                fail( "unexptected asset found: " + ai.getPackage().getName() );
            }
        }
        assertEquals( 1,
                      size );
    }

    @Test
    @Ignore
    public void testLinkedAssetItemHistoryRelated() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        @SuppressWarnings("unused")
        PackageItem testCreateNewRuleAsLinkPackage1 = impl.repository.createPackage( "testLinkedAssetItemHistoryRelatedPack",
                                                                                     "desc" );
        impl.createCategory( "",
                             "testLinkedAssetItemHistoryRelatedCat",
                             "this is a cat" );

        //Create the shared asset in global area.
        String uuid = impl.createNewRule( "testLinkedAssetItemHistoryRelatedRule",
                                          "an initial desc",
                                          "testLinkedAssetItemHistoryRelatedCat",
                                          "globalArea",
                                          AssetFormats.DSL_TEMPLATE_RULE );

        //create an asset which refers to the shared assets.
        String uuidLink = impl.createNewImportedRule( "testLinkedAssetItemHistoryRelatedRule",
                                                      "testLinkedAssetItemHistoryRelatedPack" );
        assertTrue( uuidLink.equals( uuid ) );

        //create version 1.
        RuleAsset assetWrapper = impl.loadRuleAsset( uuidLink );
        assertEquals( assetWrapper.metaData.description,
                      "an initial desc" );
        assetWrapper.metaData.description = "version 1";
        String uuidLink1 = impl.checkinVersion( assetWrapper );

        //create version 2
        RuleAsset assetWrapper2 = impl.loadRuleAsset( uuidLink );
        assetWrapper2.metaData.description = "version 2";
        String uuidLink2 = impl.checkinVersion( assetWrapper2 );

        //create version head
        RuleAsset assetWrapper3 = impl.loadRuleAsset( uuidLink );
        assetWrapper3.metaData.description = "version head";
        @SuppressWarnings("unused")
        String uuidLink3 = impl.checkinVersion( assetWrapper3 );

        assertEquals( uuidLink,
                      uuidLink1 );
        assertEquals( uuidLink,
                      uuidLink2 );

        //verify the history info of LinkedAssetItem
        TableDataResult result = impl.loadAssetHistory( uuidLink );
        assertNotNull( result );
        TableDataRow[] rows = result.data;
        assertEquals( 3,
                      rows.length );
        assertFalse( rows[0].id.equals( uuidLink ) );
        assertFalse( rows[1].id.equals( uuidLink ) );
        assertFalse( rows[2].id.equals( uuidLink ) );

        RuleAsset version1 = impl.loadRuleAsset( rows[0].id );
        RuleAsset version2 = impl.loadRuleAsset( rows[1].id );
        RuleAsset version3 = impl.loadRuleAsset( rows[2].id );
        RuleAsset versionHead = impl.loadRuleAsset( uuidLink );
        assertFalse( version1.metaData.versionNumber == version2.metaData.versionNumber );
        assertFalse( version1.metaData.versionNumber == versionHead.metaData.versionNumber );
        assertEquals( version1.metaData.description,
                      "an initial desc" );
        assertEquals( version2.metaData.description,
                      "version 1" );
        assertEquals( version3.metaData.description,
                      "version 2" );
        assertEquals( versionHead.metaData.description,
                      "version head" );

        //verify the history info of the original AssetItem
        result = impl.loadAssetHistory( uuid );
        assertNotNull( result );
        rows = result.data;
        assertEquals( 3,
                      rows.length );
        assertFalse( rows[0].id.equals( uuid ) );
        assertFalse( rows[1].id.equals( uuid ) );

        version1 = impl.loadRuleAsset( rows[0].id );
        version2 = impl.loadRuleAsset( rows[1].id );
        versionHead = impl.loadRuleAsset( uuid );
        assertFalse( version1.metaData.versionNumber == version2.metaData.versionNumber );
        assertFalse( version1.metaData.versionNumber == versionHead.metaData.versionNumber );
        assertTrue( version1.metaData.description.equals( "an initial desc" ) );
        assertTrue( version2.metaData.description.equals( "version 1" ) );
        assertTrue( versionHead.metaData.description.equals( "version head" ) );

        //test restore
        impl.restoreVersion( version1.uuid,
                             versionHead.uuid,
                             "this was cause of a mistake" );

        RuleAsset newHead = impl.loadRuleAsset( uuid );

        assertEquals( "this was cause of a mistake",
                      newHead.metaData.checkinComment );
    }

    //path name contains Apostrophe is no longer a problem with jackrabbit 2.0

    @Test
    @Ignore
    public void testCreateNewRuleContainsApostrophe() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        impl.repository.createPackage( "testCreateNewRuleContainsApostrophe",
                                       "desc" );
        impl.createCategory( "",
                             "testCreateNewRuleContainsApostrophe",
                             "this is a cat" );

        String uuid = null;
        try {
            uuid = impl.createNewRule( "testCreateNewRuleContains' character",
                                       "an initial desc",
                                       "testCreateNewRuleContainsApostrophe",
                                       "testCreateNewRuleContainsApostrophe",
                                       AssetFormats.DSL_TEMPLATE_RULE );
            //fail( "did not get expected exception" );
        } catch ( SerializationException e ) {
            //assertTrue( e.getMessage().indexOf( "'testCreateNewRuleContains' character' is not a valid path. ''' not a valid name character" ) >= 0 );
        }

        RuleAsset assetWrapper = impl.loadRuleAsset( uuid );
        assertEquals( assetWrapper.metaData.description,
                      "an initial desc" );
        assertEquals( assetWrapper.metaData.name,
                      "testCreateNewRuleContains' character" );

    }

    @Test
    @Ignore
    public void testRuleTableLoad() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        TableConfig conf = impl.loadTableConfig( AssetItemGrid.RULE_LIST_TABLE_ID );
        assertNotNull( conf.headers );
        assertNotNull( conf.headerTypes );

        CategoryItem cat = impl.repository.loadCategory( "/" );
        cat.addCategory( "testRuleTableLoad",
                         "yeah" );

        impl.repository.createPackage( "testRuleTableLoad",
                                       "yeah" );
        impl.createNewRule( "testRuleTableLoad",
                            "ya",
                            "testRuleTableLoad",
                            "testRuleTableLoad",
                            "rule" );
        impl.createNewRule( "testRuleTableLoad2",
                            "ya",
                            "testRuleTableLoad",
                            "testRuleTableLoad",
                            "rule" );

        TableDataResult result = impl.loadRuleListForCategories( "testRuleTableLoad",
                                                                 0,
                                                                 -1,
                                                                 AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 2,
                      result.data.length );

        String key = result.data[0].id;
        assertFalse( key.startsWith( "testRule" ) );

        assertEquals( result.data[0].format,
                      "rule" );

        assertTrue( result.data[0].values[0].startsWith( "testRuleTableLoad" ) );
    }

    @Test
    @Ignore
    public void testDateFormatting() throws Exception {
        Calendar cal = Calendar.getInstance();
        TableDisplayHandler handler = new TableDisplayHandler( AssetItemGrid.RULE_LIST_TABLE_ID );
        String fmt = handler.formatDate( cal );
        assertNotNull( fmt );

        assertTrue( fmt.length() > 8 );
    }

    @Test
    @Ignore
    public void testLoadRuleAsset() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        impl.repository.createPackage( "testLoadRuleAsset",
                                       "desc" );
        impl.createCategory( "",
                             "testLoadRuleAsset",
                             "this is a cat" );

        impl.createNewRule( "testLoadRuleAsset",
                            "description",
                            "testLoadRuleAsset",
                            "testLoadRuleAsset",
                            AssetFormats.DRL );

        TableDataResult res = impl.loadRuleListForCategories( "testLoadRuleAsset",
                                                              0,
                                                              -1,
                                                              AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 1,
                      res.data.length );
        assertEquals( -1,
                      res.total );
        assertTrue( res.currentPosition > 0 );
        assertFalse( res.hasNext );

        TableDataRow row = res.data[0];
        String uuid = row.id;

        RuleAsset asset = impl.loadRuleAsset( uuid );
        assertNotNull( asset );

        assertEquals( uuid,
                      asset.uuid );

        assertEquals( "description",
                      asset.metaData.description );

        assertNotNull( asset.content );
        assertTrue( asset.content instanceof RuleContentText );
        assertEquals( "testLoadRuleAsset",
                      asset.metaData.name );
        assertEquals( "testLoadRuleAsset",
                      asset.metaData.title );
        assertEquals( "testLoadRuleAsset",
                      asset.metaData.packageName );
        assertEquals( AssetFormats.DRL,
                      asset.metaData.format );
        assertNotNull( asset.metaData.createdDate );

        assertEquals( 1,
                      asset.metaData.categories.length );
        assertEquals( "testLoadRuleAsset",
                      asset.metaData.categories[0] );

        AssetItem rule = impl.repository.loadPackage( "testLoadRuleAsset" ).loadAsset( "testLoadRuleAsset" );
        impl.repository.createState( "whee" );
        rule.updateState( "whee" );
        rule.checkin( "changed state" );
        asset = impl.loadRuleAsset( uuid );

        assertEquals( "whee",
                      asset.metaData.status );
        assertEquals( "changed state",
                      asset.metaData.checkinComment );

        uuid = impl.createNewRule( "testBRLFormatSugComp",
                                   "description",
                                   "testLoadRuleAsset",
                                   "testLoadRuleAsset",
                                   AssetFormats.BUSINESS_RULE );
        asset = impl.loadRuleAsset( uuid );
        assertTrue( asset.content instanceof RuleModel );

        uuid = impl.createNewRule( "testLoadRuleAssetBRL",
                                   "description",
                                   "testLoadRuleAsset",
                                   "testLoadRuleAsset",
                                   AssetFormats.DSL_TEMPLATE_RULE );
        asset = impl.loadRuleAsset( uuid );
        assertTrue( asset.content instanceof RuleContentText );
    }

    @Test
    @Ignore
    public void testListAssets() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        PackageItem pacakgeItem = impl.repository.createPackage( "testListAssetsPackage",
                                                                 "desc" );
        impl.createCategory( "",
                             "testListAssetsCat",
                             "this is a cat" );

        String uuid1 = impl.createNewRule( "testLoadArchivedAssets1",
                                           "description",
                                           "testListAssetsCat",
                                           "testListAssetsPackage",
                                           AssetFormats.BUSINESS_RULE );

        String uuid2 = impl.createNewRule( "testLoadArchivedAssets2",
                                           "description",
                                           "testListAssetsCat",
                                           "testListAssetsPackage",
                                           AssetFormats.BUSINESS_RULE );

        String uuid3 = impl.createNewRule( "testLoadArchivedAssets3",
                                           "description",
                                           "testListAssetsCat",
                                           "testListAssetsPackage",
                                           AssetFormats.BUSINESS_RULE );

        String uuid4 = impl.createNewRule( "testLoadArchivedAssets4",
                                           "description",
                                           "testListAssetsCat",
                                           "testListAssetsPackage",
                                           AssetFormats.BUSINESS_RULE );

        String uuid5 = impl.createNewRule( "testLoadArchivedAssets5",
                                           "description",
                                           "testListAssetsCat",
                                           "testListAssetsPackage",
                                           AssetFormats.BUSINESS_RULE );

        TableDataResult res = impl.listAssets( pacakgeItem.getUUID(),
                                               new String[]{AssetFormats.BUSINESS_RULE},
                                               0,
                                               2,
                                               AssetItemGrid.PACKAGEVIEW_LIST_TABLE_ID );

        assertEquals( 2,
                      res.data.length );
        assertTrue( 5 == res.total );
        assertTrue( res.currentPosition == 2 );
        assertTrue( res.hasNext );

        res = impl.listAssets( pacakgeItem.getUUID(),
                               new String[]{AssetFormats.BUSINESS_RULE},
                               2,
                               2,
                               AssetItemGrid.PACKAGEVIEW_LIST_TABLE_ID );
        assertEquals( 2,
                      res.data.length );
        assertTrue( 5 == res.total );
        assertTrue( res.currentPosition == 4 );
        assertTrue( res.hasNext );
    }

    @Test
    @Ignore
    public void testLoadArchivedAssets() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        impl.repository.createPackage( "testLoadArchivedAssetsPackage",
                                       "desc" );
        impl.createCategory( "",
                             "testLoadArchivedAssetsCat",
                             "this is a cat" );

        String uuid1 = impl.createNewRule( "testLoadArchivedAssets1",
                                           "description",
                                           "testLoadArchivedAssetsCat",
                                           "testLoadArchivedAssetsPackage",
                                           AssetFormats.DRL );
        impl.archiveAsset( uuid1 );

        String uuid2 = impl.createNewRule( "testLoadArchivedAssets2",
                                           "description",
                                           "testLoadArchivedAssetsCat",
                                           "testLoadArchivedAssetsPackage",
                                           AssetFormats.DRL );
        impl.archiveAsset( uuid2 );

        String uuid3 = impl.createNewRule( "testLoadArchivedAssets3",
                                           "description",
                                           "testLoadArchivedAssetsCat",
                                           "testLoadArchivedAssetsPackage",
                                           AssetFormats.DRL );
        impl.archiveAsset( uuid3 );

        String uuid4 = impl.createNewRule( "testLoadArchivedAssets4",
                                           "description",
                                           "testLoadArchivedAssetsCat",
                                           "testLoadArchivedAssetsPackage",
                                           AssetFormats.DRL );
        impl.archiveAsset( uuid4 );

        String uuid5 = impl.createNewRule( "testLoadArchivedAssets5",
                                           "description",
                                           "testLoadArchivedAssetsCat",
                                           "testLoadArchivedAssetsPackage",
                                           AssetFormats.DRL );
        impl.archiveAsset( uuid5 );

        //We do not know how many archived assets we have in the test repo,
        //but definitely more than 5 (as we just created 5)
        TableDataResult res = impl.loadArchivedAssets( 0,
                                                       2 );

        assertEquals( 2,
                      res.data.length );
        //may return -1 as per JCR2.0 when precise count is not available due to performance reasons. 
        //assertTrue(-1 != res.total);
        assertTrue( res.currentPosition == 2 );
        assertTrue( res.hasNext );

        res = impl.loadArchivedAssets( 2,
                                       2 );

        assertEquals( 2,
                      res.data.length );
        //assertTrue(-1 != res.total);
        assertEquals( res.currentPosition,
                      4 );
        assertTrue( res.hasNext );
    }

    @Test
    @Ignore
    public void testTrackRecentOpenedChanged() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        UserInbox ib = new UserInbox( impl.repository );
        ib.clearAll();
        impl.repository.createPackage( "testTrackRecentOpenedChanged",
                                       "desc" );
        impl.createCategory( "",
                             "testTrackRecentOpenedChanged",
                             "this is a cat" );

        String id = impl.createNewRule( "myrule",
                                        "desc",
                                        "testTrackRecentOpenedChanged",
                                        "testTrackRecentOpenedChanged",
                                        "drl" );
        RuleAsset ass = impl.loadRuleAsset( id );

        impl.checkinVersion( ass );

        List<InboxEntry> es = ib.loadRecentEdited();
        assertEquals( 1,
                      es.size() );
        assertEquals( ass.uuid,
                      es.get( 0 ).assetUUID );
        assertEquals( ass.metaData.name,
                      es.get( 0 ).note );

        ib.clearAll();

        impl.loadRuleAsset( ass.uuid );
        es = ib.loadRecentEdited();
        assertEquals( 0,
                      es.size() );

        //now check they have it in their opened list...
        es = ib.loadRecentOpened();
        assertEquals( 1,
                      es.size() );
        assertEquals( ass.uuid,
                      es.get( 0 ).assetUUID );
        assertEquals( ass.metaData.name,
                      es.get( 0 ).note );

        assertEquals( 0,
                      ib.loadRecentEdited().size() );
    }

    @Test
    @Ignore
    public void testLoadAssetHistoryAndRestore() throws Exception {

        ServiceImplementation impl = getServiceImplementation();
        long startTime = System.currentTimeMillis();
        impl.repository.createPackage( "testLoadAssetHistory",
                                       "desc" );
        long nowTime1 = System.currentTimeMillis();
        System.out.println( "CreatePackage: " + (nowTime1 - startTime) );
        impl.createCategory( "",
                             "testLoadAssetHistory",
                             "this is a cat" );

        long nowTime2 = System.currentTimeMillis();
        System.out.println( "CreateCategory: " + (nowTime2 - nowTime1) );
        String uuid = impl.createNewRule( "testLoadAssetHistory",
                                          "description",
                                          "testLoadAssetHistory",
                                          "testLoadAssetHistory",
                                          AssetFormats.DRL );
        long nowTime3 = System.currentTimeMillis();
        System.out.println( "CreateNewRule: " + (nowTime3 - nowTime2) );

        RuleAsset asset = impl.loadRuleAsset( uuid );
        impl.checkinVersion( asset ); // 1
        long nowTime4 = System.currentTimeMillis();
        System.out.println( "Checkin 1: " + (nowTime4 - nowTime3) );
        asset = impl.loadRuleAsset( uuid );
        long nowTime5 = System.currentTimeMillis();
        System.out.println( "load ruleasset: " + (nowTime5 - nowTime4) );
        impl.checkinVersion( asset ); // 2
        long nowTime6 = System.currentTimeMillis();
        System.out.println( "Checkin 2: " + (nowTime6 - nowTime5) );
        asset = impl.loadRuleAsset( uuid );
        impl.checkinVersion( asset ); // HEAD

        TableDataResult result = impl.loadAssetHistory( uuid );
        assertNotNull( result );
        TableDataRow[] rows = result.data;
        assertEquals( 2,
                      rows.length );
        assertFalse( rows[0].id.equals( uuid ) );
        assertFalse( rows[1].id.equals( uuid ) );

        RuleAsset old = impl.loadRuleAsset( rows[0].id );
        RuleAsset newer = impl.loadRuleAsset( rows[1].id );
        assertFalse( old.metaData.versionNumber == newer.metaData.versionNumber );

        RuleAsset head = impl.loadRuleAsset( uuid );

        long oldVersion = old.metaData.versionNumber;
        assertFalse( oldVersion == head.metaData.versionNumber );

        impl.restoreVersion( old.uuid,
                             head.uuid,
                             "this was cause of a mistake" );

        RuleAsset newHead = impl.loadRuleAsset( uuid );

        assertEquals( "this was cause of a mistake",
                      newHead.metaData.checkinComment );

    }

    @Test
    @Ignore
    public void testCheckin() throws Exception {
        ServiceImplementation serv = getServiceImplementation();

        UserInbox ib = new UserInbox( serv.repository );
        List<InboxEntry> inbox = ib.loadRecentEdited();

        serv.listPackages();

        serv.createCategory( "/",
                             "testCheckinCategory",
                             "this is a description" );
        serv.createCategory( "/",
                             "testCheckinCategory2",
                             "this is a description" );
        serv.createCategory( "testCheckinCategory",
                             "deeper",
                             "description" );

        String uuid = serv.createNewRule( "testChecking",
                                          "this is a description",
                                          "testCheckinCategory",
                                          RulesRepository.DEFAULT_PACKAGE,
                                          AssetFormats.DRL );

        RuleAsset asset = serv.loadRuleAsset( uuid );

        assertNotNull( asset.metaData.lastModifiedDate );

        asset.metaData.coverage = "boo";
        asset.content = new RuleContentText();
        ((RuleContentText) asset.content).content = "yeah !";

        Date start = new Date();
        Thread.sleep( 100 );

        String uuid2 = serv.checkinVersion( asset );
        assertEquals( uuid,
                      uuid2 );

        assertTrue( ib.loadRecentEdited().size() > inbox.size() );

        RuleAsset asset2 = serv.loadRuleAsset( uuid );
        assertNotNull( asset2.metaData.lastModifiedDate );
        assertTrue( asset2.metaData.lastModifiedDate.after( start ) );

        assertEquals( "boo",
                      asset2.metaData.coverage );
        assertEquals( 1,
                      asset2.metaData.versionNumber );

        assertEquals( "yeah !",
                      ((RuleContentText) asset2.content).content );

        asset2.metaData.coverage = "ya";
        asset2.metaData.checkinComment = "checked in";

        String cat = asset2.metaData.categories[0];
        asset2.metaData.categories = new String[3];
        asset2.metaData.categories[0] = cat;
        asset2.metaData.categories[1] = "testCheckinCategory2";
        asset2.metaData.categories[2] = "testCheckinCategory/deeper";

        serv.checkinVersion( asset2 );

        asset2 = serv.loadRuleAsset( uuid );
        assertEquals( "ya",
                      asset2.metaData.coverage );
        assertEquals( 2,
                      asset2.metaData.versionNumber );
        assertEquals( "checked in",
                      asset2.metaData.checkinComment );
        assertEquals( 3,
                      asset2.metaData.categories.length );
        assertEquals( "testCheckinCategory",
                      asset2.metaData.categories[0] );
        assertEquals( "testCheckinCategory2",
                      asset2.metaData.categories[1] );
        assertEquals( "testCheckinCategory/deeper",
                      asset2.metaData.categories[2] );

        // now lets try a concurrent edit of an asset.
        // asset3 will be loaded and edited, and then asset2 will try to
        // clobber, it, which should fail.
        // as it is optimistically locked.
        RuleAsset asset3 = serv.loadRuleAsset( asset2.uuid );
        asset3.metaData.subject = "new sub";
        serv.checkinVersion( asset3 );

        asset3 = serv.loadRuleAsset( asset2.uuid );
        assertFalse( asset3.metaData.versionNumber == asset2.metaData.versionNumber );

        String result = serv.checkinVersion( asset2 );
        assertTrue( result.startsWith( "ERR" ) );
        System.err.println( result.substring( 5 ) );

    }

    @Test
    @Ignore
    public void testArchivePackage() throws Exception {
        ServiceImplementation impl = getServiceImplementation();

        PackageConfigData[] pkgs = impl.listPackages();

        PackageConfigData[] arch = impl.listArchivedPackages();

        @SuppressWarnings("unused")
        String uuid = impl.createPackage( "testCreateArchivedPackage",
                                          "this is a new package" );

        PackageItem item = impl.repository.loadPackage( "testCreateArchivedPackage" );
        TableDataResult td = impl.loadArchivedAssets( 0,
                                                      1000 );

        item.archiveItem( true );

        TableDataResult td2 = impl.loadArchivedAssets( 0,
                                                       1000 );
        assertEquals( td2.data.length,
                      td.data.length );

        PackageConfigData[] arch2 = impl.listArchivedPackages();
        assertEquals( arch2.length,
                      arch.length + 1 );

        assertEquals( pkgs.length,
                      impl.listPackages().length );

        item.archiveItem( false );
        arch2 = impl.listArchivedPackages();
        assertEquals( arch2.length,
                      arch.length );
    }

    @Test
    @Ignore
    public void testCreatePackage() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        PackageConfigData[] pkgs = impl.listPackages();
        String uuid = impl.createPackage( "testCreatePackage",
                                          "this is a new package" );
        assertNotNull( uuid );

        PackageItem item = impl.repository.loadPackage( "testCreatePackage" );
        assertNotNull( item );
        assertEquals( "this is a new package",
                      item.getDescription() );

        assertEquals( pkgs.length + 1,
                      impl.listPackages().length );

        PackageConfigData conf = impl.loadPackageConfig( uuid );
        assertEquals( "this is a new package",
                      conf.description );
        assertNotNull( conf.lastModified );

        pkgs = impl.listPackages();

        impl.copyPackage( "testCreatePackage",
                          "testCreatePackage_COPY" );

        assertEquals( pkgs.length + 1,
                      impl.listPackages().length );
        try {
            impl.copyPackage( "testCreatePackage",
                              "testCreatePackage_COPY" );
        } catch ( RulesRepositoryException e ) {
            assertNotNull( e.getMessage() );
        }
    }

    @Test
    @Ignore
    public void testLoadPackageConfig() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        PackageItem it = impl.repository.loadDefaultPackage();
        String uuid = it.getUUID();
        it.updateCoverage( "xyz" );
        it.updateExternalURI( "ext" );
        ServiceImplementation.updateDroolsHeader( "header",
                                                  it );
        impl.repository.save();

        PackageConfigData data = impl.loadPackageConfig( uuid );
        assertNotNull( data );

        assertEquals( RulesRepository.DEFAULT_PACKAGE,
                      data.name );
        assertEquals( "header",
                      data.header );
        assertEquals( "ext",
                      data.externalURI );

        assertNotNull( data.uuid );
        assertFalse( data.isSnapshot );

        assertNotNull( data.dateCreated );
        Date original = data.lastModified;

        Thread.sleep( 100 );

        impl.createPackageSnapshot( RulesRepository.DEFAULT_PACKAGE,
                                    "TEST SNAP 2.0",
                                    false,
                                    "ya" );
        PackageItem loaded = impl.repository.loadPackageSnapshot( RulesRepository.DEFAULT_PACKAGE,
                                                                  "TEST SNAP 2.0" );

        data = impl.loadPackageConfig( loaded.getUUID() );
        assertTrue( data.isSnapshot );
        assertEquals( "TEST SNAP 2.0",
                      data.snapshotName );
        assertFalse( original.equals( data.lastModified ) );
        assertEquals( "ya",
                      data.checkinComment );
    }

    @Test
    @Ignore
    public void testArchiveAndUnarchivePackageAndHeader() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        String uuid = impl.createPackage( "testArchiveAndUnarchivePackageAndHeader",
                                          "a desc" );
        PackageConfigData data = impl.loadPackageConfig( uuid );
        PackageItem it = impl.repository.loadPackageByUUID( uuid );
        data.archived = true;

        AssetItem rule1 = it.addAsset( "rule_1",
                                       "" );
        rule1.updateFormat( AssetFormats.DRL );
        rule1.updateContent( "rule 'rule1' \n when \np : Person() \n then \np.setAge(42); \n end" );
        rule1.archiveItem( true );
        rule1.checkin( "" );
        impl.repository.save();

        impl.savePackage( data );
        data = impl.loadPackageConfig( uuid );
        it = impl.repository.loadPackage( data.name );
        assertTrue( data.archived );
        assertTrue( it.loadAsset( "drools" ).isArchived() );
        assertTrue( it.loadAsset( "rule_1" ).isArchived() );

        data.archived = false;

        impl.savePackage( data );
        data = impl.loadPackageConfig( uuid );
        it = impl.repository.loadPackage( data.name );
        assertFalse( data.archived );
        assertFalse( it.loadAsset( "drools" ).isArchived() );
        assertTrue( it.loadAsset( "rule_1" ).isArchived() );

        data.archived = true;

        impl.savePackage( data );
        data = impl.loadPackageConfig( uuid );
        it = impl.repository.loadPackage( data.name );
        assertTrue( data.archived );
        assertTrue( it.loadAsset( "drools" ).isArchived() );
        assertTrue( it.loadAsset( "rule_1" ).isArchived() );

    }

    @Test
    @Ignore
    public void testPackageConfSave() throws Exception {
        RepositoryService impl = getServiceImplementation();
        String uuid = impl.createPackage( "testPackageConfSave",
                                          "a desc" );
        PackageConfigData data = impl.loadPackageConfig( uuid );

        data.description = "new desc";
        data.header = "wa";
        data.externalURI = "new URI";

        ValidatedResponse res = impl.savePackage( data );
        assertNotNull( res );
        assertTrue( res.hasErrors );
        assertNotNull( res.errorMessage );

        data = impl.loadPackageConfig( uuid );
        assertEquals( "new desc",
                      data.description );
        assertEquals( "wa",
                      data.header );
        assertEquals( "new URI",
                      data.externalURI );

        data.header = "";
        res = impl.savePackage( data );
        if ( res.hasErrors ) {
            System.out.println( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
            System.out.println( res.errorMessage );
            System.out.println( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );

        }

        assertFalse( res.hasErrors );
    }

    @Test
    @Ignore
    public void testListByFormat() throws Exception {
        RepositoryService impl = getServiceImplementation();
        String cat = "testListByFormat";
        impl.createCategory( "/",
                             cat,
                             "ya" );
        String pkgUUID = impl.createPackage( "testListByFormat",
                                             "used for listing by format." );

        String uuid = impl.createNewRule( "testListByFormat",
                                          "x",
                                          cat,
                                          "testListByFormat",
                                          "testListByFormat" );
        @SuppressWarnings("unused")
        String uuid2 = impl.createNewRule( "testListByFormat2",
                                           "x",
                                           cat,
                                           "testListByFormat",
                                           "testListByFormat" );
        String uuid3 = impl.createNewRule( "testListByFormat3",
                                           "x",
                                           cat,
                                           "testListByFormat",
                                           "testListByFormat" );
        @SuppressWarnings("unused")
        String uuid4 = impl.createNewRule( "testListByFormat4",
                                           "x",
                                           cat,
                                           "testListByFormat",
                                           "testListByFormat" );

        TableDataResult res = impl.listAssets( pkgUUID,
                                               arr( "testListByFormat" ),
                                               0,
                                               -1,
                                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 4,
                      res.data.length );
        assertEquals( uuid,
                      res.data[0].id );
        assertEquals( "testListByFormat",
                      res.data[0].values[0] );

        res = impl.listAssets( pkgUUID,
                               arr( "testListByFormat" ),
                               0,
                               4,
                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 4,
                      res.data.length );

        res = impl.listAssets( pkgUUID,
                               arr( "testListByFormat" ),
                               0,
                               2,
                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 2,
                      res.data.length );
        assertEquals( uuid,
                      res.data[0].id );
        assertEquals( 4,
                      res.total );
        assertTrue( res.hasNext );

        res = impl.listAssets( pkgUUID,
                               arr( "testListByFormat" ),
                               2,
                               2,
                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 2,
                      res.data.length );
        assertEquals( uuid3,
                      res.data[0].id );
        assertEquals( 4,
                      res.total );
        assertFalse( res.hasNext );

        uuid = impl.createNewRule( "testListByFormat5",
                                   "x",
                                   cat,
                                   "testListByFormat",
                                   "otherFormat" );

        res = impl.listAssets( pkgUUID,
                               arr( "otherFormat" ),
                               0,
                               40,
                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 1,
                      res.data.length );
        assertEquals( uuid,
                      res.data[0].id );

        res = impl.listAssets( pkgUUID,
                               new String[]{"otherFormat", "testListByFormat"},
                               0,
                               40,
                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 5,
                      res.data.length );

        TableDataResult result = impl.quickFindAsset( "testListByForma",
                                                      false,
                                                      0,
                                                      5 );
        assertEquals( 5,
                      result.data.length );

        assertNotNull( result.data[0].id );
        assertTrue( result.data[0].values[0].startsWith( "testListByFormat" ) );

        result = impl.quickFindAsset( "testListByForma",
                                      false,
                                      0,
                                      4 );
        assertEquals( 4,
                      result.data.length );
    }

    @Test
    @Ignore
    public void testListUnregisteredAssetFormats() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        PackageItem pkg = impl.repository.createPackage( "testListUnregisteredAssetFormats",
                                                         "" );
        AssetItem as = pkg.addAsset( "whee",
                                     "" );
        as.updateFormat( AssetFormats.DRL );
        as.checkin( "" );

        as = pkg.addAsset( "whee2",
                           "" );
        as.updateFormat( "something_silly" );
        as.checkin( "" );

        TableDataResult res = impl.listAssets( pkg.getUUID(),
                                               new String[0],
                                               0,
                                               40,
                                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 1,
                      res.data.length );
    }

    @Test
    @Ignore
    public void testQuickFind() throws Exception {
        RepositoryService impl = getServiceImplementation();
        String cat = "testQuickFind";
        impl.createCategory( "/",
                             cat,
                             "qkfnd" );
        impl.createPackage( "testQuickFind",
                            "for testing quick find." );
        String uuid = impl.createNewRule( "testQuickFindmyRule1",
                                          "desc",
                                          cat,
                                          "testQuickFind",
                                          AssetFormats.DRL );
        TableDataResult res = impl.quickFindAsset( "testQuickFindmyRule",
                                                   false,
                                                   0,
                                                   20 );
        assertEquals( 1,
                      res.data.length );

        impl.createNewRule( "testQuickFindmyRule2",
                            "desc",
                            cat,
                            "testQuickFind",
                            AssetFormats.DRL );
        res = impl.quickFindAsset( "testQuickFindmyRule",
                                   false,
                                   0,
                                   20 );
        assertEquals( 2,
                      res.data.length );

        impl.copyAsset( uuid,
                        "testQuickFind",
                        "testQuickFindmyRule3" );
        res = impl.quickFindAsset( "testQuickFindmyRule",
                                   false,
                                   0,
                                   20 );
        assertEquals( 3,
                      res.data.length );

        res = impl.quickFindAsset( "testQuickFindm*Rule",
                                   false,
                                   0,
                                   20 );
        assertEquals( 3,
                      res.data.length );

    }

    @Test
    @Ignore
    public void testSearchText() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        String cat = "testTextSearch";
        impl.createCategory( "/",
                             cat,
                             "qkfnd" );
        impl.createPackage( "testTextSearch",
                            "for testing search." );
        @SuppressWarnings("unused")
        String uuid = impl.createNewRule( "testTextRule1",
                                          "desc",
                                          cat,
                                          "testTextSearch",
                                          AssetFormats.DRL );
        TableDataResult res = impl.queryFullText( "testTextRule1",
                                                  false,
                                                  0,
                                                  -1 );
        assertEquals( 1,
                      res.data.length );
    }

    @Test
    @Ignore
    public void testSearchMetaData() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        PackageItem pkg = impl.repository.createPackage( "testMetaDataSearch",
                                                         "" );

        AssetItem asset = pkg.addAsset( "testMetaDataSearchAsset",
                                        "" );
        asset.updateSubject( "testMetaDataSearch" );
        asset.updateExternalSource( "numberwang" );
        asset.checkin( "" );

        MetaDataQuery[] qr = new MetaDataQuery[2];
        qr[0] = new MetaDataQuery();
        qr[0].attribute = AssetItem.SUBJECT_PROPERTY_NAME;
        qr[0].valueList = "wang, testMetaDataSearch";
        qr[1] = new MetaDataQuery();
        qr[1].attribute = AssetItem.SOURCE_PROPERTY_NAME;
        qr[1].valueList = "numberwan*";
        TableDataResult res = impl.queryMetaData( qr,
                                                  DateUtils.parseDate( "10-Jul-1974",
                                                                       new DateFormatsImpl() ),
                                                  null,
                                                  null,
                                                  null,
                                                  false,
                                                  0,
                                                  -1 );
        assertEquals( 1,
                      res.data.length );

    }

    public String[] arr(String s) {
        return new String[]{s};
    }

    @Test
    @Ignore
    public void testStatus() throws Exception {
        RepositoryService impl = getServiceImplementation();
        String uuid = impl.createState( "testStatus1" );
        assertNotNull( uuid );

        String[] states = impl.listStates();
        assertTrue( states.length > 0 );

        impl.createState( "testStatus2" );
        String[] states2 = impl.listStates();
        assertEquals( states.length + 1,
                      states2.length );

        int match = 0;
        for ( int i = 0; i < states2.length; i++ ) {
            if ( states2[i].equals( "testStatus2" ) ) {
                match++;
            } else if ( states2[i].equals( "testStatus1" ) ) {
                match++;
            }
        }

        assertEquals( 2,
                      match );

        String packagUUID = impl.createPackage( "testStatus",
                                                "description" );
        String ruleUUID = impl.createNewRule( "testStatus",
                                              "desc",
                                              null,
                                              "testStatus",
                                              AssetFormats.DRL );
        String ruleUUID2 = impl.createNewRule( "testStatus2",
                                               "desc",
                                               null,
                                               "testStatus",
                                               AssetFormats.DRL );
        impl.createState( "testState" );

        RuleAsset asset = impl.loadRuleAsset( ruleUUID );
        assertEquals( StateItem.DRAFT_STATE_NAME,
                      asset.metaData.status );
        impl.changeState( ruleUUID,
                          "testState",
                          false );
        asset = impl.loadRuleAsset( ruleUUID );
        assertEquals( "testState",
                      asset.metaData.status );
        asset = impl.loadRuleAsset( ruleUUID2 );
        assertEquals( StateItem.DRAFT_STATE_NAME,
                      asset.metaData.status );

        impl.createState( "testState2" );
        impl.changeState( packagUUID,
                          "testState2",
                          true );

        PackageConfigData pkg = impl.loadPackageConfig( packagUUID );
        assertEquals( "testState2",
                      pkg.state );

        asset = impl.loadRuleAsset( ruleUUID2 );
        assertEquals( "testState2",
                      asset.metaData.status );

        impl.checkinVersion( asset );
        asset = impl.loadRuleAsset( asset.uuid );
        assertEquals( "testState2",
                      asset.metaData.status );

    }

    @Test
    @Ignore
    public void testMovePackage() throws Exception {
        RepositoryService impl = getServiceImplementation();
        String[] cats = impl.loadChildCategories( "/" );
        if ( cats.length == 0 ) {
            impl.createCategory( "/",
                                 "la",
                                 "d" );
        }
        String sourcePkgId = impl.createPackage( "sourcePackage",
                                                 "description" );
        String destPkgId = impl.createPackage( "targetPackage",
                                               "description" );

        String cat = impl.loadChildCategories( "/" )[0];

        String uuid = impl.createNewRule( "testMovePackage",
                                          "desc",
                                          cat,
                                          "sourcePackage",
                                          AssetFormats.DRL );

        TableDataResult res = impl.listAssets( destPkgId,
                                               new String[]{"drl"},
                                               0,
                                               2,
                                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 0,
                      res.data.length );

        impl.changeAssetPackage( uuid,
                                 "targetPackage",
                                 "yeah" );
        res = impl.listAssets( destPkgId,
                               new String[]{"drl"},
                               0,
                               2,
                               AssetItemGrid.RULE_LIST_TABLE_ID );

        assertEquals( 1,
                      res.data.length );

        res = impl.listAssets( sourcePkgId,
                               new String[]{"drl"},
                               0,
                               2,
                               AssetItemGrid.RULE_LIST_TABLE_ID );

        assertEquals( 0,
                      res.data.length );

    }

    @Test
    @Ignore
    public void testCopyAsset() throws Exception {
        RepositoryService impl = getServiceImplementation();
        impl.createCategory( "/",
                             "templates",
                             "ya" );
        String uuid = impl.createNewRule( "testCopyAsset",
                                          "",
                                          "templates",
                                          RulesRepository.DEFAULT_PACKAGE,
                                          AssetFormats.DRL );
        String uuid2 = impl.copyAsset( uuid,
                                       RulesRepository.DEFAULT_PACKAGE,
                                       "testCopyAsset2" );
        assertNotSame( uuid,
                       uuid2 );

        RuleAsset asset = impl.loadRuleAsset( uuid2 );
        assertNotNull( asset );
        assertEquals( RulesRepository.DEFAULT_PACKAGE,
                      asset.metaData.packageName );
        assertEquals( "testCopyAsset2",
                      asset.metaData.name );
    }

    @Test
    @Ignore
    public void testSnapshot() throws Exception {
        RepositoryService impl = getServiceImplementation();
        impl.createCategory( "/",
                             "snapshotTesting",
                             "y" );
        impl.createPackage( "testSnapshot",
                            "d" );
        @SuppressWarnings("unused")
        String uuid = impl.createNewRule( "testSnapshotRule",
                                          "",
                                          "snapshotTesting",
                                          "testSnapshot",
                                          AssetFormats.DRL );

        impl.createPackageSnapshot( "testSnapshot",
                                    "X",
                                    false,
                                    "ya" );
        SnapshotInfo[] snaps = impl.listSnapshots( "testSnapshot" );
        assertEquals( 1,
                      snaps.length );
        assertEquals( "X",
                      snaps[0].name );
        assertEquals( "ya",
                      snaps[0].comment );
        assertNotNull( snaps[0].uuid );
        PackageConfigData confSnap = impl.loadPackageConfig( snaps[0].uuid );
        assertEquals( "testSnapshot",
                      confSnap.name );

        impl.createPackageSnapshot( "testSnapshot",
                                    "Y",
                                    false,
                                    "we" );
        assertEquals( 2,
                      impl.listSnapshots( "testSnapshot" ).length );
        impl.createPackageSnapshot( "testSnapshot",
                                    "X",
                                    true,
                                    "we" );
        assertEquals( 2,
                      impl.listSnapshots( "testSnapshot" ).length );

        impl.copyOrRemoveSnapshot( "testSnapshot",
                                   "X",
                                   false,
                                   "Q" );
        assertEquals( 3,
                      impl.listSnapshots( "testSnapshot" ).length );

        try {
            impl.copyOrRemoveSnapshot( "testSnapshot",
                                       "X",
                                       false,
                                       "" );
            fail( "should not be able to copy snapshot to empty detination" );
        } catch ( SerializationException e ) {
            assertNotNull( e.getMessage() );
        }

        impl.copyOrRemoveSnapshot( "testSnapshot",
                                   "X",
                                   true,
                                   null );
        assertEquals( 2,
                      impl.listSnapshots( "testSnapshot" ).length );

    }

    @Test
    @Ignore
    public void testSnapshotRebuild() throws Exception {

        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // get rid of other snapshot crap
        Iterator< ? > pkit = repo.listPackages();
        while ( pkit.hasNext() ) {
            PackageItem pkg = (PackageItem) pkit.next();
            String[] snaps = repo.listPackageSnapshots( pkg.getName() );
            for ( String snapName : snaps ) {
                repo.removePackageSnapshot( pkg.getName(),
                                            snapName );
            }
        }

        final PackageItem pkg = repo.createPackage( "testSnapshotRebuild",
                                                    "" );
        ServiceImplementation.updateDroolsHeader( "import java.util.List",
                                                  pkg );
        repo.save();

        AssetItem item = pkg.addAsset( "anAsset",
                                       "" );
        item.updateFormat( AssetFormats.DRL );
        item.updateContent( " rule abc \n when \n then \n System.out.println(42); \n end" );
        item.checkin( "" );

        BuilderResult res = impl.buildPackage( pkg.getUUID(),
                                               true );
        assertNull( res );

        impl.createPackageSnapshot( "testSnapshotRebuild",
                                    "SNAP",
                                    false,
                                    "" );

        PackageItem snap = repo.loadPackageSnapshot( "testSnapshotRebuild",
                                                     "SNAP" );
        long snapTime = snap.getLastModified().getTimeInMillis();

        Thread.sleep( 100 );

        impl.rebuildSnapshots();

        PackageItem snap_ = repo.loadPackageSnapshot( "testSnapshotRebuild",
                                                      "SNAP" );
        long newTime = snap_.getLastModified().getTimeInMillis();

        assertTrue( newTime > snapTime );

        item.updateContent( "garbage" );
        item.checkin( "" );

        impl.createPackageSnapshot( "testSnapshotRebuild",
                                    "SNAP2",
                                    false,
                                    "" );

        try {
            impl.rebuildSnapshots();
        } catch ( DetailedSerializationException e ) {
            assertNotNull( e.getMessage() );
            assertNotNull( e.getLongDescription() );
        }

    }

    @Test
    @Ignore
    public void testPackageRebuild() throws Exception {

        ServiceImplementation impl = getServiceImplementation();

        RulesRepository repo = impl.repository;

        final PackageItem pkg = repo.createPackage( "testPackageRebuild",
                                                    "" );
        ServiceImplementation.updateDroolsHeader( "import java.util.List",
                                                  pkg );
        repo.save();

        AssetItem item = pkg.addAsset( "anAsset",
                                       "" );
        item.updateFormat( AssetFormats.DRL );
        item.updateContent( " rule abc \n when \n then \n System.out.println(42); \n end" );
        item.checkin( "" );

        assertNull( pkg.getCompiledPackageBytes() );

        long last = pkg.getLastModified().getTimeInMillis();
        Thread.sleep( 100 );
        try {
            impl.rebuildPackages();
        } catch ( DetailedSerializationException e ) {
            assertNotNull( e.getMessage() );
            assertNotNull( e.getLongDescription() );
        }

        assertFalse( pkg.getLastModified().getTimeInMillis() == last );
        assertNotNull( pkg.getCompiledPackageBytes() );

    }

    @Test
    @Ignore
    public void testRemoveCategory() throws Exception {

        RepositoryService impl = getServiceImplementation();
        String[] children = impl.loadChildCategories( "/" );
        impl.createCategory( "/",
                             "testRemoveCategory",
                             "foo" );

        impl.removeCategory( "testRemoveCategory" );
        String[] _children = impl.loadChildCategories( "/" );
        assertEquals( children.length,
                      _children.length );

    }

    @Test
    @Ignore
    public void testRemoveAsset() throws Exception {
        RepositoryService impl = getServiceImplementation();
        String cat = "testRemoveAsset";
        impl.createCategory( "/",
                             cat,
                             "ya" );
        String pkgUUID = impl.createPackage( "testRemoveAsset",
                                             "" );
        @SuppressWarnings("unused")
        String uuid = impl.createNewRule( "testRemoveAsset",
                                          "x",
                                          cat,
                                          "testRemoveAsset",
                                          "testRemoveAsset" );
        @SuppressWarnings("unused")
        String uuid2 = impl.createNewRule( "testRemoveAsset2",
                                           "x",
                                           cat,
                                           "testRemoveAsset",
                                           "testRemoveAsset" );
        @SuppressWarnings("unused")
        String uuid3 = impl.createNewRule( "testRemoveAsset3",
                                           "x",
                                           cat,
                                           "testRemoveAsset",
                                           "testRemoveAsset" );
        String uuid4 = impl.createNewRule( "testRemoveAsset4",
                                           "x",
                                           cat,
                                           "testRemoveAsset",
                                           "testRemoveAsset" );

        TableDataResult res = impl.listAssets( pkgUUID,
                                               arr( "testRemoveAsset" ),
                                               0,
                                               -1,
                                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 4,
                      res.data.length );

        impl.removeAsset( uuid4 );

        res = impl.listAssets( pkgUUID,
                               arr( "testRemoveAsset" ),
                               0,
                               -1,
                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 3,
                      res.data.length );
    }

    @Test
    @Ignore
    public void testRemovePackage() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        int n = impl.listPackages().length;
        PackageItem p = impl.repository.createPackage( "testRemovePackage",
                                                       "" );
        assertNotNull( impl.loadPackageConfig( p.getUUID() ) );

        impl.removePackage( p.getUUID() );
        assertEquals( n,
                      impl.listPackages().length );
    }

    @Test
    @Ignore
    public void testImportPackage() throws Exception {
        ServiceImplementation impl = getServiceImplementation();

        try {
            PackageItem item = impl.repository.loadPackage( "testExportPackage" );
            fail();
            assertNull( item );
        } catch ( Exception e ) {
            // expected
        }

        //impl.createCategory( "/", "testExportPackageCat1", "desc" );
        //impl.createCategory( "/", "testExportPackageCat2", "desc" );

        File file = new File( "d:\\testExportPackage.xml" );

        FileInputStream fis = new FileInputStream( file );
        byte[] byteArray = new byte[fis.available()];
        fis.read( byteArray );

        impl.importPackages( byteArray,
                             true );

        PackageItem item = impl.repository.loadPackage( "testExportPackage" );
        assertNotNull( item );
        assertEquals( "desc",
                      item.getDescription() );
    }

    @Ignore
    @Test
    public void testExportPackage() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        int n = impl.listPackages().length;
        impl.createCategory( "/",
                             "testExportPackageCat1",
                             "desc" );
        impl.createCategory( "/",
                             "testExportPackageCat2",
                             "desc" );
        PackageItem p = impl.repository.createPackage( "testExportPackage",
                                                       "" );

        String uuid1 = impl.createNewRule( "testExportPackageAsset1",
                                           "desc",
                                           "testExportPackageCat1",
                                           "testExportPackage",
                                           "dsl" );

        String uuid2 = impl.createNewRule( "testExportPackageAsset2",
                                           "desc",
                                           "testExportPackageCat2",
                                           "testExportPackage",
                                           "dsl" );

        byte[] exportedPackage = impl.exportPackages( "testExportPackage" );

        assertNotNull( exportedPackage );

        File file = new File( "d:\\testExportPackage.xml" );

        FileOutputStream fos = new FileOutputStream( file );

        fos.write( exportedPackage );
        fos.close();
    }

    @Test
    @Ignore
    public void testArchiveAsset() throws Exception {
        RepositoryService impl = getServiceImplementation();
        String cat = "testArchiveAsset";
        impl.createCategory( "/",
                             cat,
                             "ya" );
        String pkgUUID = impl.createPackage( "testArchiveAsset",
                                             "" );
        @SuppressWarnings("unused")
        String uuid = impl.createNewRule( "testArchiveAsset",
                                          "x",
                                          cat,
                                          "testArchiveAsset",
                                          "testArchiveAsset" );
        @SuppressWarnings("unused")
        String uuid2 = impl.createNewRule( "testArchiveAsset2",
                                           "x",
                                           cat,
                                           "testArchiveAsset",
                                           "testArchiveAsset" );
        @SuppressWarnings("unused")
        String uuid3 = impl.createNewRule( "testArchiveAsset3",
                                           "x",
                                           cat,
                                           "testArchiveAsset",
                                           "testArchiveAsset" );
        String uuid4 = impl.createNewRule( "testArchiveAsset4",
                                           "x",
                                           cat,
                                           "testArchiveAsset",
                                           "testArchiveAsset" );

        TableDataResult res = impl.listAssets( pkgUUID,
                                               arr( "testArchiveAsset" ),
                                               0,
                                               -1,
                                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 4,
                      res.data.length );
        assertEquals( 4,
                      res.total );
        assertFalse( res.hasNext );

        TableDataResult td = impl.loadArchivedAssets( 0,
                                                      1000 );
        assertEquals( -1,
                      td.total );
        impl.archiveAsset( uuid4 );

        TableDataResult td2 = impl.loadArchivedAssets( 0,
                                                       1000 );
        assertTrue( td2.data.length == td.data.length + 1 );

        res = impl.listAssets( pkgUUID,
                               arr( "testArchiveAsset" ),
                               0,
                               -1,
                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 3,
                      res.data.length );

        impl.unArchiveAsset( uuid4 );

        res = impl.listAssets( pkgUUID,
                               arr( "testArchiveAsset" ),
                               0,
                               -1,
                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 4,
                      res.data.length );

    }

    @Test
    @Ignore
    public void testArchiveAssetWhenParentPackageArchived() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        String packageName = "testArchiveAssetWhenParentPackageArchived";
        String cat = packageName;
        impl.createCategory( "/",
                             cat,
                             "ya" );
        String pkgUUID = impl.createPackage( packageName,
                                             "" );
        @SuppressWarnings("unused")
        String uuid = impl.createNewRule( packageName,
                                          "x",
                                          cat,
                                          packageName,
                                          packageName );
        @SuppressWarnings("unused")
        String uuid2 = impl.createNewRule( "testArchiveAssetWhenParentPackageArchived2",
                                           "x",
                                           cat,
                                           packageName,
                                           packageName );
        @SuppressWarnings("unused")
        String uuid3 = impl.createNewRule( "testArchiveAssetWhenParentPackageArchived3",
                                           "x",
                                           cat,
                                           packageName,
                                           packageName );
        String uuid4 = impl.createNewRule( "testArchiveAssetWhenParentPackageArchived4",
                                           "x",
                                           cat,
                                           packageName,
                                           packageName );

        TableDataResult res = impl.listAssets( pkgUUID,
                                               arr( packageName ),
                                               0,
                                               -1,
                                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 4,
                      res.data.length );
        assertEquals( 4,
                      res.total );
        assertFalse( res.hasNext );

        TableDataResult td = impl.loadArchivedAssets( 0,
                                                      1000 );
        assertEquals( -1,
                      td.total );
        impl.archiveAsset( uuid4 );
        PackageItem packageItem = impl.repository.loadPackage( packageName );
        packageItem.archiveItem( true );

        TableDataResult td2 = impl.loadArchivedAssets( 0,
                                                       1000 );
        assertTrue( td2.data.length == td.data.length + 1 );

        res = impl.listAssets( pkgUUID,
                               arr( packageName ),
                               0,
                               -1,
                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 3,
                      res.data.length );

        try {
            impl.unArchiveAsset( uuid4 );
            fail( "Should throw an exception" );
        } catch ( RulesRepositoryException e ) {
            // Works
        }

        res = impl.listAssets( pkgUUID,
                               arr( packageName ),
                               0,
                               -1,
                               AssetItemGrid.RULE_LIST_TABLE_ID );
        assertEquals( 3,
                      res.data.length );

    }

    @Test
    @Ignore
    public void testLoadSuggestionCompletionEngine() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testSILoadSCE",
                                              "" );

        AssetItem model = pkg.addAsset( "MyModel",
                                        "" );
        model.updateFormat( AssetFormats.MODEL );
        model.updateBinaryContentAttachment( this.getClass().getResourceAsStream( "/billasurf.jar" ) );
        model.checkin( "" );
        ServiceImplementation.updateDroolsHeader( "import com.billasurf.Board",
                                                  pkg );

        AssetItem m2 = pkg.addAsset( "MyModel2",
                                     "" );
        m2.updateFormat( AssetFormats.DRL_MODEL );
        m2.updateContent( "declare Whee\n name: String\nend" );
        m2.checkin( "" );

        AssetItem r1 = pkg.addAsset( "garbage",
                                     "" );
        r1.updateFormat( AssetFormats.DRL );
        r1.updateContent( "this will not compile" );
        r1.checkin( "" );

        SuggestionCompletionEngine eng = impl.loadSuggestionCompletionEngine( pkg.getName() );
        assertNotNull( eng );

        //The loader could define extra imports
        assertTrue( eng.getFactTypes().length >= 2 );
        List<String> factTypes = Arrays.asList( eng.getFactTypes() );

        assertTrue( factTypes.contains( "Board" ) );
        assertTrue( factTypes.contains( "Whee" ) );

    }

    @Test
    @Ignore
    public void testDiscussion() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        PackageItem pkg = repo.createPackage( "testDiscussionFeature",
                                              "" );
        AssetItem rule1 = pkg.addAsset( "rule_1",
                                        "" );
        rule1.updateFormat( AssetFormats.DRL );
        rule1.updateContent( "rule 'rule1' \n when \np : Person() \n then \np.setAge(42); \n end" );
        rule1.checkin( "" );
        repo.save();

        List<DiscussionRecord> dr = impl.loadDiscussionForAsset( rule1.getUUID() );
        assertEquals( 0,
                      dr.size() );

        List<DiscussionRecord> dr_ = impl.addToDiscussionForAsset( rule1.getUUID(),
                                                                   "This is a note" );
        assertEquals( 1,
                      dr_.size() );
        assertNotNull( dr_.get( 0 ).author );
        assertEquals( "This is a note",
                      dr_.get( 0 ).note );
        Thread.sleep( 100 );
        impl.addToDiscussionForAsset( rule1.getUUID(),
                                      "This is a note2" );

        List<DiscussionRecord> d_ = impl.loadDiscussionForAsset( rule1.getUUID() );
        assertEquals( 2,
                      d_.size() );

        assertEquals( "This is a note",
                      d_.get( 0 ).note );
        assertEquals( "This is a note2",
                      d_.get( 1 ).note );
        assertTrue( d_.get( 1 ).timestamp > d_.get( 0 ).timestamp );

        rule1.updateContent( "some more content" );
        rule1.checkin( "" );

        impl.addToDiscussionForAsset( rule1.getUUID(),
                                      "This is a note2" );
        d_ = impl.loadDiscussionForAsset( rule1.getUUID() );
        assertEquals( 3,
                      d_.size() );

        assertEquals( "This is a note",
                      d_.get( 0 ).note );
        assertEquals( "This is a note2",
                      d_.get( 1 ).note );

        impl.clearAllDiscussionsForAsset( rule1.getUUID() );
        d_ = impl.loadDiscussionForAsset( rule1.getUUID() );
        assertEquals( 0,
                      d_.size() );

        impl.addToDiscussionForAsset( rule1.getUUID(),
                                      "This is a note2" );
        d_ = impl.loadDiscussionForAsset( rule1.getUUID() );
        assertEquals( 1,
                      d_.size() );
    }

    /**
     * This will test creating a package, check it compiles, and can exectute
     * rules, then take a snapshot, and check that it reports errors.
     */

    @Test
    @Ignore
    public void testBinaryPackageCompileAndExecute() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testBinaryPackageCompile",
                                              "" );
        ServiceImplementation.updateDroolsHeader( "global java.util.List ls \n import org.drools.Person",
                                                  pkg );
        AssetItem rule1 = pkg.addAsset( "rule_1",
                                        "" );
        rule1.updateFormat( AssetFormats.DRL );
        rule1.updateContent( "rule 'rule1' \n when \np : Person() \n then \np.setAge(42); \n end" );
        rule1.checkin( "" );
        repo.save();

        BuilderResult result = impl.buildPackage( pkg.getUUID(),
                                                  true );
        assertNull( result );

        pkg = repo.loadPackage( "testBinaryPackageCompile" );
        byte[] binPackage = pkg.getCompiledPackageBytes();

        assertNotNull( binPackage );

        Package binPkg = (Package) DroolsStreamUtils.streamIn( binPackage );

        assertNotNull( binPkg );
        assertTrue( binPkg.isValid() );

        Person p = new Person();

        BinaryRuleBaseLoader loader = new BinaryRuleBaseLoader();
        loader.addPackage( new ByteArrayInputStream( binPackage ) );
        RuleBase rb = loader.getRuleBase();

        StatelessSession sess = rb.newStatelessSession();
        sess.setGlobal( "ls",
                        new ArrayList<String>() );
        sess.execute( p );

        assertEquals( 42,
                      p.getAge() );

        impl.createPackageSnapshot( "testBinaryPackageCompile",
                                    "SNAP1",
                                    false,
                                    "" );

        rule1.updateContent( "rule 'rule1' \n when p:PersonX() \n then System.err.println(42); \n end" );
        rule1.checkin( "" );

        result = impl.buildPackage( pkg.getUUID(),
                                    true );
        assertNotNull( result );
        assertEquals( 1,
                      result.lines.length );
        assertEquals( rule1.getName(),
                      result.lines[0].assetName );
        assertEquals( AssetFormats.DRL,
                      result.lines[0].assetFormat );
        assertNotNull( result.lines[0].message );
        assertEquals( rule1.getUUID(),
                      result.lines[0].uuid );

        pkg = repo.loadPackageSnapshot( "testBinaryPackageCompile",
                                        "SNAP1" );
        result = impl.buildPackage( pkg.getUUID(),
                                    true );
        assertNull( result );

    }

    /**
     * This will test creating a package with a BRL rule, check it compiles, and
     * can exectute rules, then take a snapshot, and check that it reports
     * errors.
     */

    @Test
    @Ignore
    public void testBinaryPackageCompileAndExecuteWithBRXML() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testBinaryPackageCompileBRL",
                                              "" );
        ServiceImplementation.updateDroolsHeader( "import org.drools.Person",
                                                  pkg );
        AssetItem rule2 = pkg.addAsset( "rule2",
                                        "" );
        rule2.updateFormat( AssetFormats.BUSINESS_RULE );

        RuleModel model = new RuleModel();
        model.name = "rule2";
        FactPattern pattern = new FactPattern( "Person" );

        SingleFieldConstraint con = new SingleFieldConstraint();
        con.setConstraintValueType( BaseSingleFieldConstraint.TYPE_PREDICATE );
        con.setValue( "name soundslike 'foobar'" );
        pattern.addConstraint( con );

        pattern.boundName = "p";
        ActionSetField action = new ActionSetField( "p" );
        ActionFieldValue value = new ActionFieldValue( "age",
                                                       "42",
                                                       SuggestionCompletionEngine.TYPE_NUMERIC );
        action.addFieldValue( value );

        model.addLhsItem( pattern );
        model.addRhsItem( action );

        rule2.updateContent( BRXMLPersistence.getInstance().marshal( model ) );
        rule2.checkin( "" );
        repo.save();

        BuilderResult result = impl.buildPackage( pkg.getUUID(),
                                                  true );
        if ( result != null ) {
            for ( int i = 0; i < result.lines.length; i++ ) {
                System.err.println( result.lines[i].message );
            }
        }
        assertNull( result );

        pkg = repo.loadPackage( "testBinaryPackageCompileBRL" );
        byte[] binPackage = pkg.getCompiledPackageBytes();

        // Here is where we write it out is needed... set to true if needed for
        // the binary test below "testLoadAndExecBinary"
        boolean saveBinPackage = false;
        if ( saveBinPackage ) {
            FileOutputStream out = new FileOutputStream( "RepoBinPackage.pkg" );
            out.write( binPackage );
            out.flush();
            out.close();
        }

        assertNotNull( binPackage );

        Package binPkg = (Package) DroolsStreamUtils.streamIn( binPackage );

        assertNotNull( binPkg );
        assertTrue( binPkg.isValid() );

        // and this shows off the "soundex" thing...
        Person p = new Person( "fubar" );

        BinaryRuleBaseLoader loader = new BinaryRuleBaseLoader();
        loader.addPackage( new ByteArrayInputStream( binPackage ) );
        RuleBase rb = loader.getRuleBase();

        StatelessSession sess = rb.newStatelessSession();
        sess.execute( p );
        assertEquals( 42,
                      p.getAge() );

        impl.createPackageSnapshot( "testBinaryPackageCompileBRL",
                                    "SNAP1",
                                    false,
                                    "" );

        pattern.factType = "PersonX";
        rule2.updateContent( BRXMLPersistence.getInstance().marshal( model ) );
        rule2.checkin( "" );

        result = impl.buildPackage( pkg.getUUID(),
                                    true );
        assertNotNull( result );
        assertTrue( result.lines.length > 0 );
        // assertEquals(2, results.length);
        assertEquals( rule2.getName(),
                      result.lines[0].assetName );
        assertEquals( AssetFormats.BUSINESS_RULE,
                      result.lines[0].assetFormat );
        assertNotNull( result.lines[0].message );
        assertEquals( rule2.getUUID(),
                      result.lines[0].uuid );

        pkg = repo.loadPackageSnapshot( "testBinaryPackageCompileBRL",
                                        "SNAP1" );
        result = impl.buildPackage( pkg.getUUID(),
                                    true );
        assertNull( result );

        // check that the rule name in the model is being set
        AssetItem asset2 = pkg.addAsset( "testSetRuleName",
                                         "" );
        asset2.updateFormat( AssetFormats.BUSINESS_RULE );
        asset2.checkin( "" );

        RuleModel model2 = new RuleModel();
        assertNull( model2.name );
        RuleAsset asset = impl.loadRuleAsset( asset2.getUUID() );
        asset.content = (PortableObject) model2;

        impl.checkinVersion( asset );

        asset = impl.loadRuleAsset( asset2.getUUID() );

        model2 = (RuleModel) asset.content;
        assertNotNull( model2 );
        assertNotNull( model2.name );
        assertEquals( asset2.getName(),
                      model2.name );

    }

    /**
     * this loads up a precompile binary package. If this fails, then it means
     * it needs to be updated. It gets the package form the BRL example above.
     * Simply set saveBinPackage to true to save a new version of the RepoBinPackage.pkg.
     */

    @Test
    @Ignore
    public void testLoadAndExecBinary() throws Exception {
        Person p = new Person( "fubar" );
        BinaryRuleBaseLoader loader = new BinaryRuleBaseLoader();
        loader.addPackage( this.getClass().getResourceAsStream( "/RepoBinPackage.pkg" ) );
        RuleBase rb = loader.getRuleBase();
        StatelessSession sess = rb.newStatelessSession();
        sess.execute( p );
        assertEquals( 42,
                      p.getAge() );
    }

    @Test
    @Ignore
    public void testSuggestionCompletionLoading() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testSISuggestionCompletionLoading",
                                              "" );
        ServiceImplementation.updateDroolsHeader( "import org.drools.Person",
                                                  pkg );
        AssetItem rule1 = pkg.addAsset( "model_1",
                                        "" );
        rule1.updateFormat( AssetFormats.DRL_MODEL );
        rule1.updateContent( "declare Whee\n name: String \nend" );
        rule1.checkin( "" );
        repo.save();

    }

    @Test
    @Ignore
    public void testPackageSource() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testPackageSource",
                                              "" );
        ServiceImplementation.updateDroolsHeader( "import org.goo.Ber",
                                                  pkg );
        AssetItem rule1 = pkg.addAsset( "rule_1",
                                        "" );
        rule1.updateFormat( AssetFormats.DRL );
        rule1.updateContent( "rule 'rule1' \n when p:Person() \n then p.setAge(42); \n end" );
        rule1.checkin( "" );
        repo.save();

        AssetItem func = pkg.addAsset( "funky",
                                       "" );
        func.updateFormat( AssetFormats.FUNCTION );
        func.updateContent( "this is a func" );
        func.checkin( "" );

        String drl = impl.buildPackageSource( pkg.getUUID() );
        assertNotNull( drl );

        assertTrue( drl.indexOf( "import org.goo.Ber" ) > -1 );
        assertTrue( drl.indexOf( "package testPackageSource" ) > -1 );
        assertTrue( drl.indexOf( "rule 'rule1'" ) > -1 );
        assertTrue( drl.indexOf( "this is a func" ) > -1 );
        assertTrue( drl.indexOf( "this is a func" ) < drl.indexOf( "rule 'rule1'" ) );
        assertTrue( drl.indexOf( "package testPackageSource" ) < drl.indexOf( "this is a func" ) );
        assertTrue( drl.indexOf( "package testPackageSource" ) < drl.indexOf( "import org.goo.Ber" ) );

        AssetItem dsl = pkg.addAsset( "MyDSL",
                                      "" );
        dsl.updateFormat( AssetFormats.DSL );
        dsl.updateContent( "[when]This is foo=bar()\n[then]do something=yeahMan();" );
        dsl.checkin( "" );

        AssetItem asset = pkg.addAsset( "MyDSLRule",
                                        "" );
        asset.updateFormat( AssetFormats.DSL_TEMPLATE_RULE );
        asset.updateContent( "when \n This is foo \n then \n do something" );
        asset.checkin( "" );

        drl = impl.buildPackageSource( pkg.getUUID() );
        assertNotNull( drl );

        assertTrue( drl.indexOf( "import org.goo.Ber" ) > -1 );
        assertTrue( drl.indexOf( "This is foo" ) == -1 );
        assertTrue( drl.indexOf( "do something" ) == -1 );
        assertTrue( drl.indexOf( "bar()" ) > 0 );
        assertTrue( drl.indexOf( "yeahMan();" ) > 0 );

    }

    @Test
    @Ignore
    public void testAssetSource() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testAssetSource",
                                              "" );
        AssetItem asset = pkg.addAsset( "testRule",
                                        "" );
        asset.updateFormat( AssetFormats.DRL );
        asset.updateContent( "rule 'n' \n when Foo() then bar(); \n end" );
        asset.checkin( "" );
        repo.save();

        RuleAsset rule = impl.loadRuleAsset( asset.getUUID() );
        String drl = impl.buildAssetSource( rule );
        assertEquals( "rule 'n' \n when Foo() then bar(); \n end",
                      drl );

        asset = pkg.addAsset( "DT",
                              "" );
        asset.updateFormat( AssetFormats.DECISION_SPREADSHEET_XLS );
        asset.updateBinaryContentAttachment( this.getClass().getResourceAsStream( "/SampleDecisionTable.xls" ) );
        asset.checkin( "" );

        rule = impl.loadRuleAsset( asset.getUUID() );
        drl = impl.buildAssetSource( rule );
        assertNotNull( drl );
        assertTrue( drl.indexOf( "rule" ) > -1 );
        assertTrue( drl.indexOf( "policy: Policy" ) > -1 );

        AssetItem dsl = pkg.addAsset( "MyDSL",
                                      "" );
        dsl.updateFormat( AssetFormats.DSL );
        dsl.updateContent( "[when]This is foo=bar()\n[then]do something=yeahMan();" );
        dsl.checkin( "" );

        asset = pkg.addAsset( "MyDSLRule",
                              "" );
        asset.updateFormat( AssetFormats.DSL_TEMPLATE_RULE );
        asset.updateContent( "when \n This is foo \n then \n do something" );
        asset.checkin( "" );

        rule = impl.loadRuleAsset( asset.getUUID() );
        drl = impl.buildAssetSource( rule );
        assertNotNull( drl );
        assertTrue( drl.indexOf( "This is foo" ) == -1 );
        assertTrue( drl.indexOf( "do something" ) == -1 );
        assertTrue( drl.indexOf( "bar()" ) > -1 );
        assertTrue( drl.indexOf( "yeahMan();" ) > -1 );

        rule = impl.loadRuleAsset( repo.copyAsset( asset.getUUID(),
                                                   "testAssetSource",
                                                   "newRuleName" ) );
        // System.err.println(((RuleContentText)rule.content).content);
        drl = impl.buildAssetSource( rule );
        assertNotNull( drl );
        assertTrue( drl.indexOf( "newRuleName" ) > 0 );

    }

    @Test
    @Ignore
    public void testBuildAssetWithError() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testBuildAssetWithError",
                                              "" );
        AssetItem model = pkg.addAsset( "MyModel",
                                        "" );
        model.updateFormat( AssetFormats.MODEL );
        model.updateBinaryContentAttachment( this.getClass().getResourceAsStream( "/billasurf.jar" ) );
        model.checkin( "" );

        ServiceImplementation.updateDroolsHeader( "import com.billasurf.Person",
                                                  pkg );

        AssetItem asset = pkg.addAsset( "testRule",
                                        "" );
        asset.updateFormat( AssetFormats.DRL );
        asset.updateContent( "rule 'MyGoodRule' \n when Personx() then System.err.println(42); \n end" );
        asset.checkin( "" );
        repo.save();

        RuleAsset rule = impl.loadRuleAsset( asset.getUUID() );

        BuilderResult result = impl.buildAsset( rule );
        assertNotNull( result );
        assertEquals( -1,
                      result.lines[0].message.indexOf( "Check log for" ) );
        assertTrue( result.lines[0].message.indexOf( "Unable to resolve" ) > -1 );

    }

    @Test
    @Ignore
    public void testBuildAsset() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testBuildAsset",
                                              "" );
        AssetItem model = pkg.addAsset( "MyModel",
                                        "" );
        model.updateFormat( AssetFormats.MODEL );
        model.updateBinaryContentAttachment( this.getClass().getResourceAsStream( "/billasurf.jar" ) );
        model.checkin( "" );

        ServiceImplementation.updateDroolsHeader( "import com.billasurf.Person",
                                                  pkg );

        AssetItem asset = pkg.addAsset( "testRule",
                                        "" );
        asset.updateFormat( AssetFormats.DRL );
        asset.updateContent( "rule 'MyGoodRule' \n when Person() then System.err.println(42); \n end" );
        asset.checkin( "" );
        repo.save();

        RuleAsset rule = impl.loadRuleAsset( asset.getUUID() );

        // check its all OK
        BuilderResult result = impl.buildAsset( rule );
        assertNull( result );

        ServiceImplementation.ruleBaseCache.clear();

        // try it with a bad rule
        RuleContentText text = new RuleContentText();
        text.content = "rule 'MyBadRule' \n when Personx() then System.err.println(42); \n end";
        rule.content = text;

        result = impl.buildAsset( rule );
        assertNotNull( result );
        assertNotNull( result.lines[0].message );
        assertEquals( AssetFormats.DRL,
                      result.lines[0].assetFormat );

        // now mix in a DSL
        AssetItem dsl = pkg.addAsset( "MyDSL",
                                      "" );
        dsl.updateFormat( AssetFormats.DSL );
        dsl.updateContent( "[when]There is a person=Person()\n[then]print out 42=System.err.println(42);" );
        dsl.checkin( "" );

        AssetItem dslRule = pkg.addAsset( "dslRule",
                                          "" );
        dslRule.updateFormat( AssetFormats.DSL_TEMPLATE_RULE );
        dslRule.updateContent( "when \n There is a person \n then \n print out 42" );
        dslRule.checkin( "" );

        rule = impl.loadRuleAsset( dslRule.getUUID() );

        result = impl.buildAsset( rule );
        assertNull( result );

        asset = pkg.addAsset( "someEnumThing",
                              "" );
        asset.updateFormat( AssetFormats.ENUMERATION );
        asset.updateContent( "goober boy" );
        asset.checkin( "" );
        result = impl.buildAsset( impl.loadRuleAsset( asset.getUUID() ) );
        assertFalse( result.lines.length == 0 );

    }

    @Test
    @Ignore
    public void testBuildAssetMultipleFunctionsCallingEachOther() throws Exception {

        ServiceImplementation impl = getServiceImplementation();
        impl.createPackage( "testBuildAssetMultipleFunctionsCallingEachOther",
                            "" );
        impl.createCategory( "/",
                             "funkytest",
                             "" );

        String uuidt1 = impl.createNewRule( "t1",
                                            "",
                                            "funkytest",
                                            "testBuildAssetMultipleFunctionsCallingEachOther",
                                            AssetFormats.FUNCTION );
        RuleAsset t1 = impl.loadRuleAsset( uuidt1 );
        RuleContentText t1Content = new RuleContentText();
        t1Content.content = "function void t1(){\n";
        t1Content.content += " t2();\n";
        t1Content.content += "}\n";
        t1.content = t1Content;
        impl.checkinVersion( t1 );

        String uuidt2 = impl.createNewRule( "t2",
                                            "",
                                            "funkytest",
                                            "testBuildAssetMultipleFunctionsCallingEachOther",
                                            AssetFormats.FUNCTION );
        RuleAsset t2 = impl.loadRuleAsset( uuidt2 );
        RuleContentText t2Content = new RuleContentText();
        t2Content.content = "function void t2(){\n";
        t2Content.content += " t1();\n";
        t2Content.content += "}\n";
        t2.content = t2Content;
        impl.checkinVersion( t2 );

        BuilderResult result = impl.buildAsset( t1 );

        assertNull( result );

    }

    @Test
    @Ignore
    public void testBuildAssetBRXMLAndCopy() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testBuildAssetBRL",
                                              "" );
        AssetItem model = pkg.addAsset( "MyModel",
                                        "" );
        model.updateFormat( AssetFormats.MODEL );
        model.updateBinaryContentAttachment( this.getClass().getResourceAsStream( "/billasurf.jar" ) );
        model.checkin( "" );

        ServiceImplementation.updateDroolsHeader( "import com.billasurf.Person",
                                                  pkg );
        impl.createCategory( "/",
                             "brl",
                             "" );

        String uuid = impl.createNewRule( "testBRL",
                                          "",
                                          "brl",
                                          "testBuildAssetBRL",
                                          AssetFormats.BUSINESS_RULE );

        RuleAsset rule = impl.loadRuleAsset( uuid );

        RuleModel m = (RuleModel) rule.content;
        assertNotNull( m );
        m.name = "testBRL";

        FactPattern p = new FactPattern( "Person" );
        p.boundName = "p";
        SingleFieldConstraint con = new SingleFieldConstraint();
        con.setFieldName( "name" );
        con.setValue( "mark" );
        con.setOperator( "==" );
        con.setConstraintValueType( SingleFieldConstraint.TYPE_LITERAL );

        p.addConstraint( con );

        m.addLhsItem( p );

        ActionSetField set = new ActionSetField( "p" );
        ActionFieldValue f = new ActionFieldValue( "name",
                                                   "42-ngoo",
                                                   SuggestionCompletionEngine.TYPE_STRING );
        set.addFieldValue( f );

        m.addRhsItem( set );

        impl.checkinVersion( rule );

        // check its all OK
        BuilderResult result = impl.buildAsset( rule );
        if ( result != null ) {
            for ( int i = 0; i < result.lines.length; i++ ) {
                System.err.println( result.lines[i].message );
            }
        }
        assertNull( result );

        List<AssetItem> assets = iteratorToList( pkg.getAssets() );
        assertEquals( 3,
                      assets.size() );
        // now lets copy...
        String newUUID = impl.copyAsset( rule.uuid,
                                         rule.metaData.packageName,
                                         "ruleName2" );

        assets = iteratorToList( pkg.getAssets() );
        assertEquals( 4,
                      assets.size() ); //we have 4 due to the drools.package file.
        RuleAsset asset = impl.loadRuleAsset( newUUID );

        String pkgSource = impl.buildPackageSource( pkg.getUUID() );

        assertTrue( pkgSource.indexOf( "ruleName2" ) > 0 );
        assertTrue( impl.buildAssetSource( asset ).indexOf( "ruleName2" ) > 0 );
        assertTrue( impl.buildAssetSource( asset ).indexOf( "testBRL" ) == -1 );

        // RuleModel model2 = (RuleModel) asset.content;
        // assertEquals("ruleName2", model2.name);

    }

    private List<AssetItem> iteratorToList(Iterator<AssetItem> assets) {
        List<AssetItem> result = new ArrayList<AssetItem>();
        while ( assets.hasNext() ) {
            result.add( assets.next() );

        }
        return result;
    }

    @Test
    @Ignore
    public void testBuildAssetWithPackageConfigError() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        PackageItem pkg = repo.createPackage( "testBuildAssetWithPackageConfigError",
                                              "" );
        // AssetItem model = pkg.addAsset( "MyModel", "" );
        // model.updateFormat( AssetFormats.MODEL );
        // model.updateBinaryContentAttachment(
        // this.getClass().getResourceAsStream( "/billasurf.jar" ) );
        // model.checkin( "" );

        // pkg.updateHeader( "import com.billasurf.Person" );

        AssetItem asset = pkg.addAsset( "testRule",
                                        "" );
        asset.updateFormat( AssetFormats.DRL );
        asset.updateContent( "rule 'MyGoodRule' \n when \n then \n end" );
        asset.checkin( "" );
        repo.save();

        RuleAsset rule = impl.loadRuleAsset( asset.getUUID() );

        // check its all OK
        BuilderResult result = impl.buildAsset( rule );
        if ( !(result == null) ) {
            System.err.println( result.lines[0].assetName + " " + result.lines[0].message );
        }
        assertNull( result );

        ServiceImplementation.updateDroolsHeader( "importxxxx",
                                                  pkg );
        repo.save();
        result = impl.buildAsset( rule );
        assertNotNull( result );

        assertEquals( 1,
                      result.lines.length );
        assertEquals( "package",
                      result.lines[0].assetFormat );
        assertNotNull( result.lines[0].message );

    }

    @Test
    @Ignore
    public void testRuleNameList() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testRuleNameList",
                                              "" );
        ServiceImplementation.updateDroolsHeader( "import org.goo.Ber",
                                                  pkg );
        AssetItem rule1 = pkg.addAsset( "rule_1",
                                        "" );
        rule1.updateFormat( AssetFormats.DRL );
        rule1.updateContent( "package wee.wee \nrule 'rule1' \n  when p:Person() \n then p.setAge(42); \n end" );
        rule1.checkin( "" );
        repo.save();

        AssetItem rule2 = pkg.addAsset( "rule_2",
                                        "" );
        rule2.updateFormat( AssetFormats.DRL );
        rule2.updateContent( "rule 'rule2' \n ruleflow-group 'whee' \nwhen p:Person() \n then p.setAge(42); \n end" );
        rule2.checkin( "" );
        repo.save();

        String[] list = impl.listRulesInPackage( pkg.getName() );
        assertEquals( 2,
                      list.length );
        assertEquals( "rule1",
                      list[0] );
        assertEquals( "rule2",
                      list[1] );

        rule2.updateContent( "wang" );
        rule2.checkin( "" );

        list = impl.listRulesInPackage( pkg.getName() );
        assertEquals( 2,
                      list.length );

    }

    /**
     * This idea of this is to not compile packages more then we have to.
     */

    @Test
    @Ignore
    public void testBinaryUpToDate() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testBinaryPackageUpToDate",
                                              "" );
        assertFalse( pkg.isBinaryUpToDate() );
        ServiceImplementation.updateDroolsHeader( "import org.drools.Person",
                                                  pkg );
        AssetItem rule1 = pkg.addAsset( "rule_1",
                                        "" );
        rule1.updateFormat( AssetFormats.DRL );
        rule1.updateContent( "rule 'rule1' \n when \np : Person() \n then \np.setAge(42); \n end" );
        rule1.checkin( "" );
        repo.save();

        assertFalse( pkg.isBinaryUpToDate() );
        assertFalse( ServiceImplementation.ruleBaseCache.containsKey( pkg.getUUID() ) );
        ServiceImplementation.ruleBaseCache.remove( "XXX" );

        BuilderResult results = impl.buildPackage( pkg.getUUID(),
                                                   true );
        assertNull( results );

        pkg = repo.loadPackage( "testBinaryPackageUpToDate" );
        byte[] binPackage = pkg.getCompiledPackageBytes();

        assertNotNull( binPackage );

        assertTrue( pkg.getNode().getProperty( "drools:binaryUpToDate" ).getBoolean() );
        assertTrue( pkg.isBinaryUpToDate() );
        assertFalse( ServiceImplementation.ruleBaseCache.containsKey( pkg.getUUID() ) );

        RuleAsset asset = impl.loadRuleAsset( rule1.getUUID() );
        impl.checkinVersion( asset );

        assertFalse( pkg.getNode().getProperty( "drools:binaryUpToDate" ).getBoolean() );
        assertFalse( ServiceImplementation.ruleBaseCache.containsKey( pkg.getUUID() ) );

        impl.buildPackage( pkg.getUUID(),
                           false );

        assertTrue( pkg.getNode().getProperty( "drools:binaryUpToDate" ).getBoolean() );
        assertFalse( ServiceImplementation.ruleBaseCache.containsKey( pkg.getUUID() ) );

        PackageConfigData config = impl.loadPackageConfig( pkg.getUUID() );
        impl.savePackage( config );

        assertFalse( pkg.getNode().getProperty( "drools:binaryUpToDate" ).getBoolean() );
        assertFalse( pkg.isBinaryUpToDate() );
        impl.buildPackage( pkg.getUUID(),
                           false );
        assertTrue( pkg.getNode().getProperty( "drools:binaryUpToDate" ).getBoolean() );
        assertTrue( pkg.isBinaryUpToDate() );

    }

    @Test
    @Ignore
    public void testRunScenario() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        System.out.println( "create package" );
        PackageItem pkg = repo.createPackage( "testScenarioRun",
                                              "" );
        ServiceImplementation.updateDroolsHeader( "import org.drools.Person\n global org.drools.Cheese cheese\n",
                                                  pkg );
        AssetItem rule1 = pkg.addAsset( "rule_1",
                                        "" );
        rule1.updateFormat( AssetFormats.DRL );
        rule1.updateContent( "rule 'rule1' \n when \np : Person() \n then \np.setAge(42); \n end" );
        rule1.checkin( "" );
        repo.save();

        Scenario sc = new Scenario();
        FactData person = new FactData();
        person.name = "p";
        person.type = "Person";
        person.fieldData.add( new FieldData( "age",
                                             "40" ) );
        person.fieldData.add( new FieldData( "name",
                                             "michael" ) );

        sc.fixtures.add( person );
        sc.fixtures.add( new ExecutionTrace() );
        VerifyRuleFired vr = new VerifyRuleFired( "rule1",
                                                  1,
                                                  null );
        sc.fixtures.add( vr );

        VerifyFact vf = new VerifyFact();
        vf.name = "p";
        vf.fieldValues.add( new VerifyField( "name",
                                             "michael",
                                             "==" ) );
        vf.fieldValues.add( new VerifyField( "age",
                                             "42",
                                             "==" ) );
        sc.fixtures.add( vf );

        FactData cheese = new FactData();
        cheese.name = "cheese";
        cheese.type = "Cheese";
        cheese.fieldData.add( new FieldData( "price",
                                             "42" ) );
        sc.globals.add( cheese );

        ScenarioRunResult res = impl.runScenario( pkg.getName(),
                                                  sc ).result;
        assertNull( res.getErrors() );
        assertNotNull( res.getScenario() );
        assertTrue( vf.wasSuccessful() );
        assertTrue( vr.wasSuccessful() );

        res = impl.runScenario( pkg.getName(),
                                sc ).result;
        assertNull( res.getErrors() );
        assertNotNull( res.getScenario() );
        assertTrue( vf.wasSuccessful() );
        assertTrue( vr.wasSuccessful() );

        ServiceImplementation.ruleBaseCache.clear();
        res = impl.runScenario( pkg.getName(),
                                sc ).result;
        assertNull( res.getErrors() );
        assertNotNull( res.getScenario() );
        assertTrue( vf.wasSuccessful() );
        assertTrue( vr.wasSuccessful() );

        //BuilderResult[] results = impl.buildPackage(pkg.getUUID(), null, true);
        //assertNull(results);

        rule1.updateContent( "Junk" );
        rule1.checkin( "" );

        ServiceImplementation.ruleBaseCache.clear();
        pkg.updateBinaryUpToDate( false );
        repo.save();
        res = impl.runScenario( pkg.getName(),
                                sc ).result;
        assertNotNull( res.getErrors() );
        assertNull( res.getScenario() );

        assertTrue( res.getErrors().length > 0 );

        impl.createCategory( "/",
                             "sc",
                             "" );

        String scenarioId = impl.createNewRule( "sc1",
                                                "s",
                                                "sc",
                                                pkg.getName(),
                                                AssetFormats.TEST_SCENARIO );

        RuleAsset asset = impl.loadRuleAsset( scenarioId );
        assertNotNull( asset.content );
        assertTrue( asset.content instanceof Scenario );

        Scenario sc_ = (Scenario) asset.content;
        sc_.fixtures.add( new ExecutionTrace() );
        impl.checkinVersion( asset );
        asset = impl.loadRuleAsset( scenarioId );
        assertNotNull( asset.content );
        assertTrue( asset.content instanceof Scenario );
        sc_ = (Scenario) asset.content;
        assertEquals( 1,
                      sc_.fixtures.size() );

    }

    @Test
    @Ignore
    public void testRunScenarioWithGeneratedBeans() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        PackageItem pkg = repo.createPackage( "testScenarioRunWithGeneratedBeans",
                                              "" );
        ServiceImplementation.updateDroolsHeader( "declare GenBean\n name: String \n age: int \nend\n",
                                                  pkg );
        AssetItem rule1 = pkg.addAsset( "rule_1",
                                        "" );
        rule1.updateFormat( AssetFormats.DRL );
        rule1.updateContent( "rule 'rule1' \n when \n p : GenBean(name=='mic') \n then \n p.setAge(42); \n end" );
        rule1.checkin( "" );
        repo.save();

        Scenario sc = new Scenario();
        FactData person = new FactData();
        person.name = "c";
        person.type = "GenBean";
        person.fieldData.add( new FieldData( "age",
                                             "40" ) );
        person.fieldData.add( new FieldData( "name",
                                             "mic" ) );

        sc.fixtures.add( person );
        sc.fixtures.add( new ExecutionTrace() );
        VerifyRuleFired vr = new VerifyRuleFired( "rule1",
                                                  1,
                                                  null );
        sc.fixtures.add( vr );

        VerifyFact vf = new VerifyFact();
        vf.name = "c";
        vf.fieldValues.add( new VerifyField( "name",
                                             "mic",
                                             "==" ) );
        vf.fieldValues.add( new VerifyField( "age",
                                             "42",
                                             "==" ) );
        sc.fixtures.add( vf );

        SingleScenarioResult res_ = impl.runScenario( pkg.getName(),
                                                      sc );
        assertTrue( res_.auditLog.size() > 0 );

        String[] logEntry = res_.auditLog.get( 0 );
        assertNotNull( logEntry[0],
                       logEntry[1] );

        ScenarioRunResult res = res_.result;

        assertNull( res.getErrors() );
        assertNotNull( res.getScenario() );
        assertTrue( vf.wasSuccessful() );
        assertTrue( vr.wasSuccessful() );

    }

    @Test
    @Ignore
    public void testRunPackageScenariosWithDeclaredFacts() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        PackageItem pkg = repo.createPackage( "testScenarioRunBulkWithDeclaredFacts",
                                              "" );
        ServiceImplementation.updateDroolsHeader( "declare Wang \n age: Integer \n name: String \n end",
                                                  pkg );
        AssetItem rule1 = pkg.addAsset( "rule_1",
                                        "" );
        rule1.updateFormat( AssetFormats.DRL );
        rule1.updateContent( "rule 'rule1' \n when \np : Wang() \n then \np.setAge(42); \n end" );
        rule1.checkin( "" );

        //this rule will never fire
        AssetItem rule2 = pkg.addAsset( "rule_2",
                                        "" );
        rule2.updateFormat( AssetFormats.DRL );
        rule2.updateContent( "rule 'rule2' \n when \np : Wang(age == 1000) \n then \np.setAge(46); \n end" );
        rule2.checkin( "" );
        repo.save();

        //first, the green scenario
        Scenario sc = new Scenario();
        FactData person = new FactData();
        person.name = "p";
        person.type = "Wang";
        person.fieldData.add( new FieldData( "age",
                                             "40" ) );
        person.fieldData.add( new FieldData( "name",
                                             "michael" ) );

        sc.fixtures.add( person );
        sc.fixtures.add( new ExecutionTrace() );
        VerifyRuleFired vr = new VerifyRuleFired( "rule1",
                                                  1,
                                                  null );
        sc.fixtures.add( vr );

        VerifyFact vf = new VerifyFact();
        vf.name = "p";
        vf.fieldValues.add( new VerifyField( "name",
                                             "michael",
                                             "==" ) );
        vf.fieldValues.add( new VerifyField( "age",
                                             "42",
                                             "==" ) );
        sc.fixtures.add( vf );

        AssetItem scenario1 = pkg.addAsset( "scen1",
                                            "" );
        scenario1.updateFormat( AssetFormats.TEST_SCENARIO );
        scenario1.updateContent( ScenarioXMLPersistence.getInstance().marshal( sc ) );
        scenario1.checkin( "" );

        //now the bad scenario
        sc = new Scenario();
        person = new FactData();
        person.name = "p";
        person.type = "Wang";
        person.fieldData.add( new FieldData( "age",
                                             "40" ) );
        person.fieldData.add( new FieldData( "name",
                                             "michael" ) );

        sc.fixtures.add( person );
        sc.fixtures.add( new ExecutionTrace() );
        vr = new VerifyRuleFired( "rule2",
                                  1,
                                  null );
        sc.fixtures.add( vr );

        AssetItem scenario2 = pkg.addAsset( "scen2",
                                            "" );
        scenario2.updateFormat( AssetFormats.TEST_SCENARIO );
        scenario2.updateContent( ScenarioXMLPersistence.getInstance().marshal( sc ) );
        scenario2.checkin( "" );

        BulkTestRunResult result = impl.runScenariosInPackage( pkg.getUUID() );
        assertNull( result.result );

        assertEquals( 50,
                      result.percentCovered );
        assertEquals( 1,
                      result.rulesNotCovered.length );
        assertEquals( "rule2",
                      result.rulesNotCovered[0] );

        assertEquals( 2,
                      result.results.length );

        ScenarioResultSummary s1 = result.results[0];
        assertEquals( 0,
                      s1.failures );
        assertEquals( 3,
                      s1.total );
        assertEquals( scenario1.getUUID(),
                      s1.uuid );
        assertEquals( scenario1.getName(),
                      s1.scenarioName );

        ScenarioResultSummary s2 = result.results[1];
        assertEquals( 1,
                      s2.failures );
        assertEquals( 1,
                      s2.total );
        assertEquals( scenario2.getUUID(),
                      s2.uuid );
        assertEquals( scenario2.getName(),
                      s2.scenarioName );
    }

    @Test
    @Ignore
    public void testRunScenarioWithJar() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testRunScenarioWithJar",
                                              "" );
        AssetItem model = pkg.addAsset( "MyModel",
                                        "" );
        model.updateFormat( AssetFormats.MODEL );
        model.updateBinaryContentAttachment( this.getClass().getResourceAsStream( "/billasurf.jar" ) );
        model.checkin( "" );

        ServiceImplementation.updateDroolsHeader( "import com.billasurf.Board",
                                                  pkg );

        AssetItem asset = pkg.addAsset( "testRule",
                                        "" );
        asset.updateFormat( AssetFormats.DRL );
        asset.updateContent( "rule 'MyGoodRule' \n dialect 'mvel' \n when Board() then System.err.println(42); \n end" );
        asset.checkin( "" );
        repo.save();

        Scenario sc = new Scenario();
        FactData person = new FactData();
        person.name = "p";
        person.type = "Board";
        person.fieldData.add( new FieldData( "cost",
                                             "42" ) );

        sc.fixtures.add( person );
        sc.fixtures.add( new ExecutionTrace() );
        VerifyRuleFired vr = new VerifyRuleFired( "MyGoodRule",
                                                  1,
                                                  null );
        sc.fixtures.add( vr );

        VerifyFact vf = new VerifyFact();
        vf.name = "p";

        vf.fieldValues.add( new VerifyField( "cost",
                                             "42",
                                             "==" ) );
        sc.fixtures.add( vf );

        ScenarioRunResult res = impl.runScenario( pkg.getName(),
                                                  sc ).result;
        assertNull( res.getErrors() );
        assertNotNull( res.getScenario() );

        assertTrue( vf.wasSuccessful() );
        assertTrue( vr.wasSuccessful() );

        res = impl.runScenario( pkg.getName(),
                                sc ).result;

        assertNull( res.getErrors() );
        assertNotNull( res.getScenario() );

        assertTrue( vf.wasSuccessful() );
        assertTrue( vr.wasSuccessful() );

        ServiceImplementation.ruleBaseCache.clear();

        res = impl.runScenario( pkg.getName(),
                                sc ).result;
        assertNull( res.getErrors() );
        assertNotNull( res.getScenario() );
        assertTrue( vf.wasSuccessful() );
        assertTrue( vr.wasSuccessful() );

    }

    @Test
    @Ignore
    public void testRunScenarioWithJarThatHasSourceFiles() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        // create our package
        PackageItem pkg = repo.createPackage( "testRunScenarioWithJarThatHasSourceFiles",
                                              "" );
        AssetItem model = pkg.addAsset( "MyModel",
                                        "" );
        model.updateFormat( AssetFormats.MODEL );
        model.updateBinaryContentAttachment( this.getClass().getResourceAsStream( "/jarWithSourceFiles.jar" ) );
        model.checkin( "" );

        ServiceImplementation.updateDroolsHeader( "import org.test.Person; \n import org.test.Banana; \n ",
                                                  pkg );

        AssetItem asset = pkg.addAsset( "testRule",
                                        "" );
        asset.updateFormat( AssetFormats.DRL );
        asset.updateContent( "rule 'MyGoodRule' \n dialect 'mvel' \n when \n Person() \n then \n insert( new Banana() ); \n end" );
        asset.checkin( "" );
        repo.save();

        Scenario sc = new Scenario();
        FactData person = new FactData();
        person.name = "p";
        person.type = "Person";

        sc.fixtures.add( person );
        sc.fixtures.add( new ExecutionTrace() );
        VerifyRuleFired vr = new VerifyRuleFired( "MyGoodRule",
                                                  1,
                                                  null );
        sc.fixtures.add( vr );

        ScenarioRunResult res = null;
        try {
            res = impl.runScenario( pkg.getName(),
                                    sc ).result;
        } catch ( ClassFormatError e ) {
            fail( "Probably failed when loading a source file instead of class file. " + e );
        }

        assertNull( res.getErrors() );
        assertNotNull( res.getScenario() );
        assertTrue( vr.wasSuccessful() );

        res = impl.runScenario( pkg.getName(),
                                sc ).result;

        assertNull( res.getErrors() );
        assertNotNull( res.getScenario() );
        assertTrue( vr.wasSuccessful() );

        ServiceImplementation.ruleBaseCache.clear();

        res = impl.runScenario( pkg.getName(),
                                sc ).result;

        assertNull( res.getErrors() );
        assertNotNull( res.getScenario() );
        assertTrue( vr.wasSuccessful() );

    }

    @Test
    @Ignore
    public void testRunPackageScenarios() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        PackageItem pkg = repo.createPackage( "testScenarioRunBulk",
                                              "" );
        ServiceImplementation.updateDroolsHeader( "import org.drools.Person",
                                                  pkg );
        AssetItem rule1 = pkg.addAsset( "rule_1",
                                        "" );
        rule1.updateFormat( AssetFormats.DRL );
        rule1.updateContent( "rule 'rule1' \n when \np : Person() \n then \np.setAge(42); \n end" );
        rule1.checkin( "" );

        //this rule will never fire
        AssetItem rule2 = pkg.addAsset( "rule_2",
                                        "" );
        rule2.updateFormat( AssetFormats.DRL );
        rule2.updateContent( "rule 'rule2' \n when \np : Person(age == 1000) \n then \np.setAge(46); \n end" );
        rule2.checkin( "" );
        repo.save();

        //first, the green scenario
        Scenario sc = new Scenario();
        FactData person = new FactData();
        person.name = "p";
        person.type = "Person";
        person.fieldData.add( new FieldData( "age",
                                             "40" ) );
        person.fieldData.add( new FieldData( "name",
                                             "michael" ) );

        sc.fixtures.add( person );
        sc.fixtures.add( new ExecutionTrace() );
        VerifyRuleFired vr = new VerifyRuleFired( "rule1",
                                                  1,
                                                  null );
        sc.fixtures.add( vr );

        VerifyFact vf = new VerifyFact();
        vf.name = "p";
        vf.fieldValues.add( new VerifyField( "name",
                                             "michael",
                                             "==" ) );
        vf.fieldValues.add( new VerifyField( "age",
                                             "42",
                                             "==" ) );
        sc.fixtures.add( vf );

        AssetItem scenario1 = pkg.addAsset( "scen1",
                                            "" );
        scenario1.updateFormat( AssetFormats.TEST_SCENARIO );
        scenario1.updateContent( ScenarioXMLPersistence.getInstance().marshal( sc ) );
        scenario1.checkin( "" );

        //now the bad scenario
        sc = new Scenario();
        person = new FactData();
        person.name = "p";
        person.type = "Person";
        person.fieldData.add( new FieldData( "age",
                                             "40" ) );
        person.fieldData.add( new FieldData( "name",
                                             "michael" ) );

        sc.fixtures.add( person );
        sc.fixtures.add( new ExecutionTrace() );
        vr = new VerifyRuleFired( "rule2",
                                  1,
                                  null );
        sc.fixtures.add( vr );

        AssetItem scenario2 = pkg.addAsset( "scen2",
                                            "" );
        scenario2.updateFormat( AssetFormats.TEST_SCENARIO );
        scenario2.updateContent( ScenarioXMLPersistence.getInstance().marshal( sc ) );
        scenario2.checkin( "" );

        AssetItem scenario3 = pkg.addAsset( "scenBOGUS",
                                            "" );
        scenario3.updateFormat( AssetFormats.TEST_SCENARIO );
        scenario3.updateContent( "SOME RUBBISH" );
        scenario3.updateDisabled( true );
        scenario3.checkin( "" );

        //love you
        long time = System.currentTimeMillis();
        BulkTestRunResult result = impl.runScenariosInPackage( pkg.getUUID() );
        System.err.println( "Time taken for runScenariosInPackage " + (System.currentTimeMillis() - time) );
        assertNull( result.result );

        assertEquals( 50,
                      result.percentCovered );
        assertEquals( 1,
                      result.rulesNotCovered.length );
        assertEquals( "rule2",
                      result.rulesNotCovered[0] );

        assertEquals( 2,
                      result.results.length );

        ScenarioResultSummary s1 = result.results[0];
        assertEquals( 0,
                      s1.failures );
        assertEquals( 3,
                      s1.total );
        assertEquals( scenario1.getUUID(),
                      s1.uuid );
        assertEquals( scenario1.getName(),
                      s1.scenarioName );

        ScenarioResultSummary s2 = result.results[1];
        assertEquals( 1,
                      s2.failures );
        assertEquals( 1,
                      s2.total );
        assertEquals( scenario2.getUUID(),
                      s2.uuid );
        assertEquals( scenario2.getName(),
                      s2.scenarioName );
    }

    @Test
    @Ignore
    public void testListFactTypesAvailableInPackage() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;

        PackageItem pkg = repo.createPackage( "testAvailableTypes",
                                              "" );
        AssetItem model = pkg.addAsset( "MyModel",
                                        "" );
        model.updateFormat( AssetFormats.MODEL );
        model.updateBinaryContentAttachment( this.getClass().getResourceAsStream( "/billasurf.jar" ) );
        model.checkin( "" );
        repo.save();

        String[] s = impl.listTypesInPackage( pkg.getUUID() );
        assertNotNull( s );
        assertEquals( 2,
                      s.length );
        assertEquals( "com.billasurf.Person",
                      s[0] );
        assertEquals( "com.billasurf.Board",
                      s[1] );

        AssetItem asset = pkg.addAsset( "declaretTypes",
                                        "" );
        asset.updateFormat( AssetFormats.DRL_MODEL );
        asset.updateContent( "declare Whee\n name: String \n end" );
        asset.checkin( "" );

        s = impl.listTypesInPackage( pkg.getUUID() );
        assertEquals( 3,
                      s.length );
        assertEquals( "Whee",
                      s[2] );

    }

    @Test
    @Ignore
    public void testGuidedDTExecute() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        RulesRepository repo = impl.repository;
        impl.createCategory( "/",
                             "decisiontables",
                             "" );

        PackageItem pkg = repo.createPackage( "testGuidedDTCompile",
                                              "" );
        ServiceImplementation.updateDroolsHeader( "import org.drools.Person",
                                                  pkg );
        AssetItem rule1 = pkg.addAsset( "rule_1",
                                        "" );
        rule1.updateFormat( AssetFormats.DRL );
        rule1.updateContent( "rule 'rule1' \n when \np : Person() \n then \np.setAge(42); \n end" );
        rule1.checkin( "" );
        repo.save();

        GuidedDecisionTable dt = new GuidedDecisionTable();
        ConditionCol col = new ConditionCol();
        col.setBoundName( "p" );
        col.setConstraintValueType( BaseSingleFieldConstraint.TYPE_LITERAL );
        col.setFactField( "hair" );
        col.setFactType( "Person" );
        col.setOperator( "==" );
        dt.getConditionCols().add( col );

        ActionSetFieldCol ac = new ActionSetFieldCol();
        ac.setBoundName( "p" );
        ac.setFactField( "likes" );
        ac.setType( SuggestionCompletionEngine.TYPE_STRING );
        dt.getActionCols().add( ac );

        dt.setData( new String[][]{new String[]{"1", "descrip", "pink", "cheese"}} );

        String uid = impl.createNewRule( "decTable",
                                         "",
                                         "decisiontables",
                                         pkg.getName(),
                                         AssetFormats.DECISION_TABLE_GUIDED );
        RuleAsset ass = impl.loadRuleAsset( uid );
        ass.content = dt;
        impl.checkinVersion( ass );

        BuilderResult results = impl.buildPackage( pkg.getUUID(),
                                                   true );
        assertNull( results );

        pkg = repo.loadPackage( "testGuidedDTCompile" );
        byte[] binPackage = pkg.getCompiledPackageBytes();

        assertNotNull( binPackage );

        Package binPkg = (Package) DroolsStreamUtils.streamIn( binPackage );

        assertEquals( 2,
                      binPkg.getRules().length );

        assertNotNull( binPkg );
        assertTrue( binPkg.isValid() );

        Person p = new Person();

        p.setHair( "pink" );

        BinaryRuleBaseLoader loader = new BinaryRuleBaseLoader();
        loader.addPackage( new ByteArrayInputStream( binPackage ) );
        RuleBase rb = loader.getRuleBase();

        StatelessSession sess = rb.newStatelessSession();
        sess.execute( p );
        assertEquals( 42,
                      p.getAge() );
        assertEquals( "cheese",
                      p.getLikes() );
    }

    @Test
    @Ignore
    public void testPackageNameSorting() {
        PackageConfigData c1 = new PackageConfigData( "org.foo" );
        PackageConfigData c2 = new PackageConfigData( "org.foo.bar" );

        List<PackageConfigData> ls = new ArrayList<PackageConfigData>();
        ls.add( c2 );
        ls.add( c1 );
        ServiceImplementation serv = new ServiceImplementation();
        serv.sortPackages( ls );
        assertEquals( c1,
                      ls.get( 0 ) );
        assertEquals( c2,
                      ls.get( 1 ) );
    }

    @Test
    @Ignore
    public void testLoadDropDown() throws Exception {
        ServiceImplementation serv = new ServiceImplementation();
        String[] pairs = new String[]{"f1=x", "f2=2"};
        String expression = "['@{f1}', '@{f2}']";
        String[] r = serv.loadDropDownExpression( pairs,
                                                  expression );
        assertEquals( 2,
                      r.length );

        assertEquals( "x",
                      r[0] );
        assertEquals( "2",
                      r[1] );

    }

    @Test
    @Ignore
    public void testLoadDropDownNoValuePairs() throws Exception {
        ServiceImplementation serv = new ServiceImplementation();
        String[] pairs = new String[]{null};
        String expression = "['@{f1}', '@{f2}']";
        String[] r = serv.loadDropDownExpression( pairs,
                                                  expression );

        assertEquals( 0,
                      r.length );

    }

    @Test
    @Ignore
    public void testListUserPermisisons() throws Exception {
        ServiceImplementation serv = getServiceImplementation();
        Map<String, List<String>> r = serv.listUserPermissions();
        assertNotNull( r );
    }

    @Test
    @Ignore
    public void testManageUserPermissions() throws Exception {
        ServiceImplementation serv = getServiceImplementation();
        Map<String, List<String>> perms = new HashMap<String, List<String>>();
        serv.updateUserPermissions( "googoo",
                                    perms );

        Map<String, List<String>> perms_ = serv.retrieveUserPermissions( "googoo" );
        assertEquals( 0,
                      perms_.size() );
    }

    @Test
    @Ignore
    //Ignore
    public void testImportSampleRepository() throws Exception {
        ServiceImplementation serv = getServiceImplementation();
        serv.installSampleRepository();
        PackageConfigData[] cfgs = serv.listPackages();
        assertEquals( 2,
                      cfgs.length );
        assertTrue( cfgs[0].name.equals( "mortgages" ) || cfgs[1].name.equals( "mortgages" ) );
        String puuid = (cfgs[0].name.equals( "mortgages" )) ? cfgs[0].uuid : cfgs[1].uuid;
        BulkTestRunResult res = serv.runScenariosInPackage( puuid );
        assertEquals( null,
                      res.result );
    }

    //GUVNOR-296
    @Ignore
    @Test
    public void testHistoryAfterReImportSampleRepository() throws Exception {
        ServiceImplementation impl = getServiceImplementation();

        //Import sample, do a sanity check, make sure sample is installed correctly
        impl.installSampleRepository();
        PackageConfigData[] cfgs = impl.listPackages();
        assertEquals( 2,
                      cfgs.length );
        assertTrue( cfgs[0].name.equals( "mortgages" ) || cfgs[1].name.equals( "mortgages" ) );

        TableDataResult res = impl.quickFindAsset( "Bankruptcy history",
                                                   false,
                                                   0,
                                                   20 );
        assertEquals( 1,
                      res.data.length );
        String uuid = res.data[0].id;

        // create version 4.
        RuleAsset ai = impl.loadRuleAsset( uuid );
        ai.metaData.checkinComment = "version 4";
        impl.checkinVersion( ai );

        // create version 5.
        ai = impl.loadRuleAsset( uuid );
        ai.metaData.checkinComment = "version 5";
        impl.checkinVersion( ai );

        System.out.println( "old uuid: " + uuid );

        //NOTE: Have not figured out the reason, but if we dont create a random package here, 
        //we will get an InvalidItemStateException during impl.installSampleRepository()
        impl.repository.createPackage( "testHistoryAfterReImportSampleRepository",
                                       "desc" );

        TableDataResult result = impl.loadAssetHistory( uuid );
        assertNotNull( result );
        TableDataRow[] rows = result.data;
        assertEquals( 1,
                      rows.length );

        //Import sample again
        impl.installSampleRepository();
        res = impl.quickFindAsset( "Bankruptcy history",
                                   false,
                                   0,
                                   20 );
        assertEquals( 1,
                      res.data.length );
        String newUuid = res.data[0].id;

        //Now verify history, should be zero.
        result = impl.loadAssetHistory( newUuid );
        System.out.println( "new uuid: " + newUuid );

        assertNotNull( result );
        rows = result.data;
        assertEquals( 1,
                      rows.length );
    }

    @Test
    @Ignore
    public void testAddCategories() throws Exception {
        ServiceImplementation impl = getServiceImplementation();
        impl.repository.createPackage( "testAddCategoriesPackage",
                                       "desc" );
        impl.createCategory( "",
                             "testAddCategoriesCat1",
                             "this is a cat" );
        impl.createCategory( "",
                             "testAddCategoriesCat2",
                             "this is a cat" );

        String uuid = impl.createNewRule( "testCreateNewRuleName",
                                          "an initial desc",
                                          "testAddCategoriesCat1",
                                          "testAddCategoriesPackage",
                                          AssetFormats.DSL_TEMPLATE_RULE );

        AssetItem dtItem = impl.repository.loadAssetByUUID( uuid );
        dtItem.addCategory( "testAddCategoriesCat1" );
        impl.repository.save();

        AssetItem dtItem1 = impl.repository.loadAssetByUUID( uuid );
        assertEquals( 1,
                      dtItem1.getCategories().size() );
        assertTrue( dtItem1.getCategorySummary().contains( "testAddCategoriesCat1" ) );

        AssetItem dtItem2 = impl.repository.loadAssetByUUID( uuid );
        dtItem2.addCategory( "testAddCategoriesCat2" );
        impl.repository.save();

        AssetItem dtItem3 = impl.repository.loadAssetByUUID( uuid );
        assertEquals( 2,
                      dtItem3.getCategories().size() );
        assertTrue( dtItem3.getCategorySummary().contains( "testAddCategoriesCat2" ) );
    }

    @Test
    @Ignore
    public void testSnapshotDiff() throws Exception {
        RepositoryService impl = getServiceImplementation();

        // Lets make a package and a rule into tit.
        impl.createCategory( "/",
                             "snapshotDiffTesting",
                             "y" );
        String packageUuid = impl.createPackage( "testSnapshotDiff",
                                                 "d" );

        assertNotNull( packageUuid );

        // Create two rules
        String archiveRuleUuid = impl.createNewRule( "testRuleArchived",
                                                     "",
                                                     "snapshotDiffTesting",
                                                     "testSnapshotDiff",
                                                     AssetFormats.DRL );
        String modifiedRuleUuid = impl.createNewRule( "testRuleModified",
                                                      "",
                                                      "snapshotDiffTesting",
                                                      "testSnapshotDiff",
                                                      AssetFormats.DRL );
        String deletedRuleUuid = impl.createNewRule( "testRuleDeleted",
                                                     "",
                                                     "snapshotDiffTesting",
                                                     "testSnapshotDiff",
                                                     AssetFormats.DRL );
        String restoredRuleUuid = impl.createNewRule( "testRuleRestored",
                                                      "",
                                                      "snapshotDiffTesting",
                                                      "testSnapshotDiff",
                                                      AssetFormats.DRL );
        @SuppressWarnings("unused")
        String noChangesRuleUuid = impl.createNewRule( "testRuleNoChanges",
                                                       "",
                                                       "snapshotDiffTesting",
                                                       "testSnapshotDiff",
                                                       AssetFormats.DRL );

        impl.archiveAsset( restoredRuleUuid );

        // Create a snapshot called FIRST for the package
        impl.createPackageSnapshot( "testSnapshotDiff",
                                    "FIRST",
                                    false,
                                    "ya" );
        assertEquals( 1,
                      impl.listSnapshots( "testSnapshotDiff" ).length );
        assertEquals( 4,
                      impl.listRulesInPackage( "testSnapshotDiff" ).length );

        // Change the rule, archive one, delete one and create a new one
        RuleAsset asset = impl.loadRuleAsset( modifiedRuleUuid );
        String uuid = impl.checkinVersion( asset );
        assertNotNull( uuid );

        impl.removeAsset( deletedRuleUuid );

        impl.archiveAsset( archiveRuleUuid );

        @SuppressWarnings("unused")
        String addedRuleUuid = impl.createNewRule( "testRuleAdded",
                                                   "",
                                                   "snapshotDiffTesting",
                                                   "testSnapshotDiff",
                                                   AssetFormats.DRL );

        impl.unArchiveAsset( restoredRuleUuid );

        // Create a snapshot called SECOND for the package
        impl.createPackageSnapshot( "testSnapshotDiff",
                                    "SECOND",
                                    false,
                                    "we" );
        assertEquals( 2,
                      impl.listSnapshots( "testSnapshotDiff" ).length );
        assertEquals( 4,
                      impl.listRulesInPackage( "testSnapshotDiff" ).length );

        // Compare the snapshots
        SnapshotDiffs diffs = impl.compareSnapshots( "testSnapshotDiff",
                                                     "FIRST",
                                                     "SECOND" );
        assertEquals( "FIRST",
                      diffs.leftName );
        assertEquals( "SECOND",
                      diffs.rightName );

        SnapshotDiff[] list = diffs.diffs;
        assertNotNull( list );
        assertEquals( 5,
                      list.length );

        for ( int i = 0; i < list.length; i++ ) {
            SnapshotDiff diff = list[i];
            if ( diff.name.equals( "testRuleArchived" ) ) {
                assertEquals( SnapshotDiff.TYPE_ARCHIVED,
                              diff.diffType );
                assertNotNull( diff.leftUuid );
                assertNotNull( diff.rightUuid );
            } else if ( diff.name.equals( "testRuleModified" ) ) {
                assertEquals( SnapshotDiff.TYPE_UPDATED,
                              diff.diffType );
                assertNotNull( diff.leftUuid );
                assertNotNull( diff.rightUuid );
            } else if ( diff.name.equals( "testRuleAdded" ) ) {
                assertEquals( SnapshotDiff.TYPE_ADDED,
                              diff.diffType );
                assertNull( diff.leftUuid );
                assertNotNull( diff.rightUuid );
            } else if ( diff.name.equals( "testRuleDeleted" ) ) {
                assertEquals( SnapshotDiff.TYPE_DELETED,
                              diff.diffType );
                assertNotNull( diff.leftUuid );
                assertNull( diff.rightUuid );
            } else if ( diff.name.equals( "testRuleRestored" ) ) {
                assertEquals( SnapshotDiff.TYPE_RESTORED,
                              diff.diffType );
                assertNotNull( diff.leftUuid );
                assertNotNull( diff.rightUuid );
            } else {
                fail( "Diff not expected." );
            }
        }
    }

}
