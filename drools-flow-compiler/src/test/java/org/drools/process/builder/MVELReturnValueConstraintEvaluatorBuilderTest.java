package org.drools.process.builder;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.common.InternalKnowledgeRuntime;
import org.drools.compiler.DialectCompiletimeRegistry;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.compiler.ReturnValueDescr;
import org.drools.definition.KnowledgePackage;
import org.drools.definitions.impl.KnowledgePackageImp;
import org.drools.process.builder.dialect.mvel.MVELReturnValueEvaluatorBuilder;
import org.drools.process.instance.impl.MVELReturnValueEvaluator;
import org.drools.process.instance.impl.ReturnValueConstraintEvaluator;
import org.drools.rule.Package;
import org.drools.rule.builder.PackageBuildContext;
import org.drools.rule.builder.dialect.mvel.MVELDialect;
import org.drools.ruleflow.instance.RuleFlowProcessInstance;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.workflow.instance.node.SplitInstance;

public class MVELReturnValueConstraintEvaluatorBuilderTest extends TestCase {

    public void setUp() {
    }

    public void testSimpleReturnValueConstraintEvaluator() throws Exception {
        final Package pkg = new Package( "pkg1" );

        ReturnValueDescr descr = new ReturnValueDescr();
        descr.setText( "return value" );

        PackageBuilder pkgBuilder = new PackageBuilder( pkg );
        final PackageBuilderConfiguration conf = pkgBuilder.getPackageBuilderConfiguration();
        DialectCompiletimeRegistry dialectRegistry = pkgBuilder.getPackageRegistry( pkg.getName() ).getDialectCompiletimeRegistry();
        MVELDialect mvelDialect = (MVELDialect) dialectRegistry.getDialect( "mvel" );

        PackageBuildContext context = new PackageBuildContext();
        context.init( pkgBuilder,
                      pkg,
                      null,
                      dialectRegistry,
                      mvelDialect,
                      null );

        pkgBuilder.addPackageFromDrl( new StringReader( "package pkg1;\nglobal Boolean value;" ) );

        ReturnValueConstraintEvaluator node = new ReturnValueConstraintEvaluator();

        final MVELReturnValueEvaluatorBuilder builder = new MVELReturnValueEvaluatorBuilder();
        builder.build( context,
                       node,
                       descr,
                       null );

        final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        List<KnowledgePackage> packages = new ArrayList<KnowledgePackage>();
        packages.add( new KnowledgePackageImp(pkgBuilder.getPackage()) );
        kbase.addKnowledgePackages( packages );
        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        ksession.setGlobal( "value", true );        

        RuleFlowProcessInstance processInstance = new RuleFlowProcessInstance();
        processInstance.setKnowledgeRuntime( (InternalKnowledgeRuntime) ksession );

        SplitInstance splitInstance = new SplitInstance();
        splitInstance.setProcessInstance( processInstance );
        
        ( (MVELReturnValueEvaluator) node.getReturnValueEvaluator()).compile( Thread.currentThread().getContextClassLoader() );

        assertTrue( node.evaluate( splitInstance,
                                   null,
                                   null ) );
        
        ksession.setGlobal( "value", false );     
        
        assertFalse( node.evaluate( splitInstance,
                                   null,
                                   null ) );        
    }

}
