package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class DatePatternQuerylet extends    ColumnEnhancementQuerylet<HashMap<String,URI>> {

   protected HashMap<String,URI> patterns = new HashMap<String,URI>();
   protected int                 timezone = 0; // GMT
   
   public DatePatternQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   // TODO: this was copied from DatatypePatternQuerylet. trim down for Date's fewer needs.
   
   @Override
   public String getQueryString(Resource context) {

      this.addNamespace("rdfs", "xsd", "ov", "conversion");

      String select       = "?pattern ?range ?timezone";
      String graphPattern = 
      "?cp conversion:enhance [                               \n"+
      "   "+columnPO()+";                                     \n"+
      "   conversion:pattern                  ?pattern;  \n"+
      "   conversion:range                         ?range;    \n"+
      "] .                                                    \n"+
      "optional { ?cp conversion:datetime_timezone ?timezone; }";

      String orderBy      = "";

      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, "1"));
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String pattern = bindingSet.getValue("pattern").stringValue();
      this.patterns.put(pattern,
                        ValueFactoryImpl.getInstance().createURI(bindingSet.getValue("range").stringValue()));
      if( bindingSet.hasBinding("timezone") ) {
         this.timezone = Integer.parseInt(bindingSet.getValue("timezone").stringValue());
      }
      System.err.println(ColumnEnhancementQuerylet.REPORT_INDENT +
                         getClass().getSimpleName()+"("+this.csvColumnIndex+") ."+pattern+" timezone: "+timezone+".");
   }
   
   public HashMap<String,URI> get() {
      return this.patterns;
   }
   
   public int getTimezone() {
      return this.timezone;
   }
}