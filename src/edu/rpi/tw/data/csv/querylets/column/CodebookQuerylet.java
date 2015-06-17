package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class CodebookQuerylet extends    ColumnEnhancementQuerylet<HashMap<String,Value>> {

   protected HashMap<String,Value> codebook;
   
   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public CodebookQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.codebook = new HashMap<String,Value>();

      this.addNamespace("rdf", "xsd", "ov", "conversion");
      
      String select       = "distinct ?symbol ?interpretation";
      String graphPattern = 
         " ?s"+this.columnPO()+";                            \n" +
         "     conversion:interpret [                        \n" +
         "        conversion:symbol         ?symbol;         \n" +
         "        conversion:interpretation ?interpretation; \n" +
         "     ] ;                                           \n" +
         " .";
      String orderBy      = "";

      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy));
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String symbol         = bindingSet.getValue("symbol").stringValue();
      Value  interpretation = bindingSet.getValue("interpretation");
      this.codebook.put(symbol, interpretation);
      System.err.println(getClass().getSimpleName()+"("+this.csvColumnIndex+") ."+symbol+". -> ."+interpretation+".");
   }

	@Override
   public HashMap<String, Value> get() {
	   return this.codebook;
   }
}