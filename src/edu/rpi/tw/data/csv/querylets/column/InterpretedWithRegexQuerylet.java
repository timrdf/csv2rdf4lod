package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

/**
 * Find regular expressions that should be applied to the original input, along with the values to
 * replace what matches.
 */
public class InterpretedWithRegexQuerylet extends ColumnEnhancementQuerylet {

	private HashMap<String,String> searchReplaces = new HashMap<String,String>();
	
	public InterpretedWithRegexQuerylet(Resource context, int csvColumnIndex) {
	   super(context, csvColumnIndex);
   }

	@Override
   public HashMap<String,String> get() {
	   return searchReplaces;
   }

	@Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "distinct ?search ?replace";
      String graphPattern = "?s"+this.columnPO()+";                    \n"+
                            "   conversion:interpret [                 \n"+
                            "      conversion:regex          ?search;  \n"+
                            "      conversion:interpretation ?replace; \n"+
                            "   ] ;                                    \n"+
                            ".";
      String orderBy      = "";
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

	@Override
   public void handleBindingSet(BindingSet bindings) {
		String search  = bindings.getValue("search").stringValue();
		String replace = bindings.getValue("replace").stringValue();
		searchReplaces.put(search, replace);
      System.err.println(ColumnEnhancementQuerylet.REPORT_INDENT + 
            getClass().getSimpleName() + "(" + this.csvColumnIndex+") ." + 
      		search + " -> \"" + replace +"\"");
   }
}