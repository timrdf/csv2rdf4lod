package edu.rpi.tw.data.csv.querylets;

import org.openrdf.model.Resource;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;


/**
 * 
 */
public abstract class RowQuerylet extends OnlyOneContextQuerylet<Integer> {

   protected Integer row = null;
   
   public RowQuerylet(Resource context) {
      super(context);
   }
   
   @Override
   public Integer get() {
      return this.row;
   }
}