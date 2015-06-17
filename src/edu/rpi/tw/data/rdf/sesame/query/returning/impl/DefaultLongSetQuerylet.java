package edu.rpi.tw.data.rdf.sesame.query.returning.impl;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
public abstract class DefaultLongSetQuerylet extends OnlyOneContextQuerylet<Set<Long>> {

   protected Set<Long> set;
   
   public DefaultLongSetQuerylet(Resource context) {
      super(context);
      this.set = new HashSet<Long>();
   }

   @Override
   public Set<Long> get() {
      return set;
   }
}