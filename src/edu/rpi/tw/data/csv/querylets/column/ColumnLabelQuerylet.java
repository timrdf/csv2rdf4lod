package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class ColumnLabelQuerylet extends ColumnEnhancementQuerylet <String> {

   protected String stringResult;
   
   /**
    * 
    * @param context
    * @param columnIndex
    */
   public ColumnLabelQuerylet(Resource context, int columnIndex) {
      super(context, columnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.stringResult = null;
      
      this.addNamespace("xsd", columnPrefix(), "conversion");
      
      String select       = "?label";
      String graphPattern = "?col "+columnPO()+";          \n"+
                            "       conversion:label ?label .";
      String orderBy      = "";
      String limit        = "1";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.stringResult = bindingSet.getValue("label").stringValue();
      //System.err.println(getClass().getSimpleName() + "(" + this.csvColumnIndex+") ." + stringResult + ".");
   }

   @Override
   public String get() {
      return this.stringResult;
   }
}