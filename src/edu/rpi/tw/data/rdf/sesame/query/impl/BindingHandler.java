package edu.rpi.tw.data.rdf.sesame.query.impl;

import org.openrdf.query.BindingSet;

/**
 * A BindingHandler is responsible for handing individual binding results from a sparql query.
 */
public interface BindingHandler {
   
   /**
    * Handle a single result.
    * @param bindingSet
    */
   public void handleBindingSet(BindingSet bindingSet);
   
   /**
    * Notification that all BindingSets for current query have been sent.
    * @param numResults the number of BindingSets that were sent for the current query.
    */
   public void finish(int numResults);
}