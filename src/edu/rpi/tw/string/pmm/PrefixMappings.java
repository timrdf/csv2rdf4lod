package edu.rpi.tw.string.pmm;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import edu.rpi.tw.data.csv.querylets.PrefixMappingsQuerylet;
import edu.rpi.tw.data.rdf.sesame.query.QueryletProcessor;
import edu.rpi.tw.data.rdf.sesame.string.PrettyTurtleWriter;
import edu.rpi.tw.data.rdf.sesame.vocabulary.VANN;
import edu.rpi.tw.data.rdf.sesame.vocabulary.Vocabulary;
import edu.rpi.tw.data.rdf.utils.pipes.Constants;
import edu.rpi.tw.data.rdf.utils.pipes.starts.Cat;

/**
 * PrefixMappings stores prefix abbreviations for namespaces.
 * 
 * See PrefixMappings.graffle for method dependencies.
 * 
 * See NameFactory.getPrefixMappingNameTiny to name prefix mapping pairings.
 * 
 */
public class PrefixMappings implements IPrefixMappings {

   private static Logger logger = Logger.getLogger(PrefixMappings.class.getName());

   /** URI -> prefix */
   private TreeMap<String,String>    abbreviationFor;
   private TreeMap<String,Namespace> expansionOf;

   /**
    * Keep track of the prefixes used in any response for an abbreviation.
    * This can be accessed by clients to see which prefixes they need to display to users, etc.
    */
   protected HashMap<String,Integer> namespacesAbbreviated;
   
   //
   // Constructors
   //
   
   /**
    * 
    */
   public PrefixMappings() {
      super();
      this.abbreviationFor = new TreeMap<String,String>();
      this.expansionOf     = new TreeMap<String,Namespace>();
      this.namespacesAbbreviated = new HashMap<String,Integer>();
   }

   /**
    * 
    * @param namespaces
    */
   public PrefixMappings(Collection<Namespace> namespaces) {
      this();
      add(namespaces);
   }

   /**
    * Populate this PrefixMappings with all Namespaces defined in 'repository'.
    * 
    * @param repository - the repository from which to get Namespaces.
    */
   public PrefixMappings(Repository repository) {
      this();
      loadFromRepository(repository);
   }

   /**
    * Populate the PrefixMappings with the given Map from abbreviations to names.
    * NOTE: this is the opposite mapping direction than how PrefixMappings stores it.
    * 
    * @param prefixToNamespace - map from prefix string to the full URI it represents.
    */
   public PrefixMappings( Map<String,String> prefixToNamespace ) {
      this();
      for( String key : prefixToNamespace.keySet() ) {
         // Swapping keys and values TODO yuck
         try {
            put(prefixToNamespace.get(key),key);
         } catch (NamingException e) {
            e.printStackTrace();
         }
      }
   }

   /**
    * 
    * @param url - URL of RDF file containing VANN mappings.
    */
   public PrefixMappings(String url) {
      this();
      Repository rep = Cat.load(url);  // For Virtuoso
      loadFromRepository(rep);
   }
   
   /**
    * 
    * @param endpoint
    * @param namedGraph
    */
   public PrefixMappings(String endpoint, String namedGraph) {
      this();
      
      String query = 
         "PREFIX vann: <http://purl.org/vocab/vann/>\n"+
         "CONSTRUCT {\n"+
         " ?s vann:preferredNamespacePrefix ?prefix; vann:preferredNamespaceUri ?ns\n"+
         "}\n"+
         "WHERE {\n"+
         " GRAPH <NAMED_GRAPH> {\n"+
         "  ?s vann:preferredNamespacePrefix ?prefix; vann:preferredNamespaceUri ?ns\n"+
         " }\n"+
         "}";
      query = query.replaceAll("NAMED_GRAPH",namedGraph);
      
      Repository rep;
      try {
         rep = Cat.load(endpoint+"?default-graph-uri=&query="+URLEncoder.encode(query,"utf8")+
                                   "&format=application%2Frdf%2Bxml&debug=on&timeout=");  // For Virtuoso
         loadFromRepository(rep);
      } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
      }
   }
   
   
   //
   // Setting up prefixes
   //
   
   
   /**
    * 
    * @param repository
    */
   private void loadFromRepository(Repository repository) {
      try{
         RepositoryConnection con = repository.getConnection();
         //System.err.println(con.size((Resource)null)+" "+con.size(Constants.PIPE_CONTEXT));
         RepositoryResult<Namespace>    rr = con.getNamespaces();
         for( Namespace ns : rr.asList() ) {
            //System.err.println(ns.getPrefix() + " " + ns.getName());
            put(ns.getName(), ns.getPrefix());
         }
      }catch(Exception e) {
         e.printStackTrace();
      }
      
      PrefixMappingsQuerylet querylet = new PrefixMappingsQuerylet(Constants.PIPE_CONTEXT);
      QueryletProcessor.processQuery(repository, querylet);
      for( Namespace ns : querylet.get() ) {
         try {
            //System.err.println(ns.getPrefix() + " " + ns.getName());
            put(ns.getName(), ns.getPrefix());
         } catch (NamingException e) {
            e.printStackTrace();
         }
      }
   }
   
   
   /**
    * Add a prefix abbreviation for the given URI.
    * @param uri - the namespace
    * @param abbreviation - the namespace's abbreviation
    */
   public void addPrefix(String uri, String abbreviation) {
      try {
         put(uri,abbreviation);
      } catch (NamingException e) {
         e.printStackTrace();
      }
   }
   
   /**
    * 
    * @param vocab
    */
   public void addPrefix(Vocabulary vocab) {
      addPrefix(vocab.getBaseURI(), vocab.getPrefix());
   }

   /**
    * Add the given Namespaces to this PrefixMappings.
    * @param namespaces
    */
   public void add(Collection<Namespace> namespaces) {
      try {
         for( Namespace namespace : namespaces ) {
            put(namespace.getName(), namespace.getPrefix());
         }
      } catch (NamingException e) {
         e.printStackTrace();
      }
   }

   /**
    * @param uri,          e.g.  http://www.w3.org/1999/02/22-rdf-syntax-ns#
    * @param abbreviation, e.g. "rdf"
    */
   private void put(String uri, String abbreviation) throws NamingException {
      
      logger.finest("PrefixMappings.put("+uri+","+abbreviation+")");
      
      if( this.expansionOf.containsKey(abbreviation) && !this.expansionOf.get(abbreviation).equals(uri) ) {
         // The abbreviation is already used, and doesn't match the new URI.
         logger.warning("      \""+abbreviation+"\" already used for \""+this.expansionOf.get(abbreviation)+
               "\" (tried \""+uri+"\") PrefixMappings#put(String,String): WARNING: abbreviation ");
         throw new NamingException(abbreviation+"\" already used for \""+this.expansionOf.get(abbreviation));
      }
      if( this.abbreviationFor.containsKey(uri) && !this.abbreviationFor.get(uri).equals(abbreviation) ) {
         logger.warning("      "+uri+" already abbreviated with \""+
               this.abbreviationFor.get(uri)+"\" (tried \""+abbreviation+"\") PrefixMappings#put(String,String): WARNING");
         throw new NamingException(uri+"\" already abbreviated with \""+this.abbreviationFor.get(uri));
      }

      this.abbreviationFor.put(uri, abbreviation);
      this.expansionOf.put(abbreviation,new NamespaceImpl(abbreviation,uri));
   }

   
   //
   // <> ------ typical object-level accessors ------ <>
   //
   
   
   /**
    * @param uri
    * 
    * @return true if this PrefixMappings has an abbreviation for 'uri'.
    */
   public boolean hasPrefix(String uri) {
      return this.abbreviationFor.containsKey(uri);
   }

   /**
    * PrettyTurtleWriter.prefixMappingAsString(pmap.getNamespaces())
    * 
    * @return the URI namespaces and their abbreviations bundled as Sesame Namespaces.
    */
   public Collection<Namespace> getNamespaces() {
      HashSet<Namespace> ns = new HashSet<Namespace>();
      for( String name : abbreviationFor.keySet() ) {
         ns.add(new NamespaceImpl(this.abbreviationFor.get(name),name));
      }
      return ns;
   }

   /**
    * 
    */
   public String toString() {
      
      String string = PrettyTurtleWriter.prefixMappingAsString(getNamespaces());

      String nl = "";
      for( String ns : getAbbreviatedNamespaces() ) {
         string += nl + this.bestPrefixFor(ns) + ": " + ns + " (x " + namespacesAbbreviated.get(ns)+")";
         nl = "\n";
      }
      return string;
   }
   
   
   //
   // <> ------ typical object-level mutators ------ <>
   //
   
   
   /**
    * 
    */
   protected void clearNamespaces() {
      this.abbreviationFor.clear();
      this.expansionOf.clear();
   }
   
   
   //
   // <> ------ pretty accessors ------ <>
   //
   
   
   /**
    * @param uri - the uri
    */
   @Override
   public boolean canAbbreviate( String uri ) {
      boolean badForm = uri == null || uri.length() < 1;
      String best = bestPrefixFor(uri);
      if( !badForm ) {
         //System.err.println("canAbbreviate("+uri+")");
         //System.err.println(best + " " + best.length() + " " + uri + " " + uri.length());
      }
      //System.err.println(!badForm + " " + (best != null) + " " + best.length() + " " + uri.length() + "  " +(best.length() < uri.length()));
      // Handles case where abbreviation is longer than uri: 
      // e.g., (justtoseeifclassURIusespredicateNS http://scdb.wustl.edu/ns/Vote)
      return !badForm && best != null && (!best.equals(uri) || best.length() < uri.length());
   }// TODO: bestPrefixFor() throws exception when uri is length 0
   
   /**
    * @param uri
    * @return the longest URI that matches 'uri'
    */
   @Override
   public String bestPrefixFor( String uri ) {
      return this.abbreviationFor.get(bestNamespaceFor(uri));
   }

   /**
    * {@link IPrefixMappings#bestNamespaceFor(String)}
    * 
    * @param uri - e.g. http://my.name/ns#me
    * 
    * @return the BEST QName for the given URI (e.g. http://my.name/ns# not http://my.name/)
    */
   @Override
   public String bestNamespaceFor( String uri ) {
      
      logger.finest("bestPrefixFor("+ uri +")");
      
//      if( logger.isLoggable(Level.FINEST) ) {
//         try{ 
//            throw new IllegalStateException("what is calling this?");
//         }catch(Exception e) {
//            e.printStackTrace();
//         }
//      }
      
      String longestNamespace = "";
      for( String namespace : this.abbreviationFor.keySet() ) {
         logger.finest("   so far "+longestNamespace+", but uri.startsWith "+namespace+" ? "+uri.startsWith(namespace));
         if( uri.startsWith(namespace) && namespace.length() > longestNamespace.length()) {
            longestNamespace = namespace;
            logger.finest("  PrefixMappings found prefix " + "   " + this.abbreviationFor.get(longestNamespace));
         }
      }
      if( longestNamespace.length() > 0 ) {
         if( ! this.namespacesAbbreviated.containsKey(longestNamespace) ) {
            this.namespacesAbbreviated.put(longestNamespace, 1);
         }else {
            this.namespacesAbbreviated.put(longestNamespace, 1+this.namespacesAbbreviated.get(longestNamespace));
         }
         return longestNamespace;
      }else {
         String ns = guessNamespaceFor(uri);
         return ns;
      }
   }

   /**
    * Guess the namespace, without using any of the known prefix mappings.
    * 
    * e.g. 
    * http://blah.org/vocab/hasName -> http://blah.org/vocab/
    * http://blah.org/vocab#hasName -> http://blah.org/vocab
    * 
    * @param uri
    * @return a best guess at the namespace of uri
    */
   public static String guessNamespaceFor( String uri ) {
   	String hashBased = uri.replaceAll("#.*$", "#");
   	if( hashBased.length() < uri.length() ) {
   		return hashBased;
   	}else {
   		return uri.substring(0, uri.lastIndexOf("/")+1);
   	}
   }
   
   /**
    * 
    * @param resource - e.g. http://my.name/ns#me
    * @return prefix:localName OR original uri if no prefix.
    */
   public String bestQNameForR( Resource resource ) {
      return resource == null ? "null" : bestQNameFor(resource.stringValue());
      // TODO: "null" is never a best QName.
   }
   
   /**
    * 
    * @param uri - e.g. http://my.name/ns#me
    * @return prefix:localName OR original uri if no prefix.
    */
   @Override
   public String bestQNameFor( String uri ) {
      String bestQName     = uri;
      String bestNamespace = bestNamespaceFor(uri);
      if( !bestNamespace.equals("") ) { // TODO: call bestLocalName instead of redoing it.
         bestQName = this.abbreviationFor.get(bestNamespace) + ":" + uri.substring(bestNamespace.length(), uri.length()); 
      }
      //System.err.println("bestQNameFor "+uri+" is "+bestQName+" ov: "+abbreviationFor.get("http://open.vocab.org/terms/"));
      return bestQName;
   }

   /**
    * Cut off longest namespace that we know of.
    * If we dont' recognize the namespace, cut off to # or /, whichever is last.
    * 
    * @param uri - e.g. http://my.name/ns#me
    * @return my:me or http://my.name/ns#me if no appropriate namespace abbreviation.
    */
   @Override
   public String bestLocalNameFor( String uri ) {
      String bestLocalName = uri;
      
      String bestNamespace = bestNamespaceFor(uri);
      if( !bestNamespace.equals("") ) {
         bestLocalName = uri.substring(bestNamespace.length());
      }else {
         int hashLoc  = uri.lastIndexOf("#");
         int slashLoc = uri.lastIndexOf("/");
         int cutLoc = Math.max(hashLoc, slashLoc);
         if( cutLoc > 0 ) {
            bestLocalName = uri.substring(cutLoc+1);
         }
      }
      //System.err.println("\""+bestLocalName+"\""+" is being used as label for "+uri);
      return bestLocalName;
   }

   /**
    * @param uri - e.g. http://my.name/ns#my_stuff
    * @return "my stuff" or http://my.name/ns#my_stuff if no appropriate namespace abbreviation.
    */
   @Override
   public String bestLabelFor( String uri ) {
      //System.err.println(bestLocalNameFor(uri).replace("_", " "));
      return bestLocalNameFor(uri).replace("_"," ").replace("%20"," ");
   }

   /**
    * A hack so Saxon doesn't get confused between bestLabelFor(String) and bestLabelFor(Resource).
    * @param uri - e.g. http://my.name/ns#me
    * @return my:me or <http://my.name/ns#me>
    */
   public String bestQNameForU( Resource uri ) {
      return bestQNameFor(uri.stringValue());
   }

   /**
    * @param uri - e.g. http://my.name/ns#me
    * @return my:me or <http://my.name/ns#me>
    */
   public String bestQNameRef( URI uri ) {
   	return bestQNameRef(uri.stringValue());
   }

   /**
    * @param uri - e.g. http://my.name/ns#me
    * @return my:me or <http://my.name/ns#me>
    */
   public String bestQNameRef( String uri ) {
      String bestURIRef = bestQNameFor(uri);
      if( bestURIRef.length() == uri.length() ) {
         bestURIRef = "<"+bestURIRef+">";
      }
      return bestURIRef;
   }
   
   @Override
   public String tryQName( String uri ) {
   	return this.bestPrefixFor(uri) != null ? bestQNameFor(uri) : uri;
   }
   
   @Override
   public String tryQNameFallbackLocal( String uri ) {
      String curie = tryQName(uri);
      if( !curie.equals(uri) ) {
         return curie;
      }else {
         return bestLocalNameFor(uri);
      }
   }
   
   @Override
   public String tryQNameFallbackDomainAndLocal( String uri ) {
      String curie = tryQName(uri);
      if( !curie.equals(uri) ) {
         return curie;
      }else {
         return bestNamespaceFor(uri) + " " + bestLocalNameFor(uri);
      }
   }
   
   /**
    * A hack to trim off OWL, RDFS, AND RDF no matter what, then calls bestQNameFor(uri)
    * @deprecated - use {@link #bestQNameFor(uri)}
    * @param uri - e.g. http://my.name/ns#me
    * @return
    */
   public String bestQNameLabelFor( String uri ) {
      /*String qNameLabel = "";
      if( uri.startsWith("http://www.w3.org/2002/07/owl#") ) {
         qNameLabel = uri.substring(30);

      }else if( uri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#") ) {
         qNameLabel = uri.substring(43);

      }else if( uri.startsWith("http://www.w3.org/2000/01/rdf-schema#") ) {
         qNameLabel = uri.substring(37);
      }else {
         qNameLabel = bestQNameFor(uri);
      }*/
      return bestQNameFor(uri);
   }
   
   
   //
   // Talking back after an abbreviation
   //

   /**
    * @param abbreviation
    */
   public boolean usesAbbreviation(String abbreviation) {
      return this.expansionOf.containsKey(abbreviation);
   }
   
   /**
    * @param abbreviation (e.g., "rdfs")
    * 
    * @return the abbreviation-uri pairing (e.g., <"rdfs","http://www.w3.org/2000/01/rdf-schema#">)
    */
   public Namespace getNamespace(String abbreviation) throws javax.naming.NamingException {
      if( !this.expansionOf.containsKey(abbreviation) ) {
         throw new javax.naming.NamingException("abbreviation not specified: "+abbreviation);
      }
      return this.expansionOf.get(abbreviation);
   }
   
   /**
    * 
    * @param qname
    * @return
    */
   @Override
   public String expandQName(String qname) { // TODO: check if you actually have the expansion, throw exception.
      String uri = qname;
      //System.err.print("attempting to parse URL as "+RDFFormat.NTRIPLES+"... ");
      //new URL(qname);
      int pivot = qname.indexOf(":");
      String prefix = qname.substring(0, pivot);
      String local  = qname.substring(pivot+1,qname.length());
      //System.err.println(prefix + " " + local);
      if( this.expansionOf.get(prefix) != null ) {
         uri = this.expansionOf.get(prefix).getName() + local;
      }
      return uri;
   }
   
   /**
    * 
    * @return A set of URI namespaces that have been abbreviated during any call to this object.
    */
   public Set<String> getAbbreviatedNamespaces() {
      return new HashSet<String>(namespacesAbbreviated.keySet());
   }
   
   /**
    * {@link #getAbbreviatedNamespaces()} as Namespace, not String index.
    * 
    * @return
    */
   public Collection<Namespace> getAbbreviatedNamespacesAsNS() {
      Collection<Namespace> abbreviated = new HashSet<Namespace>();
      logger.fine("gathering "+getAbbreviatedNamespaces().size()+" abbreviated namespaces");
      for( String ns : getAbbreviatedNamespaces() ) {
         try {
            abbreviated.add(getNamespace(bestPrefixFor(ns)));
         }catch (NamingException e) {
            e.printStackTrace();
         }
      }
      return abbreviated;
   }
   
   /**
    * 
    * @return
    */
   public HashMap<String,Integer> getAbbreviatedNamespacesDistribution() {
      return new HashMap<String, Integer>(namespacesAbbreviated);
   }

   
   //
   // Saving out
   //
   
   /**
    * 
    * @param uri
    * @return
    */
   public String prefixDefinitionsFor(URI ... uri) {

      HashSet<String> abbreviated = new HashSet<String>();
      String definitions = "";

      String nl = "";
      for (URI term : uri) {
         String uriS = term.stringValue();
         String ns = this.bestNamespaceFor(uriS);
         if( canAbbreviate(uriS) && !abbreviated.contains(ns) ) {
            definitions = definitions + nl + "@prefix " + this.bestPrefixFor(uriS) +" <"+ ns +"> ."; 
            abbreviated.add(ns);
            nl = "\n";
         }else {

         }
      }
      return definitions + "\n\n";
   }

   /**
    * Add a repository's prefix definitions into the repository itself using the VANN vocabulary.
    * 
    * @param repository
    */
   public static void encodePrefixesAsVANN(Repository repository) {
      ValueFactory vf = ValueFactoryImpl.getInstance();
      Resource bundle = vf.createBNode();
      ArrayList<Resource> bnodes = new ArrayList<Resource>(); // just to pretty Sesame's ttl output.
      try {
         RepositoryConnection conn = repository.getConnection();
         for( Namespace namespace : conn.getNamespaces().asList()) {
            Resource pairing = vf.createBNode();
            bnodes.add(pairing);
            conn.add(pairing, VANN.preferredNamespacePrefix, vf.createLiteral(namespace.getPrefix()));
            conn.add(pairing, VANN.preferredNamespaceUri,    vf.createLiteral(namespace.getName()));
         }
         for( Resource pairing : bnodes ) {
            conn.add(bundle, RDFS.SEEALSO, pairing);
         }
         conn.commit();
         conn.close();
      } catch (RepositoryException e) {
         e.printStackTrace();
      }
   }
}