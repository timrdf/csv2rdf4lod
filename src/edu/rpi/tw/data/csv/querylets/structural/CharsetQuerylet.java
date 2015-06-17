package edu.rpi.tw.data.csv.querylets.structural;

import java.nio.charset.Charset;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
// https://github.com/timrdf/csv2rdf4lod-automation/issues/277
public class CharsetQuerylet extends OnlyOneContextQuerylet<Charset> {

   protected String charset = "UTF-8";
   protected boolean set = false;
   
   public CharsetQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {

      this.addNamespace("conversion");
      
      String select       = "?charset";
      String graphPattern = "?dataset conversion:conversion_process [ conversion:charset ?charset ] .";
      String orderBy      = "?charset";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String cs = bindingSet.getValue("charset").stringValue();
      if( !set ) {
         this.charset = bindingSet.getValue("charset").stringValue();
         System.err.println(getClass().getSimpleName() + "(*) ." + this.charset + ".");
      }else {
         System.err.println(getClass().getSimpleName() + "(*) IGNORING ." + cs + ".");
      }
   }
   
   @Override
   public Charset get() {
      return Charset.forName(this.charset);
   }
}