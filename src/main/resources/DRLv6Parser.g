parser grammar DRLv6Parser; 
 
options {
  language = Java;
  output = AST;
  tokenVocab = DRLv6Lexer;
}
      
     
tokens {
  VT_COMPILATION_UNIT;
  VT_PACKAGE_ID;
  VT_PACKAGE;
  
  VT_GLOBAL_ID;
  VT_DATA_TYPE;
  VT_DIM_SIZE;
  
  VT_IMPORT;
  VT_FUNCTION_IMPORT;
  VT_STAR;  
  VT_FUNCTION;
  VT_FUNCTION_ID;
  VT_PARAM;
  VT_PARAM_LIST;
  VT_NAME;

  VT_TEMPLATE;
  VT_TEMPLATE_ID;
  VT_SLOT;
  VT_SLOT_ID;
  
  VT_TYPE_DECLARE;
  VT_TYPE_DECLARE_ID;
  VT_EXTENDS;
  VT_DL_DEFINITION;
  VT_DL_TYPE;
  VT_FIELD;
  
  VT_RULE_ID; 
  VT_ATTRIBUTES;
  VT_DIALECT;
  
  VT_LHS;
  VT_ARROW;
  
  VT_RHS;
  VT_THEN;
  VT_CLOSURE;
  
  VT_BRANCH;
  VT_BRANCH_DEFAULT;
  VT_BRANCH_LABEL;
  VT_NEG_BRANCH_LABEL;
  
  VT_RISING_EDGE;
  VT_FALLING_EDGE;
  
  VT_RHS_CHUNK;
  VT_CURLY_CHUNK;
  VT_SQUARE_CHUNK;
  VT_PAREN_CHUNK;
  
  VT_NEW_OBJ;
  VT_TYPE;
  VT_ARGS;
  VT_MSR;
  
  VT_AND_IMPLICIT;
  VT_IMPLIES; 
  VT_OR;
  VT_NEG;
  VT_XOR;
  VT_EQUIV;
  VT_AND;
  
  VT_HEDGE_VERY;
  VT_HEDGE_MOL;
  
  VT_EXISTS;
  VT_FORALL;
  VT_NEXISTS;
    
  VT_PATTERN;
  VT_NESTED_PATTERN;
  VT_ENABLED;
  VT_QUERY_PATTERN;  
  
  VT_POSITIONAL_VAR;
  VT_POSITIONAL_CONST;
  VT_POSITIONAL_INDEX;
  VT_POSITIONAL_SKIP;
  
  VT_BINDING;
  VT_ACCESSOR;
  VT_VERSION;
  VT_INDEXER;
  VT_INDEX_ALL;
  VT_METHOD;
  VT_EXPR;
  VT_OTHERWISE;
  
  VT_FILTER;
  VT_SET;
  
  VT_SEQUENCE;
  VT_TRAIL;
  VT_TRAIL_NODE;
  
  VT_LIST;
  VT_RANGE;
  
  VT_BEHAVIOR;
  VT_ENTRYPOINT;
  VT_ENTRYPOINT_ID;
  VT_FROM_SOURCE;
  VT_EXPRESSION_CHAIN;
  
  VT_ACCUMULATE_LEFT;
  VT_ACCUMULATE_RIGHT;
  VT_ACCUMULATE_ITERATION;
  VT_ACCUMULATE_FUNCTION;
  VT_ACC_ITER_INIT;
  VT_ACC_ITER_ACT;
  VT_ACC_ITER_REV;
  VT_ACC_ITER_RES;
  
  VT_COLLECT_LIST;
}
  
@parser::header {
  package org.drools.lang;
  
  import java.util.List;
  import java.util.LinkedList;
}

 

/**************************** SCOPE *******************************************/

compilation_unit
  : package_statement?
    statement*
    EOF
    -> ^(VT_COMPILATION_UNIT package_statement? statement*) 
  ;
  
  
/**************************** PACKAGE *******************************************/  
   

package_statement
  : PACKAGE
    package_id SEMICOLON?
    -> ^(VT_PACKAGE package_id)
  ;
  
package_id
  : fully_qualified_name 
  ;





/**************************** STATEMENTS *******************************************/

statement
  : function_import_statement   
  | import_statement  
  | global  
  | function
  // | template
  | type_declaration
  | rule
  | query
  //| rule_attribute    // why???
  ;


/**************************** GLOBAL + DATATYPE *******************************************/
global
  : GLOBAL data_type global_id SEMICOLON?
    -> ^(GLOBAL data_type global_id)
  ;

global_id
  : id=ID 
    -> VT_GLOBAL_ID[$id]
  ;
  
data_type
@init{
 int dim=0;
}
  : fully_qualified_name (dimension_definition {dim++;})*
    -> ^(VT_DATA_TYPE VT_DIM_SIZE[$start,""+dim] fully_qualified_name )
  ;
dimension_definition
  : LEFT_SQUARE RIGHT_SQUARE 
  ;


/**************************** IMPORT *******************************************/
import_statement
  : IMPORT import_name SEMICOLON?
    -> ^(VT_IMPORT import_name)
  ;

function_import_statement
  : IMPORT FUNCTION import_name SEMICOLON?
    -> ^(VT_FUNCTION_IMPORT import_name)
  ;

import_name 
  : fully_qualified_name star=DOT_STAR?
    -> {star==null}? fully_qualified_name 
    -> ^(VT_STAR fully_qualified_name)
  ;


/**************************** FUNCTION *******************************************/
function
  : FUNCTION data_type? function_id parameters curly_chunk
    -> ^(VT_FUNCTION data_type? function_id parameters curly_chunk)
  ;

function_id
  : id=ID
    -> VT_FUNCTION_ID[$id]
  ;

parameters
  : LEFT_PAREN 
      ( param_definition (COMMA param_definition)* )?
    RIGHT_PAREN 
    -> ^(VT_PARAM_LIST param_definition*)
  ;

param_definition
  : data_type argument
    -> ^(VT_PARAM data_type argument)
  ;

argument
  : ID //dimension_definition*
  ;



/**************************** TYPE DECLARATION *******************************************/



type_declaration
  : DECLARE  type_declare_id extend?
    type_declare_attributes?  
    dl_class_descr?
    decl_field*
    END SEMICOLON?
    -> ^(VT_TYPE_DECLARE type_declare_id extend? type_declare_attributes? dl_class_descr? decl_field*)
  ;
 
type_declare_id
  :   fully_qualified_name
     -> ^(VT_TYPE_DECLARE_ID fully_qualified_name)
  ;

type_declare_attributes
  :      
        (AT type_declare_attribute)+
          -> ^(VT_ATTRIBUTES type_declare_attribute+)
        | AT LEFT_SQUARE type_declare_attribute (COMMA type_declare_attribute)* RIGHT_SQUARE
          -> ^(VT_ATTRIBUTES type_declare_attribute+)                    
  ;

type_declare_attribute
  : 
            tda_role
        |   type_declare_att_semantic        
  ;
  
type_declare_att_semantic
  :         tda_namespace
        |   tda_disjoint
        |   tda_symmetric
        |   tda_transitive
        |   tda_inverse
  ;  
  
tda_role
  :     ROLE^ LEFT_PAREN! ( EVENT | ENTITY | PROPERTY | TYPE ) RIGHT_PAREN!
  ;
  
tda_namespace
  :     NAMESPACE^ LEFT_PAREN! ID EQUALS! STRING RIGHT_PAREN! 
  ;  
  
tda_disjoint
  :     DISJOINT^ LEFT_PAREN! ID RIGHT_PAREN!
  ;

tda_inverse
  :     INVERSE^ LEFT_PAREN! ID RIGHT_PAREN!
  ;  
  
tda_symmetric
  :     SYMMETRIC^
  ;
  
 tda_transitive
  :     TRANSITIVE^
  ;  
  
  
extend
  :   EXTEND fully_qualified_name
    -> ^(VT_EXTENDS fully_qualified_name)
  ;

decl_field
  : ID  
    decl_field_initialization? 
    COLON 
    data_type
    decl_field_attributes?
    -> ^(VT_FIELD decl_field_attributes? ID data_type decl_field_initialization? )
  ;

decl_field_initialization
  : EQUALS paren_chunk
  -> ^(EQUALS paren_chunk)
  ;

 
decl_field_attributes
  :
      AT LEFT_SQUARE
          decl_field_attribute (COMMA decl_field_attribute)*         
      RIGHT_SQUARE
      -> ^(VT_ATTRIBUTES decl_field_attribute+)      
  ;

decl_field_attribute
  :
    KEY 
  ;



dl_class_descr 
  :   AS dl_implies
      -> ^(VT_DL_DEFINITION dl_implies) 
  ;
  
dl_implies
  :   dl_or (impl=IMPLIES dl_or)?
  -> {impl==null}? dl_or
  -> ^(VT_IMPLIES dl_or dl_or)
  ;  
  
dl_or
  :   dl_and (or=OR dl_and)*
  -> {or==null}? dl_and
  ->  ^(VT_OR dl_and+)
  ;  
 
dl_and
  :   dl_atom (and=AND dl_atom)*
  -> {and==null}? dl_atom
  ->  ^(VT_AND dl_atom+)
  ;  
 
dl_atom
  :   NEG dl_atom
      -> ^(VT_NEG dl_atom)
  |   dl_type 
  |   dl_prop
  ;
  
  
dl_type
  :   LEFT_PAREN! dl_implies RIGHT_PAREN!
  |   dl_class
  ;
    
dl_class
  :   ID LEFT_PAREN RIGHT_PAREN 
      -> ^(VT_DL_TYPE ID)
  ;  
  
dl_prop
  :   ID (ALL | SOME)^ dl_type      
  ;  
  
  

/******************************************************* RULES *******************************************/





rule 
  : RULE rule_id parameters? (EXTEND rule_id)?    
    rule_metadata* 
    rule_attributes?
    rule_arrow?     
    when_part? 
    then_part
    -> ^(RULE rule_id 
              ^(VT_EXTENDS rule_id)?
              parameters? 
              rule_metadata* 
              rule_attributes? 
              rule_arrow?
              when_part? 
              then_part)
  ;
  


rule_id
  :   id=ID 
    -> VT_RULE_ID[$id]
  |   id=STRING
    -> VT_RULE_ID[$id]
  ;

rule_metadata
  : AT! ID^ paren_chunk?
  ;

rule_arrow
  : (
      (implication deduction?)
      | (deduction implication?)
    )
     -> ^(VT_ARROW implication? deduction?)
  ;

deduction  
    :   A_DEDUCTION^ operator_attributes
    ; 
  
  
implication
    :   A_IMPLICATION^ operator_attributes
    ;



/**************************** RULE ATTRIBS *******************************************/


rule_attributes
  : AT rule_attribute ( COMMA? AT rule_attribute)*
    -> ^(VT_ATTRIBUTES rule_attribute+)
  ;

rule_attribute
  : ra_salience 
  | ra_no_loop
  | ra_agenda_group  
  | ra_timer  
  | ra_activation_group 
  | ra_auto_focus 
  | ra_date_effective 
  | ra_date_expires 
  | ra_enabled 
  | ra_ruleflow_group 
  | ra_lock_on_active
  | ra_dialect 
  | ra_calendars
  | ra_defeats
  | ra_direction  
  ;

ra_date_effective
  : A_DATE_EFFECTIVE^ LEFT_PAREN! STRING RIGHT_PAREN!
  ;

ra_date_expires
  : A_DATE_EXPIRES^ LEFT_PAREN! STRING RIGHT_PAREN!
  ;
  
ra_enabled
  : A_ENABLED^ 
    LEFT_PAREN! BOOL RIGHT_PAREN!
  ; 

ra_salience
  : A_SALIENCE^
    LEFT_PAREN! INT RIGHT_PAREN!
  ;

ra_no_loop
  : A_NOLOOP^ (LEFT_PAREN! BOOL RIGHT_PAREN!)?
  ;

ra_auto_focus
  : A_AUTOFOCUS^ (LEFT_PAREN! BOOL RIGHT_PAREN!)?
  ; 
  
ra_activation_group
  : A_ACTGROUP^ (LEFT_PAREN! STRING RIGHT_PAREN!)
  ;

ra_ruleflow_group
  : A_RULEFLOWGROUP^ (LEFT_PAREN! STRING RIGHT_PAREN!)
  ;

ra_agenda_group
  : A_AGENDAGROUP^ (LEFT_PAREN! STRING RIGHT_PAREN!)
  ;

ra_timer
  : (A_DURATION^| A_TIMER^) 
    LEFT_PAREN! INT RIGHT_PAREN!
  ; 
  
ra_calendars
  : A_CALENDAR^ LEFT_PAREN! literal_list RIGHT_PAREN!
  ;


ra_dialect
  : A_DIALECT^ LEFT_PAREN! STRING RIGHT_PAREN!
  ;     
  
ra_lock_on_active
  : A_LOCKONACTIVE^ (LEFT_PAREN! BOOL RIGHT_PAREN!)
  ;
  
ra_direction
  : A_DIRECTION^
  ;  

ra_defeats
  : DEFEATS^ LEFT_PAREN! STRING RIGHT_PAREN!
  ;




operator_attributes
    : (AT single_operator_attribute)+
      -> ^(VT_ATTRIBUTES single_operator_attribute+)
      | AT LEFT_SQUARE single_operator_attribute (COMMA single_operator_attribute)* RIGHT_SQUARE
      -> ^(VT_ATTRIBUTES single_operator_attribute+)
    ;
  

single_operator_attribute
  : oa_kind
  | oa_id
  | oa_params
  | oa_degree
  | oa_merge
  //| oa_filter
  | oa_missing
  | oa_defeat
  | oa_default
  | oa_crisp
  | oa_otherwise
  ;

oa_kind
  :   OA_KIND^ LEFT_PAREN! STRING RIGHT_PAREN!
  ;
  
oa_id
  : OA_ID^ LEFT_PAREN! STRING RIGHT_PAREN!
  ;

oa_params
  : OA_PARAMS^ LEFT_PAREN! STRING RIGHT_PAREN!
  ;

oa_degree
  : OA_DEGREE^ LEFT_PAREN! STRING RIGHT_PAREN!
  ;
  
oa_crisp
  : OA_CRISP^
  ;   

oa_merge
  : OA_MERGE^ LEFT_PAREN! STRING RIGHT_PAREN!
  ;   

/*    
oa_filter
  : OA_FILTER^ LEFT_PAREN! STRING RIGHT_PAREN!
  ;
*/

oa_missing
  : OA_MISSING^ LEFT_PAREN! STRING RIGHT_PAREN!
  ;
        
oa_defeat
  : OA_DEFEAT^ 
  ; 

oa_default
  : OA_DEFAULT^
  ;

oa_otherwise
  : OA_OTHERWISE^ LEFT_PAREN! STRING RIGHT_PAREN!
  ;





pattern_attributes
  : (AT single_pattern_attribute)+
    -> ^(VT_ATTRIBUTES single_pattern_attribute+)
    | AT LEFT_SQUARE single_pattern_attribute (COMMA single_pattern_attribute)* RIGHT_SQUARE
    -> ^(VT_ATTRIBUTES single_pattern_attribute+)
  ;
    
single_pattern_attribute
  : single_operator_attribute
  | pa_onChange
  ;
  
pa_onChange
  : ONCHANGE^ LEFT_PAREN!
    (
      TIMES (COMMA! NEG_MARK! ID)*
      | ID (COMMA! ID)*
    )
    RIGHT_PAREN!
  ;     



query_attributes
    : (AT single_query_attribute)+
      -> ^(VT_ATTRIBUTES single_query_attribute+)
      | AT LEFT_SQUARE single_query_attribute (COMMA single_query_attribute)* RIGHT_SQUARE
      -> ^(VT_ATTRIBUTES single_query_attribute+)
    ;

single_query_attribute
  :
  ;
  
  
  
trail_attributes
  :  (AT single_trail_attribute)+
      -> ^(VT_ATTRIBUTES single_trail_attribute+)
      | AT LEFT_SQUARE single_trail_attribute (COMMA single_trail_attribute)* RIGHT_SQUARE
      -> ^(VT_ATTRIBUTES single_trail_attribute+)
    ;

single_trail_attribute
  : ta_trail_start
  | ta_trail_end
  ;

ta_trail_start
  : START
  ;

ta_trail_end
  : END
  ;  

/**************************** LHS *******************************************/


when_part
  : WHEN
    lhs_root?
    -> ^(VT_LHS lhs_root?)
  ;

lhs_root
  : lhs_implies more=lhs_implies*
  ->  {more==null}? lhs_implies
  ->  ^(VT_AND_IMPLICIT lhs_implies+)
  ;

lhs_base
  :   LEFT_PAREN! lhs_root RIGHT_PAREN!
  | lhs_atom_pattern  
  ;


lhs_implies
  : left=lhs_or (imp=imply_connective operator_attributes? right=lhs_or)? 
    -> {imp != null}? ^($imp operator_attributes? $left $right)    
    -> ^($left)
  ;
  
lhs_or
@init{
  Token orToken = null;
  ParserRuleReturnScope seq = null;
}
  : ld=lhs_diff 
            {seq=ld;}
        ( lios=lhs_or_sequitur[seq.getTree()]
      {seq=lios;}
  )*
          -> {lios==null}? ^($ld)
          -> ^($lios)
  ;
  
lhs_or_sequitur[Object leftChild]
  : or=or_connective^ (atts=operator_attributes!)? rightChild=lhs_diff!
             {
               Tree t = ((Tree) or.getTree());               
               if (atts != null)
                 t.addChild((Tree) atts.getTree());
              
             t.addChild((Tree) leftChild);
       t.addChild((Tree) rightChild.getTree());           
           } 
  ;
  
  


lhs_diff
  : lhs_and (( xor_connective^ | eq_connective^ ) operator_attributes? lhs_and)?
  ;
    
  
  
  
      
lhs_and
@init{
  Token orToken = null;
  ParserRuleReturnScope seq = null;
}
  : ld=lhs_unary
            {seq=ld;}
        ( lias=lhs_and_sequitur[seq.getTree()]
      {seq=lias;}
  )*
          -> {lias==null}? ^($ld)
          -> ^($lias)
  ;
  
lhs_and_sequitur[Object leftChild]
  : and=and_connective^ (atts=operator_attributes!)? rightChild=lhs_unary!
             {
               Tree t = ((Tree) and.getTree());                
               if (atts != null)
                 t.addChild((Tree) atts.getTree());
              
             t.addChild((Tree) leftChild);
       t.addChild((Tree) rightChild.getTree());           
           } 
  ;
  
lhs_unary
  : lhs_modified_unary filter_chain^?
  |   lhs_query
  ;

lhs_modified_unary
  : 
    unary_operator^  operator_attributes? lhs_modified_unary
  | LEFT_PAREN! lhs_root RIGHT_PAREN!    
  | lhs_quantifiexpr
  | lhs_edge
  | lhs_branch
  | lhs_sequence
  | lhs_label_atom_pattern
  ;




lhs_quantifiexpr
  : lhs_exists
  | lhs_not
  | lhs_forall
  ;


lhs_exists  
  : lab=label? EXISTS operator_attributes?
    lhs_base
  -> {lab==null}? ^(VT_EXISTS operator_attributes? lhs_base)
  -> ^(VT_BINDING label ^(VT_EXISTS operator_attributes? lhs_base)) 
  ;
  


lhs_forall  
  : FORALL operator_attributes?
  (
        pat=lhs_atom_pattern
    |   LEFT_PAREN lhs_unary lhs_unary RIGHT_PAREN   
  )
  -> {pat!=null}? ^(VT_FORALL operator_attributes? lhs_atom_pattern)
  -> ^(VT_FORALL operator_attributes? lhs_unary lhs_unary)
  ; 
  
lhs_not   
  : NOT operator_attributes? lhs_base
  -> ^(VT_NEXISTS operator_attributes? lhs_base)
  ; 


lhs_label_atom_pattern
  : lab=label? ( lhs_atom_pattern | ordered_obj_pattern )
    -> {lab==null}? lhs_atom_pattern? ordered_obj_pattern? 
    -> ^(VT_BINDING label lhs_atom_pattern ordered_obj_pattern?)  
  ;  


/* over_clause obsolete, replaced by filters (see far below in lhs_unary)*/
lhs_atom_pattern
  : ID LEFT_PAREN constraints? RIGHT_PAREN pattern_attributes?  from?  
  -> ^(VT_PATTERN
          ^(VT_AND pattern_attributes? VT_ENABLED ^(VT_TYPE ID) constraints? )
          from?
      )       
  ;



/************************************************ LHS CONSTRUCTS *************************/


lhs_edge
  : RISING lhs_base
    -> ^(VT_RISING_EDGE lhs_base)
   | FALLING lhs_base     
    -> ^(VT_FALLING_EDGE lhs_base)
  ;


lhs_branch
  : BRANCH  
      LEFT_PAREN  
        lhs_base?
        branch_alternative+
      RIGHT_PAREN  
      -> ^(VT_BRANCH ^(VT_BRANCH_DEFAULT lhs_base)? branch_alternative+ )
  ;
  
branch_alternative
  :  branch_label^ lhs_base    
  ;  
  
branch_label
  : LEFT_SQUARE neg=NEG_MARK? ID RIGHT_SQUARE
  -> {neg!=null}? ^(VT_NEG_BRANCH_LABEL ID)
  -> ^(VT_BRANCH_LABEL ID)
  ;  



filter_chain
  :   PIPE filter (PIPE filter)*
  -> ^(VT_FILTER filter+ )
  ;
  
filter
  :   over_filter
  |   unique_filter
  |   throttle_filter
  |   FILTER ID  
  ;
  
  
   
  
lhs_sequence
  : 
   SEQ LEFT_PAREN
      trail+
  RIGHT_PAREN   
  -> ^(VT_SEQUENCE trail+)
  ;

trail
  : trail_path 
  -> ^(VT_TRAIL trail_path)          
  ;  
  
trail_path
  : trail_node
    (lhs_root DOUBLE_GREATER! trail_node)+       
    SEMICOLON!    
  ;  

trail_node
  : ID trail_attributes?
  -> ^(VT_TRAIL_NODE ID trail_attributes?)
  ;
  
  
/*********************************************** INSIDE PATTERN *****************************************/



constraints
  : positional_constraints
    -> ^(VT_AND positional_constraints)
  | slotted_constraints
    -> ^(VT_AND slotted_constraints)
  ;
  
positional_constraints
@init{
  int j = 0;
}
  : positional_constraint[j++] (COMMA! positional_constraint[j++])* (COMMA! slotted_constraint)*
  ;
    
slotted_constraints
  : slotted_constraint (COMMA! slotted_constraint)*
  ;   
  
  
positional_constraint[int j]
@init{
  String idx = ""+j;
}
  : literal  
    -> ^(VT_POSITIONAL_CONST VT_POSITIONAL_INDEX[$start,idx] ^(EQUAL ^(VT_EXPR literal)))   
  | var_literal 
    -> ^(VT_POSITIONAL_VAR VT_POSITIONAL_INDEX[$start,idx] ^(EQUAL ^(VT_EXPR var_literal)))
  | QUESTION_MARK rest=restriction_root?
    ->{rest==null}? ^(VT_POSITIONAL_SKIP VT_POSITIONAL_INDEX[$start,idx])
    -> ^(VT_POSITIONAL_CONST VT_POSITIONAL_INDEX[$start,idx] restriction_root)   
  ;
                
slotted_constraint
  :   constr_implies  
  ; 
  
/*  
ordered_constraints
  : ordered_constraint[j++] (COMMA! ordered_constraint[j++])*
  ;  
  
ordered_constraint[int j]
@init{
  String idx = ""+j;
}
: literal  
    -> ^(VT_POSITIONAL_CONST VT_POSITIONAL_INDEX[$start,idx] literal)   
  | var_literal 
    -> ^(VT_POSITIONAL_VAR VT_POSITIONAL_INDEX[$start,idx] var_literal)
  | QUESTION_MARK restriction_root?
    -> ^(VT_POSITIONAL_SKIP VT_POSITIONAL_INDEX[$start,idx])   
  ;
*/

/********************************************* ATOMIC DATA DEFINITIONS ************************************************/


fully_qualified_name
  : ID ( DOT ID )*  
    -> ^(VT_NAME ID+)
  ;


gen_accessor_list
  : first=general_accessor_path (COMMA next=general_accessor_path)* 
  -> ^(VT_LIST general_accessor_path+)
  ;
 
literal_list
  : first=literal (COMMA next=literal)* 
  -> ^(VT_LIST literal+)
  ;  

var_list
  : first=VAR (COMMA next=VAR)* 
  -> ^(VT_LIST VAR+)
  ;
  
members_list
  : (literal|var_literal) (COMMA! (literal|var_literal))*
  ;  

integer_range
  : INT DOUBLE_DOT INT
  -> ^(VT_RANGE INT INT)
  ;

list_literal
  : LEFT_CURLY members_list? RIGHT_CURLY  
    ->^(VT_LIST members_list?)  
  ; 


literal
options{
k=6;
}
    :   STRING m=msr_unit?
        -> {m==null}? STRING
        -> ^(VT_MSR STRING $m)
    |   INT m=msr_unit?
        -> {m==null}? INT
        -> ^(VT_MSR INT $m)
    |   FLOAT m=msr_unit?
        -> {m==null}? FLOAT
        -> ^(VT_MSR FLOAT $m)
    |   BOOL 
    |   NULL 
    | literal_object
    | list_literal 
    ;
    
    
var
    :   VAR
    ;    
    
    
var_literal
    :   VAR m=msr_unit?
        -> {m==null}? VAR
        -> ^(VT_MSR VAR $m)
    ;    
    
label
    :   var COLON!
    ;
    
msr_unit
    :   (GATE! ID)+
    ;
       
    



    
  
    
literal_object
  : new_object
    | ordered_object_literal
  ;   
  
new_object
  : NEW ID LEFT_PAREN literal_object_args? RIGHT_PAREN
    -> ^(VT_NEW_OBJ ^(VT_TYPE ID) ^(VT_ARGS literal_object_args)?)
  ;  
  
literal_object_args
  :  method_args
  ;
  
  
//TODO : time format strings
time_string
  : STRING
  ;  


/**************************************************** METHODS AND EXPRESSIONS ***********************************************/
  
method_args
  : method_arg (COMMA! method_arg)* 
  ; 


method_arg
  : expr_root
  ;
  
method
  : core=method_core m=msr_unit?
    -> {m==null}? $core
    -> ^(VT_MSR $core $m)
  ;  
  
method_core
  : ID LEFT_PAREN args=method_args? RIGHT_PAREN
  -> {args==null}? ^(VT_METHOD ID )
  -> ^(VT_METHOD ID ^(VT_ARGS method_args?))
  ;

expr_root
  : factor  ( (PLUS | MINUS)^ factor )*
  ; 
  
factor
  : term ( (TIMES | SLASH)^ term )*  
  ; 
      
term
  : (MINUS^)? expr_unary 
  ; 
       
expr_unary
  : expr_atom  
  | LEFT_PAREN! expr_root RIGHT_PAREN! 
  ;
  
expr_atom
  : accessor_path
  | var_literal
  | literal 
  ; 



/************************************* SLOTTED CONSTRAINTS LOGIC HIERARCHY **********************************/

constr_implies
  : left=constr_or (imp=imply_connective operator_attributes? right=constr_or)? 
    -> {imp != null}? ^($imp operator_attributes? $left $right)    
    -> ^($left)
  ;
  
constr_or
@init{
  Token orToken = null;
  ParserRuleReturnScope seq = null;
}
  : ld=constr_diff 
            {seq=ld;}
        ( lios=constr_or_sequitur[seq.getTree()]
      {seq=lios;}
  )*
          -> {lios==null}? ^($ld)
          -> ^($lios)
  ;
  
constr_or_sequitur[Object leftChild]
  : or=or_connective^ (atts=operator_attributes!)? rightChild=constr_diff!
             {
               Tree t = ((Tree) or.getTree());               
               if (atts != null)
                 t.addChild((Tree) atts.getTree());
              
             t.addChild((Tree) leftChild);
       t.addChild((Tree) rightChild.getTree());           
           } 
  ;
  
  


constr_diff
  : constr_and (( xor_connective^ | eq_connective^ ) operator_attributes? constr_and)?
  ;
    
  
  
  
      
constr_and
@init{
  Token orToken = null;
  ParserRuleReturnScope seq = null;
}
  : ld=constr_unary
            {seq=ld;}
        ( lias=constr_and_sequitur[seq.getTree()]
      {seq=lias;}
  )*
          -> {lias==null}? ^($ld)
          -> ^($lias)
  ;
  
constr_and_sequitur[Object leftChild]
  : and=and_connective^ (atts=operator_attributes!)? rightChild=constr_unary!
             {
               Tree t = ((Tree) and.getTree());                
               if (atts != null)
                 t.addChild((Tree) atts.getTree());
              
             t.addChild((Tree) leftChild);
       t.addChild((Tree) rightChild.getTree());           
           } 
  ;
  
  

constr_unary
  : unary_operator^ operator_attributes? constr_unary
  | constr_atom
  | LEFT_PAREN! constr_implies RIGHT_PAREN! 
  ;
  
constr_atom
  : left=left_expression rest=restriction_root?
    -> {rest==null}? ^(left_expression)
    -> ^(VT_AND_IMPLICIT left_expression restriction_root)
  ;
    /*
    { 
        if (rest != null) {
              Tree t = ((Tree) rest.getTree());     
              Tree temp = t.getChild(0);                                        
              t.setChild(0,(Tree) left.getTree());
              t.addChild(temp);
        }  
    }
    -> {rest==null}? ^($left)
    -> ^($rest)
    */
  
  

  
restriction_root
  : restr_implies
  ; 


restr_implies
  : left=restr_or (imp=imply_symbol operator_attributes? right=restr_or)? 
    -> {imp != null}? ^($imp operator_attributes? $left $right)    
    -> ^($left)
  ;
  
restr_or
@init{
  Token orToken = null;
  ParserRuleReturnScope seq = null;
}
  : ld=restr_diff 
            {seq=ld;}
        ( lios=restr_or_sequitur[seq.getTree()]
      {seq=lios;}
  )*
          -> {lios==null}? ^($ld)
          -> ^($lios)
  ;
  
restr_or_sequitur[Object leftChild]
  : or=or_symbol^ (atts=operator_attributes!)? rightChild=restr_diff!
             {
               Tree t = ((Tree) or.getTree());               
               if (atts != null)
                 t.addChild((Tree) atts.getTree());
              
             t.addChild((Tree) leftChild);
       t.addChild((Tree) rightChild.getTree());           
           } 
  ;
  
  


restr_diff
  : restr_and (( xor_symbol^ | eq_symbol^ ) operator_attributes? restr_and)?
  ;
    
  
  
  
      
restr_and
@init{
  Token orToken = null;
  ParserRuleReturnScope seq = null;
}
  : ld=restr_unary
            {seq=ld;}
        ( lias=restr_and_sequitur[seq.getTree()]
      {seq=lias;}
  )*
          -> {lias==null}? ^($ld)
          -> ^($lias)
  ; 
  
restr_and_sequitur[Object leftChild]
  : and=and_symbol^ (atts=operator_attributes!)? rightChild=restr_unary!
             {
               Tree t = ((Tree) and.getTree());                
               if (atts != null)
                 t.addChild((Tree) atts.getTree());
              
             t.addChild((Tree) leftChild);
       t.addChild((Tree) rightChild.getTree());           
           } 
  ;



restr_unary
  : unary_operator operator_attributes? restr_unary
  | LEFT_PAREN! restr_implies RIGHT_PAREN!
  | restr_atom
  ;


restr_atom
  : qnt=inner_quantifier? eval=evaluator operator_attributes? right_expression
    -> {qnt==null}? ^(evaluator operator_attributes? right_expression)
    -> ^(inner_quantifier ^(evaluator operator_attributes? right_expression))
  ;

  
left_expression
  : label
    ( 
      accessor_path 
      -> ^(VT_BINDING label accessor_path)
      | PIPE expr_root PIPE
      -> ^(VT_BINDING label ^(VT_EXPR expr_root))
    )
  | PIPE expr_root PIPE
    -> ^(VT_EXPR expr_root)
  | accessor_path    
    -> accessor_path
  ;

right_expression
  : expr_root
  -> ^(VT_EXPR expr_root)
  | OTHERWISE
  -> ^(VT_OTHERWISE)
  ;


  
/***************************************** ACCESSOR PATHS *******************************/


general_accessor_path
  : var
  | accessor_path
  ;

accessor_path
  : accessor (DOT accessor)*
    -> ^(VT_ACCESSOR accessor+)
  | var (DOT accessor)+
    -> ^(VT_ACCESSOR var accessor+)
  ;


accessor
  :
  (ID LEFT_PAREN) => m=method ix=indexer?
    { 
        if (ix != null ) {
              Tree t = ((Tree) ix.getTree());     
              Tree temp = t.getChild(0);                                        
              t.setChild(0,(Tree) m.getTree());
              t.addChild(temp);
        }  
    }
    -> {ix==null}? ^($m)
    -> ^($ix)
  | id=versioned_accessor ix=indexer?
    { 
        if (ix != null ) {
              Tree t = ((Tree) ix.getTree());     
              Tree temp = t.getChild(0);                                        
              t.setChild(0,(Tree) id.getTree());
              t.addChild(temp);
        }  
    }  
    -> {ix==null}? ^($id)
    -> ^($ix)
  | nested_obj_pattern
  ;


versioned_accessor
  : ID v=version?
  -> {v!=null}? ^(VT_VERSION ID version)
  -> ID
  ;

version
  : DOUBLE_LESS! INT DOUBLE_GREATER!
  | DOUBLE_LESS! integer_range DOUBLE_GREATER!
  ;

nested_obj_pattern
  : GATE fqn=fully_qualified_name?  LEFT_PAREN constraints RIGHT_PAREN
    -> {fqn==null}? ^(VT_NESTED_PATTERN constraints) 
    -> ^(VT_NESTED_PATTERN 
          ^(VT_AND ^(VT_TYPE fully_qualified_name) constraints)
        )
  ;


indexer
  : LEFT_SQUARE
    (
        INT
        -> ^(VT_INDEXER INT)
      | STRING
        -> ^(VT_INDEXER STRING)
      | method
        -> ^(VT_INDEXER method)
      | GATE lhs_label_atom_pattern
        -> ^(VT_INDEXER lhs_label_atom_pattern)      
    )
    RIGHT_SQUARE
   
   | LEFT_SQUARE RIGHT_SQUARE
   -> ^(VT_INDEXER VT_INDEX_ALL) 
    
  ;






/************************************************** FILTERS ******************************/

over_filter
  : id1=WINDOW COLON 
    (  id2=TIME paren_chunk
      -> ^(VT_BEHAVIOR $id1 $id2 paren_chunk)
    |  id2=LENGTH LEFT_PAREN INT RIGHT_PAREN
      -> ^(VT_BEHAVIOR $id1 $id2 INT) 
    ) 
  ;
  
unique_filter
  : UNIQUE
    -> ^(VT_BEHAVIOR UNIQUE) 
  ;  
  
throttle_filter
  : THROTTLE LEFT_PAREN INT RIGHT_PAREN
    -> ^(VT_BEHAVIOR THROTTLE INT)
  ;  


/***************************************************   PATTERN SOURCES ******************************************/

from
  : FROM^
            (  accumulate_statement
              | collect_statement 
              | entrypoint_statement
              | from_source
            )
  ;




collect_statement
  : COLLECT 
    LEFT_PAREN 
      lhs_label_atom_pattern 
    RIGHT_PAREN 
  -> ^(COLLECT lhs_label_atom_pattern)
  ;

entrypoint_statement
  : ENTRYPOINT
    entrypoint_id
  -> ^(VT_ENTRYPOINT entrypoint_id)
  ;

entrypoint_id
  :   value=ID 
    -> VT_ENTRYPOINT_ID[$value]
  |   value=STRING 
    -> VT_ENTRYPOINT_ID[$value]
  ;

from_source
  : var
    | accessor_path 
    | LEFT_SQUARE! literal_list RIGHT_SQUARE!
    | LEFT_SQUARE! var_list RIGHT_SQUARE!
    | LEFT_SQUARE! integer_range RIGHT_SQUARE!    
  ;




accumulate_statement
  : acc_left
  | acc_right
  ;
  
  
acc_left
  : ACCUMULATE accumulate_body
  -> ^(VT_ACCUMULATE_LEFT accumulate_body)
  ;

acc_right
  : ACCUMULATE_RIGHT accumulate_body
  -> ^(VT_ACCUMULATE_RIGHT accumulate_body)
  ;    

accumulate_body
  : LEFT_PAREN 
      lhs_root 
    ( COMMA?
      ( accumulate_iteration
      | l=label? accumulate_functions )
    )?     
    RIGHT_PAREN
    -> {l==null}? lhs_root accumulate_iteration? accumulate_functions?
    -> lhs_root ^(VT_BINDING label accumulate_functions?)
  ;


accumulate_functions
  : acc_collect_list
  | acc_avg
  | acc_min
  | acc_max
  | acc_sum
  | acc_count
  | acc_distinct
  | acc_orderby
  | acc_limit
  ;

 
acc_collect_list
  : COLLECT_LIST 
      LEFT_PAREN
        (constr_implies
        | var)
      RIGHT_PAREN
    -> ^(VT_COLLECT_LIST constr_implies? var?)
  ;

acc_avg
  : AVERAGE LEFT_PAREN expr_root RIGHT_PAREN
  -> ^(VT_ACCUMULATE_FUNCTION AVERAGE ^(VT_EXPR expr_root))
  ;
  
acc_min
  : MIN LEFT_PAREN expr_root RIGHT_PAREN
  -> ^(VT_ACCUMULATE_FUNCTION MIN ^(VT_EXPR expr_root))
  ;

acc_max
  : MAX LEFT_PAREN expr_root RIGHT_PAREN
  -> ^(VT_ACCUMULATE_FUNCTION MAX ^(VT_EXPR expr_root))
  ;

acc_sum
  : SUM LEFT_PAREN expr_root RIGHT_PAREN
  -> ^(VT_ACCUMULATE_FUNCTION SUM ^(VT_EXPR expr_root))
  ;
  
acc_count
  : COUNT LEFT_PAREN RIGHT_PAREN
  -> ^(VT_ACCUMULATE_FUNCTION COUNT)
  ;
  
acc_distinct
  : DISTINCT LEFT_PAREN gen_accessor_list RIGHT_PAREN
  -> ^(VT_ACCUMULATE_FUNCTION DISTINCT gen_accessor_list)
  ;

acc_orderby
  : ORDERBY LEFT_PAREN gen_accessor_list RIGHT_PAREN
  -> ^(VT_ACCUMULATE_FUNCTION ORDERBY gen_accessor_list)
  ;
  
acc_limit
  : LIMIT LEFT_PAREN INT RIGHT_PAREN
  -> ^(VT_ACCUMULATE_FUNCTION LIMIT INT)
  ;



accumulate_iteration
  : INIT pc1=accumulate_paren_chunk COMMA?  
    ACTION pc2=accumulate_paren_chunk COMMA?
    REVERSE pc3=accumulate_paren_chunk COMMA?
    RESULT pc4=accumulate_paren_chunk COMMA?   
    -> ^(VT_ACCUMULATE_ITERATION ^(VT_ACC_ITER_INIT $pc1) ^(VT_ACC_ITER_ACT $pc2) ^(VT_ACC_ITER_REV $pc3)? ^(VT_ACC_ITER_RES $pc4))
  ;
  
  
accumulate_paren_chunk
@init{
  String text = "";
} 
  : pc=accumulate_paren_chunk_data[false] {text = $pc.text;} 
  -> VT_PAREN_CHUNK[$pc.start,text]
  ;

accumulate_paren_chunk_data[boolean isRecursive]
  : LEFT_PAREN
    (any=~ ( LEFT_PAREN | RIGHT_PAREN )   
    | accumulate_paren_chunk_data[true] )* 
    RIGHT_PAREN    
  ;


/******************************************* QUICK OBJS **********************/


ordered_object_literal
  : DOUBLE_SQUARE_LEFT
      method_args
    DOUBLE_SQUARE_RIGHT
    -> ^(VT_NEW_OBJ ^(VT_ARGS method_args))
  ;

ordered_obj_pattern
  : DOUBLE_SQUARE_LEFT
      positional_constraints
    DOUBLE_SQUARE_RIGHT
    -> ^(VT_AND positional_constraints)
  ;

/***************************************************   QUERIES ******************************************/


query   //TODO
  : QUERY ID parameters? 
      query_body+
    END
    -> ^(QUERY ID parameters? ^(VT_AND_IMPLICIT query_body+) )   
  ;

query_body
  : lhs_label_atom_pattern 
  | lhs_query 
  ;


lhs_query
  : QUESTION_MARK ID LEFT_PAREN positional_constraints? RIGHT_PAREN query_attributes? from?
    -> ^(VT_QUERY_PATTERN ID query_attributes? positional_constraints? from?)
  ;






/*************************************** QUANTIFIERS AND EVALUATORS ***********************************/

inner_quantifier
  : ALL^
  | SOME^
  | VALUE^
  | COUNT^ (AT! LEFT_SQUARE! 
        (
          INT
          | inner_attrs
        )
       RIGHT_SQUARE!)?
  ;
  
attr_min
  : (MIN^ EQUALS! INT)
  ;

attr_max
  : (MAX^ EQUALS! INT)
  ;
  
inner_attrs
  : inner_attr (COMMA! inner_attr)?
  ;  

inner_attr
  :
    attr_min
    | attr_max 
  ;


evaluator
  : (TILDE!)?
    (
      simple_evaluator
    | complex_evaluator
    | custom_evaluator
    // TODO : | temporal_evaluator
    )
  
  ;

simple_evaluator
  : EQUAL^ 
  | GREATER^ 
  | GREATER_EQUAL^ 
  | LESS^ 
  | LESS_EQUAL^ 
  | NOT_EQUAL^ 
  ;   
  
complex_evaluator
  : IN
  | CONTAINS
  ; 
  
custom_evaluator
  : ID square_chunk?  //TODO: [] is for backward compat.
  ;




/******************************** VARIOUS OPERATORS *************************************/


imply_connective
  : IMPLIES
    -> ^(VT_IMPLIES)
  ;

or_connective
  : OR
    -> ^(VT_OR)
  ;
      
and_connective
  : AND
    -> ^(VT_AND)
  ;     
  
xor_connective
  : XOR
    -> ^(VT_XOR)
  ;
  
eq_connective
  : EQUIV
    -> ^(VT_EQUIV)
  ;
  
  
imply_symbol
  : ARROW
    -> ^(VT_IMPLIES)
  ;

or_symbol
  : DOUBLE_PIPE
    -> ^(VT_OR)
  ;
      
and_symbol
  : DOUBLE_AMPER
    -> ^(VT_AND)
  ;     
  
xor_symbol
  : DOUBLE_CAP
    -> ^(VT_XOR)
  ;
  
eq_symbol
  : DOUBLE_ANG
    -> ^(VT_EQUIV)
  ; 
  

unary_operator
    : NEG
      -> ^(VT_NEG)    
    |   hedge^
    ;
  
    
hedge
    :     VERY
      -> ^(VT_HEDGE_VERY)
      | MOL
        -> ^(VT_HEDGE_VERY)
  ;
      

/********************************************************* RHS STRUCTURE ****************************************************************/

 
then_part  
  :     
      rhs_structured      
 //   | rhs_chunk
 //     -> ^(VT_RHS rhs_chunk)      
  ; 


rhs_structured
  : THEN then? (CLOSURE closure)? END    
    -> ^(VT_RHS then? closure?)
    
  | DO! LEFT_CURLY! rhs_atom* RIGHT_CURLY!    
  ;
  
then
  : rhs_atom+
  -> ^(VT_THEN rhs_atom+)
  ;  

closure
  : rhs_atom+
  -> ^(VT_CLOSURE rhs_atom+)
  ;   
  

rhs_atom
  :   rhs_insert
    | rhs_insert_logical
    | rhs_retract
    | rhs_retract_logical
    | rhs_update
    | rhs_modify
    | rhs_modify_logical
    | rhs_side_effect
  ; 
  
rhs_insert
  : INSERT^ literal_object
    SEMICOLON!
  ;

rhs_insert_logical
  : INSERT_LOG^ literal_object
    SEMICOLON!
  ;
  
rhs_retract
  : RETRACT^ 
    ( literal_object
    | var
    )
    SEMICOLON!
  ;

rhs_retract_logical
  : RETRACT_LOG^
    ( literal_object
    | var
    )
    SEMICOLON!
  ;

rhs_update
  : UPDATE^
      var
    SEMICOLON!  
  ;

rhs_modify
  : MODIFY^ LEFT_PAREN! var RIGHT_PAREN! 
    LEFT_CURLY!
      set_action+
    RIGHT_CURLY!
    SEMICOLON?
  ;
  
rhs_modify_logical
  : MODIFY_LOG^ LEFT_PAREN! var RIGHT_PAREN! 
    LEFT_CURLY!
      set_action+
    RIGHT_CURLY!
    SEMICOLON?
  ;  
  
set_action
  : accessor_path EQUALS right_expression SEMICOLON   
  -> ^(VT_SET accessor_path right_expression)
  ;  
  
  
rhs_side_effect
@init{
  String text = "";
}
  :     
    LESS_PERCENT rc=side_effect_chunk {text = $rc.text;} PERCENT_GREATER
    -> VT_RHS_CHUNK[$rc.start,text]
    | LESS dialect PERCENT
        rc=side_effect_chunk {text = $rc.text;}
      PERCENT_GREATER 
    -> ^(VT_DIALECT dialect VT_RHS_CHUNK[$rc.start,text])         
  ;
  
dialect
  : JAVA 
  | MVEL
  ;
  
side_effect_chunk
  : ~ ( END | (PERCENT_GREATER) )*   
  ;










/***************************************************************** CHUNKS *********************************************************************/


rhs_chunk
@init{
  String text = "";
} : THEN
    rc=rhs_chunk_data {text = $rc.text;}
    END 
    SEMICOLON?        
  -> VT_RHS_CHUNK[$rc.start,text]
  ;

rhs_chunk_data
  :     
      ~END*     
  ;
  
  














curly_chunk
@init{
  String text = "";
} : cc=curly_chunk_data[false] {text = $cc.text;}
  -> VT_CURLY_CHUNK[$cc.start,text]
  ;

curly_chunk_data[boolean isRecursive]
  : lc1=LEFT_CURLY
      (any=~ ( LEFT_CURLY | RIGHT_CURLY ) | curly_chunk_data[true] )* 
    rc1=RIGHT_CURLY   
  ;

square_chunk
@init{
  String text = "";
} : sc=square_chunk_data[false] {text = $sc.text;}
  -> VT_SQUARE_CHUNK[$sc.start,text]
  ;

square_chunk_data[boolean isRecursive]
  : ls1=LEFT_SQUARE
      (any=~ ( LEFT_SQUARE | RIGHT_SQUARE ) | square_chunk_data[true] )* 
    rs1=RIGHT_SQUARE
  ;
 
 
paren_chunk
@init{
  String text = "";
} : cc=paren_chunk_data[false] {text = $cc.text;}
  -> VT_PAREN_CHUNK[$cc.start,text]
  ;

paren_chunk_data[boolean isRecursive]
  : lc1=LEFT_PAREN
      (any=~ ( LEFT_PAREN | RIGHT_PAREN ) | paren_chunk_data[true] )* 
    rc1=RIGHT_PAREN
  ;

