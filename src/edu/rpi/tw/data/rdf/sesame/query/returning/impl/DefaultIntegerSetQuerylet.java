package edu.rpi.tw.data.rdf.sesame.query.returning.impl;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
public abstract class DefaultIntegerSetQuerylet extends OnlyOneContextQuerylet<Set<Integer>> {

   protected Set<Integer> integerSet;
   
   public DefaultIntegerSetQuerylet(Resource context) {
      super(context);
      this.integerSet = new HashSet<Integer>();
   }

   @Override
   public Set<Integer> get() {
      return integerSet;
   }
}