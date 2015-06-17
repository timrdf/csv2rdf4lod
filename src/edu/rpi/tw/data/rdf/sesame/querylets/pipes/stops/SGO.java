package edu.rpi.tw.data.rdf.sesame.querylets.pipes.stops;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;

import edu.rpi.tw.data.rdf.sesame.query.Querylet;
import edu.rpi.tw.data.rdf.sesame.query.QueryletProcessor;
import edu.rpi.tw.data.rdf.sesame.query.impl.FocusedQuerylet;
import edu.rpi.tw.data.rdf.utils.pipes.Constants;
import edu.rpi.tw.string.pmm.DefaultPrefixMappings;

/**
 * List the subjects and objects related by the given predicate.
 */
public class SGO extends    FocusedQuerylet<HashMap<Resource,HashSet<Value>>> {
   
   protected HashMap<Resource,HashSet<Value>> results = null;
   
   /**
    * 
    * @param context - the named graph to search.
    * @param predicate - the 
    */
   public SGO(Resource context, Resource predicate) {
      super(context, predicate);
   }

   /**
    * 
    */
   @Override
   public String getQueryString(Collection<Resource> contexts) {
      this.results = new HashMap<Resource,HashSet<Value>>();
      
      String p = "<"+focus.stringValue()+">";
      
      String select       = "distinct *";
      String graphPattern = "?s "+p+" ?o .";
      String orderBy      = "";

      return this.composeQuery(select, contexts, graphPattern, orderBy);
   }

   /**
    * 
    */
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      //String quoteIfLiteral = bindingSet.getValue("o") instanceof Literal ? "\"" : "";
      /*System.out.println("   "+pmap.bestQNameFor(bindingSet.getValue("s").stringValue())+" "+
                              "GIVEN "+
                              quoteIfLiteral + 
                              pmap.bestQNameFor(bindingSet.getValue("o").stringValue()) +
                              quoteIfLiteral    
                           );*/
      Resource sR = (Resource)bindingSet.getValue("s");
      Value    o = bindingSet.getValue("o");
      if( !this.results.containsKey(sR) ) {
         this.results.put(sR, new HashSet<Value>() );
      }
      this.results.get(sR).add(o);
   }
   
   /**
    * 
    * @return
    */
   public HashMap<Resource,HashSet<Value>> get() {
      return this.results;
   }
   
   public static final String USAGE = "SGO predicateURI [predicateURI ...]";
   /**
    * 
    * @param args - SGO predicateURI [predicateURI ...]
    */
   public static void main(String[] args) {

      if( args.length == 0 ) {
         System.err.println("usage: "+USAGE);
         System.exit(1);
      }
      Repository repository = Constants.getPipeRepository();
      
      for( int i=0; i < args.length; i++ ) {
         String predicate = args[i];
         System.out.println(DefaultPrefixMappings.pmap.bestQNameFor(predicate));
         Resource subjectR = ValueFactoryImpl.getInstance().createURI(predicate);
         Querylet handler = new SGO(null,subjectR);
         QueryletProcessor.processQuery(repository, handler);
      }
   }
}