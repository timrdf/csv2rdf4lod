package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;


/**
 * 
 */
public class ObjectLabelTemplateQuerylet extends ColumnEnhancementQuerylet<String> {

   protected String labelTemplate;
   
   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public ObjectLabelTemplateQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.labelTemplate = null;
      
      this.addNamespace("rdfs","xsd", columnPrefix(), "conversion");
      
      String select       = "?template";
      String graphPattern = "?col "+columnPO()+";                          \n"+
                            "     conversion:label_template ?template . ";
      String orderBy      = "";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.labelTemplate = bindingSet.getValue("template").stringValue();
      System.err.println(getClass().getSimpleName() + "(" + this.csvColumnIndex+") ." + this.labelTemplate + ".");
   }

   @Override
   public String get() {
      return this.labelTemplate;
   }
}