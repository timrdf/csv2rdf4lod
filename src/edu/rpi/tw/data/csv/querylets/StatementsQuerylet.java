package edu.rpi.tw.data.csv.querylets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
public class StatementsQuerylet extends OnlyOneContextQuerylet<HashMap<Value,HashMap<Value,Set<Value>>>> {

	private HashMap<Value,HashMap<Value,Set<Value>>> statements = null;
	
	/**
	 * 
	 * @param context
	 */
	public StatementsQuerylet(Resource context) {
		super(context);
	}
	
	@Override
   public String getQueryString(Resource context) {
		
      this.statements = new HashMap<Value,HashMap<Value,Set<Value>>>();
      
      addNamespace("conversion");
      
      String select       = "distinct ?s ?p ?o";
      String graphPattern =  "[] conversion:enhance ["+
                             "  conversion:subject   ?s;\n"+
                             "  conversion:predicate ?p;\n"+
                             "  conversion:object    ?o;\n"+
                             "] .";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

	@Override
   public void handleBindingSet(BindingSet bindings) {
		
		Value subject   = bindings.getValue("s");
		Value predicate = bindings.getValue("p");		
		Value object    = bindings.getValue("o");
		
		if( ! statements.containsKey(subject) ) {
			statements.put(subject, new HashMap<Value,Set<Value>>());
		}
		if( ! statements.get(subject).containsKey(predicate) ) {
			statements.get(subject).put(predicate, new HashSet<Value>());
		}
		statements.get(subject).get(predicate).add(object);
   }
	
	@Override
   public HashMap<Value,HashMap<Value,Set<Value>>> get() {
	   return statements;
   }
}