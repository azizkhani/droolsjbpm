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

package org.drools.bpmn2;

import org.drools.bpmn2.xml.BPMNSemanticModule;
import org.drools.compiler.BPMN2ProcessProvider;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;

public class BPMN2ProcessProviderImpl implements BPMN2ProcessProvider {

    public void configurePackageBuilder(PackageBuilder packageBuilder) {
        PackageBuilderConfiguration conf = packageBuilder.getPackageBuilderConfiguration();
        if (conf.getSemanticModules().getSemanticModule(BPMNSemanticModule.BPMN2_URI) == null) {
        	conf.addSemanticModule(new BPMNSemanticModule());
        	conf.addSemanticModule(new org.drools.bpmn2.legacy.beta1.BPMNSemanticModule());
        	conf.addSemanticModule(new org.drools.bpmn2.legacy.beta1.BPMN2SemanticModule());
        	conf.addSemanticModule(new org.drools.bpmn2.legacy.beta1.BPMNDISemanticModule());
        }
    }

}
