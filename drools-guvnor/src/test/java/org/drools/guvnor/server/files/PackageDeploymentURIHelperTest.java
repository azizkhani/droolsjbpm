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

package org.drools.guvnor.server.files;
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


import org.junit.Test;

import static org.junit.Assert.*;

public class PackageDeploymentURIHelperTest {

    @Test
    public void testGetPackageToExport() throws Exception {
        String uri = "/org.drools.guvnor.Guvnor/package/boo/ya+man";

        PackageDeploymentURIHelper helper = new PackageDeploymentURIHelper(uri);

        assertEquals( "ya man", helper.getVersion() );
        assertEquals( "boo", helper.getPackageName() );
        assertFalse(helper.isLatest());

        helper = new PackageDeploymentURIHelper("/asset/bar/LATEST");
        assertTrue(helper.isLatest());
        assertEquals("bar", helper.getPackageName());
        assertFalse(helper.isAsset());
    }

    @Test
    public void testGetPackageWithDRL() throws Exception {
    	String uri = "/org.drools.guvnor.Guvnor/package/boo/ya+man.drl";
        PackageDeploymentURIHelper helper = new PackageDeploymentURIHelper(uri);

        assertTrue(helper.isSource());
        assertFalse(helper.isDocumentation());
        assertEquals( "ya man", helper.getVersion() );
        assertEquals( "boo", helper.getPackageName() );
        assertFalse(helper.isLatest());
        assertFalse(helper.isAsset());
    }

    @Test
    public void testGetAssetDRL() throws Exception {
    	String uri = "/org.drools.guvnor.Guvnor/package/packName/LATEST/assetName.drl";
        PackageDeploymentURIHelper helper = new PackageDeploymentURIHelper(uri);
        assertTrue(helper.isSource());
        assertFalse(helper.isDocumentation());
        assertEquals("LATEST", helper.getVersion());
        assertEquals("packName", helper.getPackageName());
        assertEquals("assetName", helper.getAssetName());
        assertTrue(helper.isAsset());
    }

    @Test
    public void testGetDocumentation() throws Exception {
        String uri = "/org.drools.guvnor.Guvnor/package/packName/LATEST/documentation.pdf";
        PackageDeploymentURIHelper helper = new PackageDeploymentURIHelper(uri);
        assertFalse(helper.isSource());
        assertTrue(helper.isDocumentation());
        assertEquals("LATEST", helper.getVersion());
        assertEquals("packName", helper.getPackageName());
        assertEquals("documentation", helper.getAssetName());
        assertTrue(helper.isAsset());
    }
    
    @Test
	/*
	 * Here is the use case: when executing a process with a given process
	 * definition id, we want to look up an image of that process. The naming
	 * convention for that image is {processDefinitionId}.png.
	 */
    public void testGetPNG() throws Exception {
        String uri = "/org.drools.guvnor.Guvnor/package/packName/LATEST/process.png";
        PackageDeploymentURIHelper helper = new PackageDeploymentURIHelper(uri);
        assertTrue(helper.isAsset());
        assertEquals("LATEST", helper.getVersion());
        assertEquals("packName", helper.getPackageName());
        assertEquals("process", helper.getAssetName());
        assertTrue(helper.isPng());
    }

}
