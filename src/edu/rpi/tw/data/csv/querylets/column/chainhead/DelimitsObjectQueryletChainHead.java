package edu.rpi.tw.data.csv.querylets.column.chainhead;

import org.openrdf.model.Resource;

import edu.rpi.tw.data.csv.querylets.column.DelimitsObjectQuerylet;

public class DelimitsObjectQueryletChainHead extends DelimitsObjectQuerylet {

   public DelimitsObjectQueryletChainHead(Resource context, int csvColumnIndex) {
	   super(context, csvColumnIndex);
   }

	@Override
   public String getQueryString(Resource context) {
      this.objectDelimiter = null;
      
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?delimiter";
      String graphPattern = "[] conversion:conversion_process [ conversion:enhance ?col ]. \n"+
      							 "?col "+columnPO()+                        ";\n"+
                            "     conversion:delimits_object ?delimiter .";
      String orderBy      = "";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy));
      return this.composeQuery(select, context, graphPattern, orderBy);
   }
}
