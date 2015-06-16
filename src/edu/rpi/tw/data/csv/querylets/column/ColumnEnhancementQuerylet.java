package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Resource;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;


/**
 * @param <T>
 * 
 */
public abstract class ColumnEnhancementQuerylet<T> extends OnlyOneContextQuerylet<T> { // TODO: should not be a URI returner.

   public static String REPORT_INDENT = "       ";
   
   protected int csvColumnIndex;

   public ColumnEnhancementQuerylet(Resource context) {
      super(context);
   }
   
   public ColumnEnhancementQuerylet(Resource context, int csvColumnIndex) {
      super(context);
      this.csvColumnIndex = csvColumnIndex;
   }
   
   protected String columnPrefix() {
      return "ov";
   }
   
   protected String column() {
      return "\""+this.csvColumnIndex+"\"^^xsd:integer";
   }
   
   protected String columnPO() {
      return " ov:csvCol "+this.column()+" ";
   }
}