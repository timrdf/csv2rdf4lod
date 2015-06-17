package edu.rpi.tw.data.csv.querylets;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.returning.impl.DefaultStringQuerylet;


/**
 * The rdf:type of the row or cell.
 */
public class SubjectTypeQuerylet extends DefaultStringQuerylet {

   public SubjectTypeQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
   	
   	super.stringResult = null;
   	
      this.addNamespace("rdf", "xsd", "ov", "conversion");
      
      String select       = "distinct ?type";
      String graphPattern = 
                            "?s conversion:enhance [             \n"+
                            "      conversion:domain_name ?type; \n"+
                            "   ]                                \n";
      String orderBy      = "";
      String limit        = "1";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.stringResult = bindingSet.getValue("type").stringValue();
      System.err.println(getClass().getSimpleName() + "(*) ." + this.stringResult + ".");
   }
}