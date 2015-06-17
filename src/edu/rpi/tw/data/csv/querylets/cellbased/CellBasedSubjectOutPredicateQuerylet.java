package edu.rpi.tw.data.csv.querylets.cellbased;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
public class CellBasedSubjectOutPredicateQuerylet extends OnlyOneContextQuerylet<Value> {

   protected Value outPredicate;
   
   public CellBasedSubjectOutPredicateQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) { // TODO: add column-specific version of this query.
      this.outPredicate = null;
      
      this.addNamespace("rdf","conversion");
      
      String select       = "?property";
      String graphPattern = "rdf:value conversion:equivalent_property ?property .";
      String orderBy      = "?property";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.outPredicate = bindingSet.getValue("property");
      System.err.println(getClass().getSimpleName() + "(*) ." + this.outPredicate.stringValue() + ".");
   }

   @Override
   public Value get() {
      return this.outPredicate;
   }
}