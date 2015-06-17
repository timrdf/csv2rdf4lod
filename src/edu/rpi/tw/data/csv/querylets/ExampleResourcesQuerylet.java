package edu.rpi.tw.data.csv.querylets;

import java.util.HashSet;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.returning.impl.DefaultLongSetQuerylet;

/**
 * 
 */
public class ExampleResourcesQuerylet extends DefaultLongSetQuerylet {

   public ExampleResourcesQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      super.set = new HashSet<Long>();
      
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?rowNum";
      String graphPattern = "?s \n"+
                            "     conversion:enhance [ ov:csvRow ?rowNum; a conversion:ExampleResource ] .";
      String orderBy      = "";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String rowS = bindingSet.getValue("rowNum").stringValue();
      super.set.add(Long.parseLong(rowS));
      System.err.println(getClass().getSimpleName() + "(*) ." + rowS + ".");
      //System.err.println("num example resources: " + super.set.size());
   }
}