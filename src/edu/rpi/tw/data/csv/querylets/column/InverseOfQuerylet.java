package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class InverseOfQuerylet extends    ColumnEnhancementQuerylet<Set<URI>> {

	private Set<URI> inverses = null;
	
	// https://github.com/timrdf/csv2rdf4lod-automation/issues/315
	
	public InverseOfQuerylet(Resource context, int csvColumnIndex) {
	   super(context, csvColumnIndex);
   }

	@Override
   public String getQueryString(Resource context) {
		
		inverses = new HashSet<URI>();
		
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?inverse";
      String graphPattern = "?col "+columnPO()+" ;\n"+
                            "     conversion:inverse_of ?inverse . filter(isIRI(?inverse))";
      String orderBy      = "";

      return this.composeQuery(select, context, graphPattern, orderBy, "");
   }

	@Override
   public void handleBindingSet(BindingSet bindingSet) {
		URI inverse = (URI) bindingSet.getValue("inverse");
      inverses.add(inverse);
      System.err.println(getClass().getSimpleName() +"("+ this.csvColumnIndex +") ."+ inverse +".");
   }

	@Override
   public Set<URI> get() {
	   return inverses;
   }
}