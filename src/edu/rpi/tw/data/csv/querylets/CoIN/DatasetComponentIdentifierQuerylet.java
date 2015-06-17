package edu.rpi.tw.data.csv.querylets.CoIN;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.returning.impl.DefaultStringQuerylet;


/**
 * 
 */
public class DatasetComponentIdentifierQuerylet extends DefaultStringQuerylet {

   public DatasetComponentIdentifierQuerylet(Resource context) {
      super(context);
      super.stringResult = ""; // Empty, not null b/c this string is always being justed w/o null checks.
   }

   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?subjectDisc";
      String graphPattern = "?s conversion:subject_discriminator ?subjectDisc . ";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, "1");
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      super.stringResult = bindingSet.getValue("subjectDisc").stringValue();
      //System.err.println("DatasetComponentIdentifierQuerylet: "+super.stringResult);
   }
}