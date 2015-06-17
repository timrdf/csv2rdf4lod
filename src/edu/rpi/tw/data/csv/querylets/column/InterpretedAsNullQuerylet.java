package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Resource;

/**
 * 
 */
public class InterpretedAsNullQuerylet extends InterpretedAsQuerylet {
   
   public InterpretedAsNullQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex, "null");
   }
}