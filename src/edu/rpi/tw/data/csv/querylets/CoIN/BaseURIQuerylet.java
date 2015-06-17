package edu.rpi.tw.data.csv.querylets.CoIN;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.returning.impl.DefaultStringQuerylet;


/**
 * 
 */
public class BaseURIQuerylet extends DefaultStringQuerylet {
   
   public BaseURIQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?namespace";
      String graphPattern = "?s \n"+
                            "     conversion:base_uri ?namespace .";
      String orderBy      = "";
      
      //System.err.println(composeQuery(select, context, graphPattern, orderBy, "1"));
      return composeQuery(select, context, graphPattern, orderBy, "1");
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      super.stringResult = bindingSet.getValue("namespace").stringValue();
      System.err.println(getClass().getSimpleName() + "(*) ." + super.stringResult + ".");
   }
}