package org.drools.lang.dl.descr;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DLDescrFactory {

	private static DLDescrFactory instance = new DLDescrFactory();
	
	public static DLDescrFactory instance() {
		return instance;
	}
	
	private DLDescrFactory() {
		
	}
	
	private static int N = 0;
	
	public DL_OntologyDescr buildOntologyDescr() {
		return new DL_OntologyDescr();
	}
	
	
	public DL_IriDescr buildIriDescr(String nameSpace, String localName, Map<String,String> nsMap) {
		DL_IriDescr descr =  new DL_IriDescr(nameSpace,localName,nsMap);
		descr.setId(nextId());
		return descr;
	}
	
	public DL_IriDescr buildIriDescr(String iri) {
		DL_IriDescr descr = new DL_IriDescr(iri);
		descr.setId(nextId());
		return descr;
	}
	
	public DL_AnnotationDescr buildAnnotationDescr() {
		DL_AnnotationDescr descr = new DL_AnnotationDescr();
		descr.setId(nextId());
		return descr;
	}
	
	public DL_ClassDescr buildClassDescr() {
		DL_ClassDescr descr = new DL_ClassDescr();
		descr.setId(nextId());
		return descr;
	}
	
	public DL_DatatypeDescr buildDatatypeDescr() {
		DL_DatatypeDescr descr = new DL_DatatypeDescr();
		descr.setId(nextId());
		return descr;
	}
	
	public DL_ObjectPropertyDescr buildObjectPropertyDescr() {
		DL_ObjectPropertyDescr descr = new DL_ObjectPropertyDescr();
		descr.setId(nextId());
		return descr;
	}
	
	public DL_DataPropertyDescr buildDataPropertyDescr() {
		DL_DataPropertyDescr descr = new DL_DataPropertyDescr();
		descr.setId(nextId());
		return descr;
	}
	
	public DL_AnnotationPropertyDescr buildAnnotationPropertyDescr() {
		DL_AnnotationPropertyDescr descr = new DL_AnnotationPropertyDescr();
		descr.setId(nextId());
		return descr;
	}
	
	public DL_IndividualDescr buildIndividualDescr() {
		DL_IndividualDescr descr = new DL_IndividualDescr();
		descr.setId(nextId());
		return descr;
	}
	
	public DL_RelationDescr buildGenericRelationDescr(DL_IriDescr iri, boolean inverse) {
		DL_RelationDescr descr = new DL_RelationDescr();
			descr.setIri(iri);
			descr.setInverse(inverse);
			descr.setId(nextId());
		return descr;
	}
	

	
	public DL_DefinitionDescr buildDefinitionDescr(DL_aBaseDescr root) {
		DL_DefinitionDescr descr = new DL_DefinitionDescr(root);
		descr.setId(nextId());
		return descr;
	}
	
	
	public DL_DefinitionDescr buildDefinitionDescr() {
		DL_DefinitionDescr descr = new DL_DefinitionDescr();
		descr.setId(nextId());
		return descr;
	}
	
	

	public Object buildFactDescr(DL_RelationDescr rel, Object tgt, boolean negated) {
		DL_FactDescr descr = new DL_FactDescr();
			descr.setRel(rel);
			descr.setObject(tgt);
			descr.setNegated(negated);
			descr.setId(nextId());
		return descr;
	}
	
	
	public DL_aBaseDescr buildImplies(List<DL_aBaseDescr> children) {
		DL_aCompositeDescr impl = new DL_ImpliesDescr();
			impl.addChildren(children);
		impl.setId(nextId());
		return impl;
	}
	
	public DL_aBaseDescr buildAnd(List<DL_aBaseDescr> children) {
		DL_aCompositeDescr et = new DL_AndDescr();
			et.addChildren(children);
		et.setId(nextId());
		return et;
	}
	
	public DL_aBaseDescr buildOr(List<DL_aBaseDescr> children) {
		DL_aCompositeDescr vel = new DL_OrDescr();
			vel.addChildren(children);	
		vel.setId(nextId());
		return vel;
	}
	
	public DL_aBaseDescr buildNeg(DL_aBaseDescr child) {
		if (child instanceof DL_NegDescr) {
			return ((DL_NegDescr) child).getChildren().get(0); 
		} else {
			child.setNegated(! child.isNegated());
			return child;
		}
	}
	
	
	public DL_aBaseDescr buildType(String klass) {
		DL_TypeDescr type = new DL_TypeDescr();
			type.setType(klass);
		type.setId(nextId());
		return type;
	}
	
	
	public DL_aBaseDescr buildProperty(String klass) {
		DL_PropertyDescr prop = new DL_PropertyDescr();
			prop.setType(klass);
		prop.setId(nextId());
		return prop;
	}
	
	
	public DL_aBaseDescr buildExists(DL_aBaseDescr prop, DL_aBaseDescr klass) {
		DL_aCompositeDescr exist = new DL_ExistsDescr();
			exist.addChild(prop);
			exist.addChild(klass);
		exist.setId(nextId());
		return exist;
	}
	
	
	public DL_aBaseDescr buildForall(DL_aBaseDescr prop, DL_aBaseDescr klass) {
		DL_aCompositeDescr foral = new DL_ForallDescr();
			foral.addChild(prop);
			foral.addChild(klass);
		foral.setId(nextId());
		return foral;
	}
	
	
	public DL_aBaseDescr buildValue(DL_aBaseDescr prop, Object val) {
		DL_ValueDescr value = new DL_ValueDescr();
			value.addChild(prop);
			if (val != null) 
				value.setVal(val);
			else
				value.addChild(new DL_SelfDescr());
		value.setId(nextId());
		return value;
	}
	
	
	public DL_aBaseDescr buildCount(DL_aBaseDescr prop, String min, String max, DL_aBaseDescr klass) {
		DL_CountDescr count = new DL_CountDescr();
			count.addChild(prop);
			if (klass != null) count.addChild(klass);
			if (min != null) {
				try {
					count.setMin(Integer.valueOf(min));
				} catch (NumberFormatException nfe) { }
			}
			if (max != null) {
				try {
					count.setMax(Integer.valueOf(max));
				} catch (NumberFormatException nfe) { }
			}
			
		count.setId(nextId());
		return count;
	}
	
		
	public DL_aBaseDescr buildEnumTypeDescr(Collection<Object> values) {
		DL_EnumTypeDescr descr = new DL_EnumTypeDescr();
			descr.addAll(values);
			descr.setId(nextId());
		return descr;
	}
	
	public DL_RestrictedTypeDescr buildRestrictedTypeDescr(DL_aBaseDescr type, DL_aBaseDescr facets) {
		DL_RestrictedTypeDescr descr = new DL_RestrictedTypeDescr();
			if (type instanceof DL_TypeDescr)
				descr.setType(((DL_TypeDescr) type).getType());
			if (facets instanceof DL_AndDescr)
				descr.addChildren(((DL_AndDescr) facets).getChildren());
		descr.setId(nextId());
		return descr;
	}
	
	public DL_FacetDescr buildFacetDescr(String op, Object val) {
		DL_FacetDescr facet = new DL_FacetDescr();
			facet.setOp(op);
			facet.setVal(val);
		facet.setId(nextId());	
		return facet;
	}
	
	public DL_aBaseDescr buildRootType(String klass, DL_aBaseDescr def) {
		DL_RootNode root = new DL_RootNode();
			root.setType(klass);
			root.addChild(def);
		root.setId(nextId());
		return root;
	}
	
	
	public DL_TypeDescr buildRandomTypeDescr() {
		int id = nextId();
		String type = "klass_"+id;
		DL_TypeDescr td = new DL_TypeDescr();
			td.setType(type);
		td.setId(id);	
		return td;
	}
	
	public static int nextId() {
		return N++;
	}


	
	
	
}
