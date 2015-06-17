package edu.rpi.tw.data.csv.querylets;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.returning.impl.DefaultIntegerSetQuerylet;

/**
 * 
 */
public class ColumnIndexesQuerylet extends DefaultIntegerSetQuerylet {

   public ColumnIndexesQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?index";
      String graphPattern = "?col ov:csvCol ?index .";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      int index = Integer.parseInt(bindingSet.getValue("index").stringValue());
      
      this.integerSet.add(index);
      //System.err.println(getClass().getSimpleName()+"(*) ." + index+".");
   }
}