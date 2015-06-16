package edu.rpi.tw.data.csv.querylets.deprecation;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
public class OldNamespaceQuerylet extends OnlyOneContextQuerylet<Boolean> {

   protected boolean someTriples;
   
   public OldNamespaceQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.someTriples = false;
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "distinct ?s";
      String graphPattern = "?s <http://data-gov.tw.rpi.edu/vocab/conversion/enhance> ?o .";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.someTriples = true;
      System.err.println(getClass().getSimpleName() + "(*) ." + bindingSet.getBinding("s") + " " + bindingSet.getBinding("o") + " .");
   }

   @Override
   public Boolean get() {
      return this.someTriples;
   }
}