package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class DelimitsObjectQuerylet extends ColumnEnhancementQuerylet<String> {

   protected String objectDelimiter;
   
   public DelimitsObjectQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.objectDelimiter = null;
      
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?delimiter";
      String graphPattern = "?col "+columnPO()+                        ";\n"+
                            "     conversion:delimits_object ?delimiter .";
      String orderBy      = "";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy));
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      if( bindingSet.hasBinding("delimiter") && this.objectDelimiter == null ) {
         this.objectDelimiter = bindingSet.getValue("delimiter").stringValue();
         System.err.println(getClass().getSimpleName() + "("+this.csvColumnIndex+") ."+this.objectDelimiter+".");
      }
   }

   @Override
   public String get() {
      return this.objectDelimiter;
   }
}