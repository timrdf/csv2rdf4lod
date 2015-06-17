package edu.rpi.tw.data.csv.querylets;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * Note: this can be asserted globally or on a specific column enhancement. If globally, cannot use "[.] "in template,
 * since there is no notion of "this column's value".
 * 
 * The domain only applies to non-bundled properties and is the type of the row/cell.
 * For the domain template of an implicit bundle, see edu.rpi.tw.data.csv.querylets.column.ImplicitBundledNameTemplateQuerylet.
 */
public class DomainTemplateQuerylet extends OnlyOneContextQuerylet<String> {

   protected String template;
   protected Integer column;
   
   public DomainTemplateQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.template = null;
      this.column   = null;
      
      addNamespace("conversion","ov");
      
      String select       = "distinct ?template ?column";
      String graphPattern = "  ?ds conversion:conversion_process [                                         \n"+
                            "       conversion:enhance ?enhancement;                                       \n"+
                            "  ] .                                                                         \n"+
                            "                          ?enhancement conversion:domain_template ?template . \n"+
                            "               optional { ?enhancement ov:csvCol                  ?column }   \n"+
                            "               optional { ?enhancement conversion:bundled_by      ?bundle }   \n"+
                            "               filter(!bound(?bundle))                                        \n";
      String orderBy      = "";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, "1"));
      return this.composeQuery(select, context, graphPattern, orderBy, "1");
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.template = bindingSet.getValue("template").stringValue();
      if( bindingSet.hasBinding("column") ) {
         this.column = Integer.parseInt(bindingSet.getValue("column").stringValue());
      }
      String col = column == null ? "*" : column.toString();
      System.err.println(getClass().getSimpleName() + "("+col+") ." + template + ".");
   }

   @Override
   public String get() {
      return this.template;
   }
   
   /**
    * 
    * @return The column of the enhancement; used only to fill the domain pattern.
    */
   public Integer getColumn() {
      return this.column;
   }
}