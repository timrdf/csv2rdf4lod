package edu.rpi.tw.data.csv.querylets.column.chainhead;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.csv.querylets.column.ColumnEnhancementQuerylet;

/**
 * https://github.com/timrdf/csv2rdf4lod-automation/issues/368
 */
public class EnhancementChainQuerylet extends    ColumnEnhancementQuerylet<Boolean> {

	boolean chained = false;
	
	/**
	 * 
	 * @param context
	 * @param csvColumnIndex
	 */
	public EnhancementChainQuerylet(Resource context, int csvColumnIndex) {
	   super(context, csvColumnIndex);
   }

	@Override
   public String getQueryString(Resource context) {
		
		chained = false;
		
      this.addNamespace("xsd", columnPrefix(), "conversion");
      
      String select       = "?e";
      String graphPattern = "?e "+columnPO()+";\n"+
                            "   conversion:enhance [] .\n";
      String orderBy      = "";
      String limit        = "1";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, limit));
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

	@Override
   public void handleBindingSet(BindingSet bindingSet) {
		chained = true;
      System.err.println(ColumnEnhancementQuerylet.REPORT_INDENT + 
            getClass().getSimpleName() + "(" + this.csvColumnIndex+")");
   }

	@Override
   public Boolean get() {
	   return chained;
	}
}