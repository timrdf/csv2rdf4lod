package edu.rpi.tw.data.csv.querylets;

import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 * 
 *
 */
public class ColumnRangeQuerylet extends OnlyOneContextQuerylet<HashMap<Resource,HashMap<Integer,Integer>> > {

	private HashMap<Resource,HashMap<Integer,Integer>> ranges;

   public ColumnRangeQuerylet(Resource context) {
   	super(context);
   }
   
	@Override
   public String getQueryString(Resource context) {
		this.ranges = new HashMap<Resource,HashMap<Integer,Integer>>();
		
      addNamespace("conversion");
      
      String select       = "distinct ?enhancement ?from ?to";
      
      String graphPattern = "[] conversion:enhance ?enhancement .    \n"+
                            "?enhancement conversion:fromCol ?from;  \n"+
                            "             conversion:toCol   ?to .   \n"+
                            "               filter(isLiteral(?from)) \n"+
                            "               filter(isLiteral(?to))   \n";
      String orderBy      = "";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, ""));
      return this.composeQuery(select, context, graphPattern, orderBy, "");
   }

	@Override
   public void handleBindingSet(BindingSet bindings) {
		try {
			Resource enhancementR = (Resource) bindings.getValue("enhancement");
			int from = Integer.parseInt(bindings.getValue("from").stringValue());
			int to   = Integer.parseInt(bindings.getValue("to").stringValue());
			if( ! this.ranges.containsKey(enhancementR)) {
				this.ranges.put(enhancementR, new HashMap<Integer,Integer>());
			}
			this.ranges.get(enhancementR).put(from, to);
	      System.err.println(getClass().getSimpleName() + "("+from+", "+to+")");
		}catch (Exception e){
			System.err.println("WARNING: could not process column range "+
					bindings.getValue("from").stringValue() + " - " +
					bindings.getValue("to").stringValue() );
		}
	}

	@Override
   public HashMap<Resource,HashMap<Integer,Integer>> get() {
	   return ranges;
   }
}