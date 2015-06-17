package edu.rpi.tw.data.csv.querylets.CoIN;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.returning.impl.DefaultStringQuerylet;


/**
 * 
 */
public class SourceIdentifierQuerylet extends DefaultStringQuerylet {

   public SourceIdentifierQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?sourceID";
      String graphPattern = "?s \n"+
                            "     conversion:source_identifier ?sourceID .";
      String orderBy      = "";
      
      //System.err.println(composeQuery(select, context, graphPattern, orderBy, "1"));
      return composeQuery(select, context, graphPattern, orderBy, "1");
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      super.stringResult = bindingSet.getValue("sourceID").stringValue();
      System.err.println(getClass().getSimpleName() + "(*) ." + super.stringResult + ".");
   }
}