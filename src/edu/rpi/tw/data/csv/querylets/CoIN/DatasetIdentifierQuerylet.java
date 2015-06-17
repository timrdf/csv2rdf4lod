package edu.rpi.tw.data.csv.querylets.CoIN;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.returning.impl.DefaultStringQuerylet;

/**
 * 
 */
public class DatasetIdentifierQuerylet extends DefaultStringQuerylet {

   public DatasetIdentifierQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?datasetIDTag";
      String graphPattern = "?s \n"+
                            "     conversion:dataset_identifier ?datasetIDTag .";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, "1");
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      super.stringResult= bindingSet.getValue("datasetIDTag").stringValue();
      System.err.println(getClass().getSimpleName() + "(*) ." + super.stringResult + ".");
   }
}