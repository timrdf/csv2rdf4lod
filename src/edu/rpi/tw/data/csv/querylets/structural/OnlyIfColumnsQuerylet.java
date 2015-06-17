package edu.rpi.tw.data.csv.querylets.structural;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.returning.impl.DefaultIntegerSetQuerylet;


/**
 * 
 */
public class OnlyIfColumnsQuerylet extends DefaultIntegerSetQuerylet {

   public OnlyIfColumnsQuerylet(Resource context) {
      super(context);
   }
 
   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?index";
      String graphPattern = "?s a conversion:Only_if_column ; ov:csvCol ?index ";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      int onlyIfColumnIndex = Integer.parseInt(bindingSet.getValue("index").stringValue());
      super.integerSet.add(onlyIfColumnIndex);
      System.err.println(getClass().getSimpleName() +"("+ onlyIfColumnIndex +")");
   }
}