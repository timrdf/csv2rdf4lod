package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class DateTimePatternQuerylet extends    ColumnEnhancementQuerylet<HashMap<String,URI>> {

   /**
    * pattern to interpret value into a dateTime, and the range (date or dateTime) that should be used when 
    * the pattern matches.
    */
   protected HashMap<String,URI> patterns = new HashMap<String,URI>();
   protected int                 timezone = 0; // GMT
   
   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public DateTimePatternQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {

      this.addNamespace("rdfs", "xsd", "ov", "conversion");

      String select       = "distinct ?pattern ?range ?timezone";
      String graphPattern = 
                              "?cp conversion:enhance [                      \n"+
                              "   "+columnPO()+";                            \n"+
                              "   conversion:pattern              ?pattern;  \n"+
                              "   conversion:range                ?range;    \n"+
                              "] .                                           \n"+
                              "optional { ?cp conversion:datetime_timezone ?timezone; }";

      String orderBy      = "";

      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, "1"));
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String pattern = bindingSet.getValue("pattern").stringValue();
      String range   = bindingSet.getValue("range").stringValue();
      
      this.patterns.put(pattern, ValueFactoryImpl.getInstance().createURI(range));
      if( bindingSet.hasBinding("timezone") ) {
         this.timezone = Integer.parseInt(bindingSet.getValue("timezone").stringValue());
      }
      System.err.println(ColumnEnhancementQuerylet.REPORT_INDENT +
            getClass().getSimpleName()+"("+this.csvColumnIndex+") ."+pattern +". timezone: ."+timezone+". a " + range);
   }
   
   public HashMap<String,URI> get() {
      return this.patterns;
   }
   
   public int getTimezone() {
      return this.timezone;
   }
}