package edu.rpi.tw.data.rdf.utils.pipes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.nquads.NQuadsWriter;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.rio.trig.TriGWriter;
import org.openrdf.rio.trix.TriXWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.memory.MemoryStore;

import edu.rpi.tw.data.rdf.sesame.query.impl.DefaultQuerylet;
import edu.rpi.tw.data.rdf.sesame.string.PrettyTurtleWriter;
import edu.rpi.tw.data.rdf.sesame.vocabulary.TetherlessWorld;
import edu.rpi.tw.string.pmm.DefaultPrefixMappings;
import edu.rpi.tw.string.pmm.PrefixMappings;

/**
 * 
 */
public class Constants {

   public static Resource  PIPE_CONTEXT = ValueFactoryImpl.getInstance().createURI(TetherlessWorld.BASE_URI+"PIPE_CONTEXT");
   //public static RDFFormat DEFAULT_PIPELINE_FORMAT         = RDFFormat.RDFXML;
   //public static String    DEFAULT_PIPELINE_EXTENSION      = "rdf";
   //public static String    DEFAULT_PIPELINE_EXTENSION_JENA = "RDF/XML";
   
   public static RDFFormat DEFAULT_PIPELINE_FORMAT         = RDFFormat.NTRIPLES;
   public static String    DEFAULT_PIPELINE_EXTENSION      = "nt";
   public static String    DEFAULT_PIPELINE_EXTENSION_JENA = "N-TRIPLE";
   
   public static PrefixMappings pmap = new DefaultPrefixMappings();
   
   /**
    * @param extension - the file extension for the desired serialization.
    * @param out - the OutputStream to which the RDFHandler should write.
    * @return an RDFHandler
    */
   public static RDFHandler handlerForFileExtension(String extension, OutputStream out) {
      
      RDFHandler writer = null;
      
      if ( "ttl".equalsIgnoreCase(extension) ) {
         writer = new TurtleWriter(out);
      }else if( "pttl".equalsIgnoreCase(extension) ) {
         writer = new PrettyTurtleWriter(out);
      }else if( "trig".equalsIgnoreCase(extension) ) {
         writer = new TriGWriter(out);
      }else if( "nquads".equalsIgnoreCase(extension) ) {
         writer = new NQuadsWriter(out);
      }else if( "trix".equalsIgnoreCase(extension) ) {
         writer = new TriXWriter(out);
      }else if( "nt".equalsIgnoreCase(extension)) {
         writer = new NTriplesWriter(out);
      }else {
         // "rdf/xml", "rdf"
         writer = new RDFXMLPrettyWriter(out);
      } 
      
      return writer;
   }
   
   public static RDFFormat formatForFileExtension(String extension) {
      //System.err.println("RDFFormat for extension >"+extension+"<");
      RDFFormat format = null;
      if ( "ttl".equalsIgnoreCase(extension) ) {
         format = RDFFormat.TURTLE;
      }else if( "n3".equalsIgnoreCase(extension) ) {
         format = RDFFormat.N3;
      }else if( "pttl".equalsIgnoreCase(extension) ) {
         format = RDFFormat.TURTLE;
      }else if( "trig".equalsIgnoreCase(extension) ) {
         format = RDFFormat.TRIG;
      }else if( "trix".equalsIgnoreCase(extension) ) {
         format = RDFFormat.TRIX;
      }else if( "nt".equalsIgnoreCase(extension)) {
         format = RDFFormat.NTRIPLES;
      }else if( "nq".equalsIgnoreCase(extension)) {
         format = RDFFormat.NQUADS;
      }else {
         //System.err.println("going with : else");
         // "rdf/xml", "rdf"
         format = RDFFormat.RDFXML;
      } 
      //System.err.println("going with : " + format);
      return format;
   }
   
   /**
    * 
    * @param filename
    * @return
    */
   public static RDFFormat formatForFilename(String filename) {
      return formatForFileExtension(filename.substring(filename.lastIndexOf(".")+1));
   }
   
   /**
    * @param extension - the file extension for the desired serialization.
    * @return an RDFHandler appropriate for the given file extension, prepared to write to System.out.
    */
   public static RDFHandler forFileExtension(String extension) {
      return Constants.handlerForFileExtension(extension, System.out);
   }
   
   /**
    * 
    * @param repository
    * @param args
    * @return
    */
   public static List<Resource> getEnumeratedOrEnumerate(Repository repository, String[] args) {
      return getEnumeratedOrEnumerate(repository, args, 2);
   }
   
   /**
    * Parse an argument string array and return a Set of Resources representing the contexts. If no contexts are
    * enumerated in the argument array, return all contexts in the repository.
    * 
    * For example,
    *  "usage: Wc serverURL repositoryID [context ...]"
    *  
    * could be handled with getEnumeratedOrEnumerate(Repository repository, String[] args) 
    * because the default startIndex is 2.
    *  
    *  "usage: DumpR serverURL repositoryID directoryPath [context ...]"
    * could be handled with getEnumeratedOrEnumerate(Repository repository, String[] args, 3)
    * 
    * @param repository
    * @param args
    * @param startIndex
    * @return a set of contexts listed in 'args' or all in repository if none are listed.
    */
   public static List<Resource> getEnumeratedOrEnumerate(Repository repository, String[] args, int startIndex) {
      ArrayList<Resource> contexts = null;
      if( startIndex < args.length ) {
         contexts = new ArrayList<Resource>();
         PrefixMappings pmap = DefaultPrefixMappings.getInstance();
         for( int i=startIndex; i < args.length; i++ ) {
            String resource = pmap.expandQName(args[i]);
            contexts.add(ValueFactoryImpl.getInstance().createURI(resource));
         }
      }else {
         RepositoryConnection conn = null;
         try {
            conn = repository.getConnection();
            System.err.println("Getting contextIDs ...");
            contexts = new ArrayList<Resource>(conn.getContextIDs().asList()); // TODO: change from Resource to URI
         } catch (RepositoryException e) {
            e.printStackTrace();
         }finally {
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
    * Do same thing as DefaultQuerylet#getContexts, but add PIPE_CONTEXT if empty results.
    * 
    * @param args
    * @param startIndex
    * @param repository
    * @return
    */
   public static Set<Resource> getEnumeratedOrPipe(String[] args, int startIndex, Repository repository) {
      Set<Resource> contexts = DefaultQuerylet.getContexts(args, startIndex, repository);
      if( contexts.size() == 0 ) {
         contexts.add(PIPE_CONTEXT);
      }
      return contexts;
   }
   
   /**
    * @return a local-memory Repository containing the RDF from System.in in null context.
    */
   public static Repository getPipeRepository() {
      return getPipeRepository(PIPE_CONTEXT);
   }

   /**
    * @param destinationContext - the context to place the RDF.
    * @return local-memory Repository containing the RDF from System.in.
    */
   public static Repository getPipeRepository( Resource destinationContext ) {
      Repository repository = new SailRepository(new MemoryStore());
      try {
         repository.initialize();
      } catch (RepositoryException e) {
         e.printStackTrace();
      }

      RepositoryConnection conn = null;
      try {
         conn = repository.getConnection();
         conn.add(System.in, "", Constants.DEFAULT_PIPELINE_FORMAT, destinationContext);
         conn.commit();
      } catch (RepositoryException e) {
         e.printStackTrace();
      } catch (RDFParseException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }finally {
         try {
            conn.close();
         } catch (RepositoryException e) {
            e.printStackTrace();
         }
      }
      return repository;
   }
}