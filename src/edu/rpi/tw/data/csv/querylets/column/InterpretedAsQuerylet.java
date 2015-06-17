package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;


/**
 * Collects the symbols that should have a specific interpretation in a single column. 
 * Superclass of InterpretedAsNull, InterpretedAsTrue, and InterpretedAsFalse Querylets, 
 * which defer to this class by passing "Null", "True", and "False", respectively. 
 * 
 * To collect the symbols that should have a specific interpretation in all columns, 
 * see {@link edu.rpi.tw.data.csv.querylets.InterpretedAsQuerylet} in the non-columns package.
 */
public abstract class InterpretedAsQuerylet extends    ColumnEnhancementQuerylet<Set<String>> {
   
   protected Set<String> symbols = null;
   protected String interpretation = "";
   
   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public InterpretedAsQuerylet(Resource context, int csvColumnIndex) {
      this(context, csvColumnIndex, "");
   }
   
   /**
    * 
    * @param context
    * @param csvColumnIndex
    * @param interpretation
    */
   public InterpretedAsQuerylet(Resource context, int csvColumnIndex, String interpretation) {
      super(context, csvColumnIndex);
      this.interpretation = interpretation;
   }
   
   @Override
   public String getQueryString(Resource context) {
      this.symbols = new HashSet<String>();
      this.addNamespace("rdf", "xsd", "ov", "conversion");
      
      String select       = "distinct ?symbol";
      String graphPattern = "?s"+this.columnPO()+";                                              \n"+
                            "   conversion:interpret [                                           \n"+
                            "      conversion:symbol         ?symbol;                            \n"+
                            "      conversion:interpretation conversion:"+this.interpretation+"; \n"+
                            "   ] ;                                                              \n"+
                            ".";
      String orderBy      = "";
      return this.composeQuery(select, context, graphPattern, orderBy);
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String symbol = bindingSet.getValue("symbol").stringValue();
      
      this.symbols.add(symbol);
      
      System.err.println(ColumnEnhancementQuerylet.REPORT_INDENT + 
                         getClass().getSimpleName() + "(" + this.csvColumnIndex+") ." + symbol + ".");
   }
   
   @Override
   public Set<String> get() {
      return this.symbols;
   }
}