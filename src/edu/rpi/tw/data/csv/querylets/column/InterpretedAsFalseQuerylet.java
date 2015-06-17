package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Resource;

/**
 * 
 */
public class InterpretedAsFalseQuerylet extends InterpretedAsQuerylet {
   
   public InterpretedAsFalseQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex, "false");
   }
}