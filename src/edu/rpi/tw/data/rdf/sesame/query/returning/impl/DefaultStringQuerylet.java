package edu.rpi.tw.data.rdf.sesame.query.returning.impl;

import org.openrdf.model.Resource;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;
import edu.rpi.tw.string.pmm.PrefixMappings;

/**
 * 
 */
public abstract class DefaultStringQuerylet extends    OnlyOneContextQuerylet<String> {

   protected String stringResult;
   
   public DefaultStringQuerylet(Resource context) {
      super(context);
   }
   
   public DefaultStringQuerylet(Resource context, PrefixMappings prefixMappings) {
   	super(context, prefixMappings);
   }
   @Override
   public String get() {
      return this.stringResult;
   }
}