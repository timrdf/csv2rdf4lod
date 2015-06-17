package edu.rpi.tw.data.csv.querylets.structural;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
public class CellDelimiterQuerylet extends OnlyOneContextQuerylet<Object> {

   protected char delimiter = ',';
   
   public CellDelimiterQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {

      this.addNamespace("conversion");
      
      String select       = "?delimiter";
      String graphPattern = "?dataset conversion:conversion_process [ conversion:delimits_cell ?delimiter ] .";
      String orderBy      = "?delimiter";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String d = bindingSet.getValue("delimiter").stringValue();
      if( d.length() == 1 ) {
         delimiter = bindingSet.getValue("delimiter").stringValue().charAt(0);
         System.err.println(getClass().getSimpleName() + "(*) ." + delimiter + ".");
      }else {
         System.err.println(getClass().getSimpleName() + "(*) IGNORING ." + delimiter + ".");
      }
   }
   
   /**
    * 
    * @return
    */
   public char getDelimiter() {
      return this.delimiter;
   }

	@Override
   public Object get() {
		// USE getDelimiter!!!
	   return null;
   }
}