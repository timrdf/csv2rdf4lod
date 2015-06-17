package edu.rpi.tw.data.csv.querylets.column.chainhead;

import org.openrdf.model.Resource;

import edu.rpi.tw.data.csv.querylets.column.InterpretedWithRegexQuerylet;

public class InterpretedWithRegexQueryletChainHead extends InterpretedWithRegexQuerylet {

	public InterpretedWithRegexQueryletChainHead(Resource context, int csvColumnIndex) {
	   super(context, csvColumnIndex);
   }

	@Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "distinct ?search ?replace";
      String graphPattern = "[] conversion:conversion_process [ conversion:enhance ?s ]. \n"+
    		                   "?s"+this.columnPO()+";                    \n"+
                            "   conversion:interpret [                 \n"+
                            "      conversion:regex          ?search;  \n"+
                            "      conversion:interpretation ?replace; \n"+
                            "   ] ;                                    \n"+
                            ".";
      String orderBy      = "";
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy));
      return this.composeQuery(select, context, graphPattern, orderBy);
   }
}