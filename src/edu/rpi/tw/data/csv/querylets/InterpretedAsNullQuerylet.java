package edu.rpi.tw.data.csv.querylets;

import org.openrdf.model.Resource;

/**
 * Global. Applies to all columns.
 */
public class InterpretedAsNullQuerylet extends InterpretedAsQuerylet {

   public InterpretedAsNullQuerylet(Resource context) {
      super(context,"null");
   }
}