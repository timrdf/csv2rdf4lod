package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class TypedResourcePromotionQuerylet extends ColumnEnhancementQuerylet<Value> {

   protected Value type;
   
   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public TypedResourcePromotionQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   /**
    * 
    */
   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("rdfs","xsd", columnPrefix(), "conversion");
      
      String select       = "?class";
      String graphPattern = "?col "+columnPO()+                         "; \n"+
                            "       conversion:range_name ?class . \n";
      String orderBy      = "";
      String limit        = "1";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }
   
   /**
    * 
    */
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.type = bindingSet.getValue("class");
      System.err.println(getClass().getSimpleName() + "(" + this.csvColumnIndex+") ." + type.stringValue() + ".");
   }

   @Override
   public Value get() {
      return this.type;
   }
}