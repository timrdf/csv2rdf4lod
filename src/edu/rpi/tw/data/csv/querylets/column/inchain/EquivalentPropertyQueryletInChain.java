package edu.rpi.tw.data.csv.querylets.column.inchain;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.csv.querylets.column.ColumnEnhancementQuerylet;

/**
 * Relaxed to literal to accept templates.
 * https://github.com/timrdf/csv2rdf4lod-automation/issues/290 (for [/sdv])
 * https://github.com/timrdf/csv2rdf4lod-automation/issues/297 (for [#1])
 * 
 */
public class EquivalentPropertyQueryletInChain extends ColumnEnhancementQuerylet<Value> {

   protected Value equivalentProperty = null;
   
   public EquivalentPropertyQueryletInChain(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.equivalentProperty = null;
      
      this.addNamespace("rdfs","xsd", columnPrefix(), "conversion");
      
      String select       = "?equivalentProperty";
      String graphPattern = "[] conversion:enhance [ conversion:enhance ?col ] . ?col "+columnPO()+";\n"+
                            "     conversion:equivalent_property ?equivalentProperty . \n";
      String orderBy      = "";
      String limit        = "";

      //System.err.println("IN CHAIN: "+this.composeQuery(select, context, graphPattern, orderBy, limit));
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      //URI equivalentProperty = (URI) bindingSet.getValue("equivalentProperty");
      this.equivalentProperty = bindingSet.getValue("equivalentProperty");
      System.err.println(ColumnEnhancementQuerylet.REPORT_INDENT +
                         getClass().getSimpleName() + "(" + this.csvColumnIndex+") ." + 
                         equivalentProperty.stringValue() + ".");
   }

   @Override
   public Value get() {
      return this.equivalentProperty;
   }  
}