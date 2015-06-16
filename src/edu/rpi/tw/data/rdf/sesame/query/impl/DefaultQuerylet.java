package edu.rpi.tw.data.rdf.sesame.query.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.rdf.sesame.query.Querylet;
import edu.rpi.tw.data.rdf.sesame.query.QueryletProcessor;
import edu.rpi.tw.data.utils.SetUtilities;
import edu.rpi.tw.string.pmm.DefaultPrefixMappings;
import edu.rpi.tw.string.pmm.IPrefixMappings;
import edu.rpi.tw.string.pmm.PrefixMappings;


/**
 * An implementation of the Querylet interface that stores the context to query and 
 * the Namespaces required for a query's graph pattern.
 * 
 * Reports that it returns SPARQL query strings by default, can override for other languages.
 * 
 * OnlyOneContextQuerylet for backward compatibility.
 */
public abstract class DefaultQuerylet<T> implements Querylet<T> { //, Useful {

   protected ValueFactory vf = ValueFactoryImpl.getInstance();
   
   public static DefaultPrefixMappings singletonPrefixMappings = new DefaultPrefixMappings();
   
   /**
    * Replaces this.context (n=1) with n=n.
    */
   protected Collection<Resource> contexts = new HashSet<Resource>();
   
   /**
    * @deprecated use contexts Collection.
    */
   //protected Resource       context;
   protected Set<Namespace> prefixes; // The prefixes that should be included in the query.
   protected PrefixMappings pmap;     // Abbreviations for every known namespace
   
   protected boolean ask = false; // To support behavior as a ASK SPARQL query.
   
   /**
    * Cannot use empty constructor: must specify a context (which can be null).
    */
   protected DefaultQuerylet() {
      this((Collection<Resource>) null);
   }
   
   /**
    * Use a default PrefixMappings.
    * 
    * @param context
    */
   public DefaultQuerylet(Resource context) {
      this(context, singletonPrefixMappings);
   }
   
   /**
    * Use a default PrefixMappings.
    * 
    * @param _contexts
    */
   public DefaultQuerylet(Collection<Resource> _contexts) {
      this(_contexts, singletonPrefixMappings);
   }
   
   /**
    * @param context - a context that the query should be executed against.
    * @param pmap - namespaces and their abbreviations.
    */
   public DefaultQuerylet(Resource context, PrefixMappings prefixMappings) {
      this((Collection<Resource>) null, prefixMappings);
      
      this.contexts.add(context);
   }
   
   /**
    * @param contexts - the context(s) that the query should be executed against (graph union).
    * @param pmap - namespaces and their abbreviations.
    */
   public DefaultQuerylet(Collection<Resource> _contexts, PrefixMappings _prefixMappings) {
      super();
      
      if( _contexts != null ) {
         this.contexts = _contexts;
      }
      
      if( _prefixMappings != null ) {
         this.pmap = _prefixMappings;
      }

      this.prefixes = new HashSet<Namespace>();
   }



   /**
    * SPARQL
    */
   @Override
   public QueryLanguage getQueryLanguage() { // TODO: make this final.
      return QueryLanguage.SPARQL;
   }

   @Override
   public String getQueryString() {
      return getQueryString(this.contexts);
   }

   @Override
   public String getQueryString(Resource context) {
      return getQueryString(SetUtilities.setOf(context));
   }

   @Override
   public void finish(int numResults) {
      this.ask = numResults > 0;
   }

   /**
    * @param namespace - a namespace required for the query.
    */
   protected void addNamespace(Namespace... namespace) {
      for( Namespace ns : namespace ) {
         this.prefixes.add(ns);
      }
   }

   /**
    * @param abbreviation - e.g. "rdf"
    */
   protected void addNamespace(String... abbreviation) {
      for( String abbrev : abbreviation ) {
         try {
            this.addNamespace(pmap.getNamespace(abbrev));
         } catch (NamingException e) {
            e.printStackTrace();
         }
      }
   }

   /**
    * @return a sparql-friendly string defining the prefixes this Querylet needs.
    */
   protected String definePrefixes() {
      return DefaultQuerylet.definePrefixes(this.prefixes,false);
   }
   
   public static String definePrefixes(Collection<Namespace> namespaces) {
      return DefaultQuerylet.definePrefixes(namespaces,false);
   }
   
   /**
    * @param namespaces
    * @param useAtPeriodSyntax
    * @return a string defining prefixes in 'namespaces. 
    *         if 'sparqlSyntax', make sparql-friendly, o/w make turtle-friendly.
    */
   public static String definePrefixes(Collection<Namespace> namespaces, boolean useAtPeriodSyntax) {
      String at     = useAtPeriodSyntax ? "@"  : "";
      String period = useAtPeriodSyntax ? " ." : "";
      
      String def = "";
      for( Namespace n : namespaces ) {
         def = def + at + "prefix " + n.getPrefix() + ": <" + n.getName() + ">"+period+"\n";
      }
      return def;
   }

   protected String composeQuery(String select, Collection<Resource> contexts, String graphPattern) {

      // return composeQuery(select, context, graphPattern, orderBy, "");
      return QueryletProcessor.composeQuery(definePrefixes(), select, contexts, graphPattern, "", "", "");
   }
   
   protected String composeQuery(String select, Resource context, String graphPattern) {

      // return composeQuery(select, context, graphPattern, orderBy, "");
      return QueryletProcessor.composeQuery(definePrefixes(), select, context, graphPattern, "", "", "");
   }

   protected String composeQuery(String select,
                                 Resource context, String graphPattern,
                                 String orderBy, String limit) {
      
      return QueryletProcessor.composeQuery(definePrefixes(),
                                            select, 
                                            context, 
      												  graphPattern, orderBy, limit);
   }

   protected String composeQuery(String select,
                                 Resource context, String graphPattern,
                                 String orderBy) {
      
      //return composeQuery(select, context, graphPattern, orderBy, "");
      return QueryletProcessor.composeQuery(definePrefixes(), select, context, graphPattern, "", orderBy, "");
   }
   
   protected String composeQuery(String select,
         Collection<Resource> contexts, String graphPattern,
         String orderBy) {

      //return composeQuery(select, context, graphPattern, orderBy, "");
      return QueryletProcessor.composeQuery(definePrefixes(), select, contexts, graphPattern, "", orderBy, "");
   }
   
   protected String composeQuery(String select,
                                 Resource context, String graphPattern,
                                 String groupBy, String orderBy, String limit) {

      return composeQuery(select, SetUtilities.setOf(context), graphPattern, groupBy, orderBy, limit);
   }

   /**
    * @param select
    * @param context
    * @param graphPattern
    * @param groupBy
    * @param orderBy
    * @param limit
    * @return a concatenation of 'select', 'context', 'graphPattern', 'orderBy', and 'limit' with the appropriate 
    *         sparql keywords and named graph syntax.
    */
   protected String composeQuery(String select,
                                 Collection<Resource> contexts, String graphPattern, 
                                 String groupBy, String orderBy, String limit) {
      
      return QueryletProcessor.composeQuery(definePrefixes(),
                                            select,
                                            contexts, graphPattern,
                                            groupBy, orderBy, limit);
      // composeQuery(String prefixes,
      //              String select, Resource context, String graphPattern,
      //              String groupBy, String orderBy, String limitTo) {
   }
   
   /**
    * Process 'args' for a set of contexts.
    * 
    * @param args - the arguments to a main()
    * @param startIndex - the first index of 'args' that names a context.
    * @param repository - an initialized repository from which to get contexts if none found in 'args'.
    * 
    * @return a set of contexts from args; if args empty populate with all contexts from 'repository'.
    */
   public static Set<Resource> getContexts(String[] args, int startIndex, Repository repository) {
      Set<Resource> contexts = getResources(args, startIndex);
      if( contexts.size() == 0 ) {
         RepositoryConnection conn = null;
         try {
            conn = repository.getConnection();
            System.err.println("Getting contextIDs from repository...");
            contexts.addAll(conn.getContextIDs().asList());
            if( contexts.size() == 0 ) {
               System.err.println("no contexts");
               //contexts.add(Constants.PIPE_CONTEXT);
            }
            //System.err.println("numcontexts:"+primary.getContextIDs().asList() );
         } catch (RepositoryException e) {
            e.printStackTrace();
         } finally {
            try {
               conn.close();
            } catch (RepositoryException e) {
               e.printStackTrace();
            }            
         }
      }
      return contexts;
   }
   
   /**
    * Process 'args' for a set of Resources.
    * 
    * @param args - the arguments to a main()
    * @param startIndex - the first index of 'args' that names a Resource.
    * @return a set of contexts from args; if args empty populate with all contexts from 'repository'.
    */
   public static Set<Resource> getResources(String[] args, int startIndex) {
      Set<Resource> resources = new HashSet<Resource>();
      IPrefixMappings pmap = DefaultPrefixMappings.getInstance();
      for( int i = startIndex; i < args.length; i++ ) {
         String uri = pmap.expandQName(args[i]); // If a QName, use DefaultPrefixmappings to expand.
         resources.add(ValueFactoryImpl.getInstance().createURI(uri));
      }
      return resources;
   }
   
//   @Override
//   /**
//    * Assume false, but should be overridden for cases where it is useful.
//    */
//   public boolean useful() {
//   	return false;
//   }
}