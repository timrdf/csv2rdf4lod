package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class ColumnCommentQuerylet extends ColumnEnhancementQuerylet<Set<Value>> {

   protected Set<Value> stringResults = null;
   
   /**
    * 
    * @param context
    * @param columnIndex
    */
   public ColumnCommentQuerylet(Resource context, int columnIndex) {
      super(context,columnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.stringResults = new HashSet<Value>();
      
      this.addNamespace("xsd", columnPrefix(), "conversion");
      
      String select       = "?label";
      String graphPattern = "?col "+columnPO()+";             \n"+
                            "       conversion:comment ?label .";
      String orderBy      = "";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      
      Value comment = bindingSet.getValue("label");
      
      if( comment.stringValue().length() > 0 ) { // conversion:comments are part of the template; ignore them if empty.
         this.stringResults.add(comment);
         System.err.println(ColumnEnhancementQuerylet.REPORT_INDENT + getClass().getSimpleName() + 
                           "(" + this.csvColumnIndex+") ." + comment.stringValue().substring(0, (int)Math.min(60, comment.stringValue().length())) + "....");
      }
   }
   
   @Override
   public Set<Value> get() {
      return this.stringResults;
   }
}