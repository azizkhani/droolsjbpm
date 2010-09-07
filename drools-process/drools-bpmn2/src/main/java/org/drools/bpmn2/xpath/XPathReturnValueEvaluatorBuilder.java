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

package org.drools.bpmn2.xpath;

import org.drools.compiler.ReturnValueDescr;
import org.drools.process.builder.ReturnValueEvaluatorBuilder;
import org.drools.process.core.ContextResolver;
import org.drools.process.instance.impl.ReturnValueConstraintEvaluator;
import org.drools.rule.builder.PackageBuildContext;

public class XPathReturnValueEvaluatorBuilder implements ReturnValueEvaluatorBuilder {

    public void build(PackageBuildContext context,
            ReturnValueConstraintEvaluator returnValueConstraintEvaluator,
            ReturnValueDescr returnValueDescr, ContextResolver contextResolver) {
        XPathReturnValueEvaluator evaluator = new XPathReturnValueEvaluator();
        String xpathString = returnValueDescr.getText();
        evaluator.setExpression(xpathString);
        returnValueConstraintEvaluator.setEvaluator(evaluator);
    }

}
