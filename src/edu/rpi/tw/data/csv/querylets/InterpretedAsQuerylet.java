package edu.rpi.tw.data.csv.querylets;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;


/**
 * Collects the symbols that should have a specific interpretation in all columns. Superclass of InterpretedAsNull, 
 * InterpretedAsTrue, and InterpretedAsFalse Querylets, which defer to this class by 
 * passing "Null", "True", and "False", respectively. 
 * 
 * To collect the symbols that should have a specific interpretation in only a specific column, 
 * see {@link edu.rpi.tw.data.csv.querylets.column.InterpretedAsQuerylet} in the columns package.
 */
public abstract class InterpretedAsQuerylet extends OnlyOneContextQuerylet<Set<String>> {

   protected Set<String> symbols = null;
   protected String interpretation = "";
   
   protected InterpretedAsQuerylet(Resource context, String interpretation) {
      super(context);
      this.interpretation = interpretation;
   }
   
   public InterpretedAsQuerylet(Resource context) {
      this(context,"");
   }
   
   @Override
   public String getQueryString(Resource context) {
   	
   	this.symbols = new HashSet<String>();
      this.addNamespace("rdf", "xsd", "ov", "conversion");
      
      String select       = "distinct ?symbol                                                     \n";
      String graphPattern = "?ds conversion:conversion_process [                                  \n"+
                            "    conversion:interpret [                                           \n"+
                            "       conversion:symbol         ?symbol;                            \n"+
                            "       conversion:interpretation conversion:"+this.interpretation+"; \n"+
                            "    ];                                                               \n"+
                            "] .";
      String orderBy      = "";

      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy));
      return this.composeQuery(select, context, graphPattern, orderBy);
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String symbol = bindingSet.getValue("symbol").stringValue();
      
      this.symbols.add(symbol);

      System.err.println(getClass().getSimpleName()+"(*) ." + symbol+".");
   }

   @Override
   public Set<String> get() {
      return this.symbols;
   }
}