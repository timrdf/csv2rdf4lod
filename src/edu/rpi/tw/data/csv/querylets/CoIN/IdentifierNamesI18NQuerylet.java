package edu.rpi.tw.data.csv.querylets.CoIN;

import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * Inspired by https://github.com/timrdf/csv2rdf4lod-automation/issues/113
 * See https://github.com/timrdf/csv2rdf4lod-automation/wiki/conversion:i18n
 */
public class IdentifierNamesI18NQuerylet extends OnlyOneContextQuerylet<HashMap<String,String>> {

   protected HashMap<String,String> i18n;
   
   /**
    * 
    * @param context
    */
   public IdentifierNamesI18NQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      i18n = new HashMap<String,String>();
      this.addNamespace("conversion");
      
      String select       = "distinct ?symbol ?interpretation";
      String graphPattern = "[] conversion:i18n [ conversion:interpret [ conversion:symbol ?symbol; \n"+
                            "                                            conversion:interpretation ?interpretation ]].";
      String orderBy      = "?symbol ?interpretation";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String symbol         = bindingSet.getValue("symbol").stringValue();
      String interpretation = bindingSet.getValue("interpretation").stringValue();
      
      i18n.put(symbol, interpretation);
      
      System.err.println(getClass().getSimpleName() + "(*) ." + symbol+ ". ."+ interpretation+".");
   }

   @Override
   public HashMap<String, String> get() {
      return this.i18n;
   }
}