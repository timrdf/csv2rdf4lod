package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Resource;

/**
 * 
 */
public class InterpretedAsTrueQuerylet extends InterpretedAsQuerylet {
   
   public InterpretedAsTrueQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex, "true");
   }
}