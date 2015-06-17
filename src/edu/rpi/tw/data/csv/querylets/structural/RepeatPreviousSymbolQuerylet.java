package edu.rpi.tw.data.csv.querylets.structural;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
public class RepeatPreviousSymbolQuerylet extends OnlyOneContextQuerylet<Set<String>> {

   protected HashSet<String> repeatIndicators;
   
   public RepeatPreviousSymbolQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) { // TODO: implement the column-specific querylet
      repeatIndicators = new HashSet<String>();
      
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?symbol";
      String graphPattern = "?s conversion:conversion_process [ conversion:interpret [ \n"+
                            "                              conversion:symbol         ?symbol; \n"+
                            "                              conversion:interpretation conversion:repeat_previous; ] ] .";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String repeatColumnIndicator = bindingSet.getValue("symbol").stringValue();
      this.repeatIndicators.add(repeatColumnIndicator);
      System.err.println(getClass().getSimpleName() + "(*) ." + repeatColumnIndicator + ".");
   }

   @Override
   public Set<String> get() {
      return this.repeatIndicators;
   }
}