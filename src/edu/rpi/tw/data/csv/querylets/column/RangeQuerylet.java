package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class RangeQuerylet extends ColumnEnhancementQuerylet<URI> {

	protected URI range = null;
	
   public RangeQuerylet(Resource context, int csvColumnIndex) {
      super(context,csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");

      String select       = "?range";
      String graphPattern = "?col "+columnPO()+" ;\n"+
                            "     conversion:range ?range . filter(isIRI(?range))";
      String orderBy      = "";

      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, "1"));
      return this.composeQuery(select, context, graphPattern, orderBy, "1");
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.range = (URI) bindingSet.getValue("range");
      System.err.println(getClass().getSimpleName() + "(" + this.csvColumnIndex+") ." + range + ".");
   }

	@Override
   public URI get() {
	   return this.range;
   }
}