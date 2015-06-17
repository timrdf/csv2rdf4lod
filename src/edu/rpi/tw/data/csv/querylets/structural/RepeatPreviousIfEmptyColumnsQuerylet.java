package edu.rpi.tw.data.csv.querylets.structural;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.returning.impl.DefaultIntegerSetQuerylet;


/**
 * 
 */
public class RepeatPreviousIfEmptyColumnsQuerylet extends DefaultIntegerSetQuerylet {

   public RepeatPreviousIfEmptyColumnsQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?index";
      String graphPattern = "?s a conversion:Repeat_previous_if_empty_column ; ov:csvCol ?index ";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String repeatColIndex = bindingSet.getValue("index").stringValue();
      super.integerSet.add(Integer.parseInt(repeatColIndex));
      System.err.println(getClass().getSimpleName() + "(*) ." + repeatColIndex + ".");
   }
}