package edu.rpi.tw.data.csv.querylets.structural;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.csv.querylets.RowQuerylet;

/**
 * 
 */
public class DataStartRowQuerylet extends RowQuerylet {
   
   /**
    * 
    * @param context
    */
   public DataStartRowQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.row = null;
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?rowNum";
      String graphPattern = "?s \n"+
                            "     conversion:enhance [ ov:csvRow ?rowNum; a conversion:DataStartRow ] .";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, "1");
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      super.row = Integer.parseInt(bindingSet.getValue("rowNum").stringValue());
      System.err.println(getClass().getSimpleName() + "(*) ." + this.row + ".");
   }
}