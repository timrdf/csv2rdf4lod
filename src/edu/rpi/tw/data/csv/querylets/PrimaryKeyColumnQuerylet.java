package edu.rpi.tw.data.csv.querylets;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;


/**
 * 1-based
 */
public class PrimaryKeyColumnQuerylet extends    OnlyOneContextQuerylet<Integer> { 

   protected int primaryKeyColumn = 0;
   
   public PrimaryKeyColumnQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?index";
      String graphPattern = "?s a conversion:PrimaryKeyEnhancement ; ov:csvCol ?index ";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, "1");
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.primaryKeyColumn = Integer.parseInt(bindingSet.getValue("index").stringValue());
      //System.err.println("PrimaryKeyColumnQuerylet: "+this.primaryKeyColumn);
   }
   
   public Integer get() {
      //System.err.println("PrimaryKeyColumnQuerylet get: "+this.primaryKeyColumn);
      return this.primaryKeyColumn;
   }
}