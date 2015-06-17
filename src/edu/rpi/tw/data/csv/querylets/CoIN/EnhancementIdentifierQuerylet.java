package edu.rpi.tw.data.csv.querylets.CoIN;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.returning.impl.DefaultStringQuerylet;


/**
 * 
 */
public class EnhancementIdentifierQuerylet extends DefaultStringQuerylet {

   public EnhancementIdentifierQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");
      
      super.stringResult = null;
      
      String select       = "?eTag";
      String graphPattern = "?s conversion:enhancement_identifier ?eTag . ";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, "1");
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      super.stringResult = bindingSet.getValue("eTag").stringValue();
   }
}