package edu.rpi.tw.data.csv.querylets.column.chainhead;

import org.openrdf.model.Resource;

import edu.rpi.tw.data.csv.querylets.column.inchain.EquivalentPropertyQueryletInChain;

public class EquivalentPropertyQueryletChainHead extends EquivalentPropertyQueryletInChain {
	
   public EquivalentPropertyQueryletChainHead(Resource context, int csvColumnIndex) {
	   super(context, csvColumnIndex);
   }

	@Override
   public String getQueryString(Resource context) {
      this.equivalentProperty = null;
      
      this.addNamespace("rdfs","xsd", columnPrefix(), "conversion");
      
      String select       = "?equivalentProperty";
      String graphPattern =  "[] conversion:conversion_process [ conversion:enhance ?col ]. \n"+
      							  "?col "+columnPO()+";\n"+
                             "     conversion:equivalent_property ?equivalentProperty . \n";
      String orderBy      = "";
      String limit        = "";

      //System.err.println("CHAIN HEAD: " + this.composeQuery(select, context, graphPattern, orderBy, limit));
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }
}