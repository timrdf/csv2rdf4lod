package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class ImplicitBundledNameTemplateQuerylet extends ColumnEnhancementQuerylet<String> {

   protected String  template;
   protected Integer column;
   
   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public ImplicitBundledNameTemplateQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.template = null;
      this.column   = null;
      
      addNamespace("conversion","ov",this.columnPrefix(),"xsd");
      
      String select       = "distinct ?template";
      String graphPattern = "  ?ds conversion:conversion_process [                                         \n"+
                            "       conversion:enhance ?enhancement;                                       \n"+
                            "  ] .                                                                         \n"+
                            "                          ?enhancement ov:csvCol "+this.column()+";           \n"+
                            "                                       conversion:bundled_by ?bundle .        \n"+
                            ""+
                            " ?bundle conversion:name_template ?template .                                 \n";
      // resurfaced as https://github.com/timrdf/csv2rdf4lod-automation/issues/283
      
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