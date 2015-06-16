package edu.rpi.tw.data.rdf.utils.pipes.starts;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.sail.memory.MemoryStore;

import edu.rpi.tw.data.rdf.utils.pipes.Constants;

/**
 * Accept a series of file pathnames and URLs of RDF files, concatenate them into a single model, and print to sysout.
 * Strip all named graph designations that may be specified in trig and trix files.
 * 
 * Inspired by jena.rdfcopy, but it cannot handle trig named graph designations.
 * TODO: varargs for file could be used as a cat, which would do part of PlusMinus's job.
 * 
 * 
 * Cat my.rdf
 *   reads my.rdf as rdf/xml and outputs as rdf/xml
 *   
 * Cat my.blah
 *   reads my.blah as rdf/xml and outputs as rdf/xml
 *   
 * Cat my.ttl rdf/xml
 *   reads my.ttl as turtle and outputs as rdf/xml
 * 
 * Cat my.nt ttl
 *   reads my.nt as n-triples and outputs as turtle
 *   
 * Cat - rdf/xml ttl
 *   reads stdin as rdf/xml and outputs as turtle
 *   
 * Cat - rdf/xml nt my.ttl other.ttl
 *   concatenates stdin (as rdf/xml), my.ttl (as turtle) and other.ttl (as turtle); outputs as n-triples
 */
public class Cat {
   
   private static Logger logger = Logger.getLogger(Cat.class.getName());
   
   public static Resource DEFAULT_CONTEXT = Constants.PIPE_CONTEXT;
   
   public static boolean ENCOUNTERED_PARSE_ERROR = false;
   
   public static final String USAGE = "Cat [filePath | URL]*";   
   /**
    * e.g., Cat foaf/examples/hendler.foaf.rdf http://www.w3.org/People/Berners-Lee/card
    * 
    * @param args - [filePath | URL]*
    */
   public static void main(String[] args) {
      
      if( args.length < 1 ) {
         //System.err.println("usage: "+USAGE);
      }
      
      Repository repository = new SailRepository(new MemoryStore());
      try {
         repository.initialize();
      } catch (RepositoryException e) {
         e.printStackTrace();
      }

      RepositoryConnection conn = null;
      try {
         
         // If there is content on sysin, include it in the model union.
         if( System.in.available() > 0 ) { // TODO:  this does not work.
            conn = repository.getConnection();
            conn.add(System.in, "", Constants.DEFAULT_PIPELINE_FORMAT, Constants.PIPE_CONTEXT);
            conn.commit();
            conn.close();
         }
         
         // Concatenate all RDF from filepaths and URLs designated as arguments.
         for( int i=0; i < args.length; i++ ) {
            load(args[i], repository, Constants.PIPE_CONTEXT);
         }
         conn = repository.getConnection();
         conn.export(Constants.handlerForFileExtension(Constants.DEFAULT_PIPELINE_EXTENSION, System.out),Constants.PIPE_CONTEXT);
      } catch (RepositoryException e) {
         e.printStackTrace();
      } catch (RDFHandlerException e) {
         e.printStackTrace();
      } catch (RDFParseException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }finally {
         if( conn != null ) {
            try {
               conn.close();
            } catch (RepositoryException e) {
               e.printStackTrace();
            }
         }
      }
   }
   
   /**
    * 
    * @param location - the file/URL to load. If null, just return an initialized in-memory Repository.
    * @param context - the context to load 'location' into.
    * 
    * @return
    */
   public static Repository load(String location, Resource context) {
      Repository rep = new SailRepository(new MemoryStore());
      try {
         rep.initialize();
      } catch (RepositoryException e) {
         e.printStackTrace();
      }
      if( location != null ) {
         Cat.load(location, rep, context);
      }
      return rep;
   }
   
   /**
    * Create a repository populated with the given location, 
    * placed in the context Constants.PIPE_CONTEXT.
    * 
    * @param location
    * 
    * @return a new Repository with context Constants.PIPE_CONTEXT containing RDF at file/URL 'location'.
    */
   public static Repository load(String location) {
      return load(location, Constants.PIPE_CONTEXT);
   }
   
   /**
    * 
    * @param location
    * @param repository
    */
   public static void load(String location, Repository repository) {
   	load(location, repository, Cat.DEFAULT_CONTEXT);
   }
   
   /**
    * Load the location (file path or URL) of an RDF file into the given initialized 'repository'.
    * 
    * @param location - a file path of URL of an RDF file.
    * @param repository - an initialized Repository to which to add the RDF file.
    */
   public static void load(String location, Repository repository, Resource context) {
      
      // TODO: see http://rdf4j.org/sesame/2.7/docs/users.docbook?view#chapter-rio4
      
   	//
   	// Debug with:
   	//   java \
   	//   -Djava.util.logging.config.file=$CSV2RDF4LOD_HOME/bin/logging/$CSV2RDF4LOD_CONVERT_DEBUG_LEVEL.properties \
   	//   edu.rpi.tw.data.rdf.utils.pipes.starts.Cat
   	//
   	
      logger.info("load(location,repository): "+location+ " --> " +repository);
      //System.err.println("load(location,repository): "+repository);
      
      if( location == null || location.length() == 0 ) {
         return;
      }
      
      /* :-( if( repository == null ) {
         repository = load(location); // creates, initializes, and calls load(String location, Repository repository)
      }*/
      
      RDFFormat format = Constants.formatForFilename(location);
      logger.info("location requested to load: "+ location+"\n"+
                  "format guess: " + format+" / "+
                         format.getName()+" / "+
                         format.getFileExtensions());
      RepositoryConnection conn = null;
      int attempts = 0;
      boolean success = false;
      try {
         conn = repository.getConnection();
         
         // http://sourceforge.net/mailarchive/message.php?msg_id=31982163
         // all of the settings classes are all in the org.openrdf.rio.helpers package right now
         conn.getParserConfig().addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
         // Attempt as local file.

         File file = new File(location);
         String nonURIPath = location.replaceAll("^file:", "");
         if( !file.exists() ) {
            file = new File(nonURIPath);
         }
         if( file.exists() ) {
            
            try {
               attempts++;
               logger.fine("attempting to parse File as guessed "+Constants.formatForFilename(location)+"... "+file.getAbsolutePath());
               logger.fine("size before: "+conn.size(context) + " in context " + context);

               // This was approach before adding conversion:includes:
               //primary.add(new File(location), "file:/"+location, Constants.formatForFilename(location), context);
               //
               conn.add(file, null, Constants.formatForFilename(location), context);
               
               logger.fine("added with guessed format");
               logger.fine("after: "+conn.size(context));
               conn.commit();
               success = true;
            } catch (RepositoryException e) {
               e.printStackTrace();
            } catch (RDFParseException e) {
               ENCOUNTERED_PARSE_ERROR = true;
               System.err.println(location);
               logger.finer(Constants.formatForFilename(location) +" failed");
            } catch (IOException e) {
               e.printStackTrace();
            } catch (Exception e) {
               e.printStackTrace();
            }
            
            logger.fine("success after guess: " + success);
            
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
            if( !success ) {
               try {
                  attempts++;
                  logger.fine("attempting to parse File as "+RDFFormat.TURTLE+"... ");
                  logger.finer("before turtle parse: "+conn.size(context));
                  conn.add(file, null, RDFFormat.TURTLE, context);
                  conn.commit();
                  success = true;
                  logger.finer("after turtle parse:  "+conn.size(context));
               } catch (RDFParseException e) {
                  ENCOUNTERED_PARSE_ERROR = true;
                  logger.finer("failed.");
                  e.printStackTrace();
               } catch (RepositoryException e) {
                  logger.finer("failed.");
               } catch (MalformedURLException e) {
                  logger.finer("failed.");
               } catch (IOException e) {
                  logger.finer("failed.");
               } catch (Exception e) {
                  logger.finer("failed.");
               }
            }else {
               logger.finer("We're good with format "+format);
            }
            
            logger.fine("success after turtle: " + success);
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
            
            
            
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
            if( !success ) {
               try {
                  attempts++;
                  logger.finer("attempting to parse File as "+RDFFormat.NTRIPLES+"... ");
                  conn.add(file, null, RDFFormat.NTRIPLES, context);
                  conn.commit();
                  success = true;
               } catch (RDFParseException e) {
                  ENCOUNTERED_PARSE_ERROR = true;
                  logger.finer("failed.");
               } catch (RepositoryException e) {
                  logger.finer("failed.");
               } catch (MalformedURLException e) {
                  logger.finer("failed.");
               } catch (IOException e) {
                  logger.finer("failed.");
               } catch (Exception e) {
                  logger.finer("failed.");
               }
            }
            
            logger.fine("success after ntriples: " + success);
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
            
            
            
            
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
            if( !success ) {
               try {
                  attempts++;
                  logger.finer("attempting to parse File as "+RDFFormat.NQUADS+"... ");
                  conn.add(file, null, RDFFormat.NQUADS, context);
                  conn.commit();
                  success = true;
               } catch (RDFParseException e) {
                  ENCOUNTERED_PARSE_ERROR = true;
                  logger.finer("failed.");
               } catch (RepositoryException e) {
                  logger.finer("failed.");
               } catch (MalformedURLException e) {
                  logger.finer("failed.");
               } catch (IOException e) {
                  logger.finer("failed.");
               } catch (Exception e) {
                  logger.finer("failed.");
               }
            }
            
            logger.fine("success after nquads: " + success);
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
            
            
            
            
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
            if( !success ) {
               try {
                  attempts++;
                  logger.finer("attempting to parse File as "+RDFFormat.RDFA+"... ");
                  conn.add(file, null, RDFFormat.RDFA, context);
                  conn.commit();
                  success = true;
               } catch (RDFParseException e) {
                  ENCOUNTERED_PARSE_ERROR = true;
                  logger.finer("failed.");
               } catch (RepositoryException e) {
                  logger.finer("failed.");
               } catch (MalformedURLException e) {
                  logger.finer("failed.");
               } catch (IOException e) {
                  logger.finer("failed.");
               } catch (Exception e) {
                  logger.finer("failed.");
               }
            }
            
            logger.fine("success after rdfa: " + success);
            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
            
            
         }else {
            
            // URI does not exist as a local file.
            
            try {
               attempts++;
               logger.finer("attempting to parse URL as "+Constants.formatForFilename(location)+"... ");
               conn.add(new URL(location), location, Constants.formatForFilename(location), context);
               conn.commit();
               success = true;
            } catch (RDFParseException e) {
               ENCOUNTERED_PARSE_ERROR = true;
               logger.finer(Constants.formatForFilename(location) +" failed"); e.printStackTrace();
            } catch (RepositoryException e) {
            	logger.finer(Constants.formatForFilename(location) +" failed."); e.printStackTrace();
            } catch (MalformedURLException e) {
            	logger.finer(Constants.formatForFilename(location) +" failed."); e.printStackTrace();
            } catch (IOException e) {
            	logger.finer(Constants.formatForFilename(location) +" failed."); e.printStackTrace();
            } catch (NullPointerException e ) {
            	logger.finer(Constants.formatForFilename(location) +" failed."); e.printStackTrace();
            } catch (Exception e) {
               logger.finer("failed.");
            }
            
            if(!success) {
               try {
                  attempts++;
                  logger.finer("attempting to parse URL as "+RDFFormat.RDFXML+"... ");
                  conn.add(new URL(location), location, RDFFormat.RDFXML, context);
                  conn.commit();
                  success = true;
               } catch (RDFParseException e) {
                  ENCOUNTERED_PARSE_ERROR = true;
                  logger.finer("failed.");
               } catch (RepositoryException e) {
               	logger.finer("failed.");
               } catch (MalformedURLException e) {
               	logger.finer("failed.");
               } catch (IOException e) {
               	logger.finer("failed.");
               } catch (Exception e) {
                  logger.finer("failed.");
               }
            }
            if(!success) {
               try {
                  attempts++;
                  logger.finer("attempting to parse URL as "+RDFFormat.TURTLE+"... ");
                  conn.add(new URL(location), location, RDFFormat.TURTLE, context);
                  conn.commit();
                  success = true;
               } catch (RDFParseException e) {
                  ENCOUNTERED_PARSE_ERROR = true;
                  logger.finer("failed.");
               } catch (RepositoryException e) {
               	logger.finer("failed.");
               } catch (MalformedURLException e) {
               	logger.finer("failed.");
               } catch (IOException e) {
               	logger.finer("failed.");
               } catch (Exception e) {
                  logger.finer("failed.");
               }
            }
            if(!success) {
               try {
                  attempts++;
                  logger.finer("attempting to parse URL as "+RDFFormat.N3+"... ");
                  conn.add(new URL(location), location, RDFFormat.N3, context);
                  conn.commit();
                  success = true;
               } catch (RDFParseException e) {
                  ENCOUNTERED_PARSE_ERROR = true;
                  logger.finer("failed.");
               } catch (RepositoryException e) {
               	logger.finer("failed.");
               } catch (MalformedURLException e) {
               	logger.finer("failed.");
               } catch (IOException e) {
               	logger.finer("failed.");
               } catch (Exception e) {
                  logger.finer("failed.");
               }
            }
            if(!success) {
               try {
                  attempts++;
                  logger.finer("attempting to parse URL as "+RDFFormat.NTRIPLES+"... ");
                  conn.add(new URL(location), location, RDFFormat.NTRIPLES, context);
                  conn.commit();
                  success = true;
               } catch (RDFParseException e) {
                  ENCOUNTERED_PARSE_ERROR = true;
                  logger.finer("failed.");
               } catch (RepositoryException e) {
               	logger.finer("failed.");
               } catch (MalformedURLException e) {
               	logger.finer("failed.");
               } catch (IOException e) {
               	logger.finer("failed.");
               } catch (Exception e) {
                  logger.finer("failed.");
               }
            }
            
            if(!success) {
               try {
                  attempts++;
                  logger.finer("attempting to parse URL as "+RDFFormat.TRIG+"... ");
                  conn.add(new URL(location), location, RDFFormat.TRIG, context);
                  conn.commit();
                  success = true;
               } catch (RDFParseException e) {
                  ENCOUNTERED_PARSE_ERROR = true;
                  logger.finer("failed.");
               } catch (RepositoryException e) {
               	logger.finer("failed.");
               } catch (MalformedURLException e) {
               	logger.finer("failed.");
               } catch (IOException e) {
               	logger.finer("failed.");
               } catch (Exception e) {
                  logger.finer("failed.");
               }
            }
            
            if(!success) {
               try {
                  attempts++;
                  logger.finer("attempting to parse URL as "+RDFFormat.TRIX+"... ");
                  conn.add(new URL(location), location, RDFFormat.TRIX, context);
                  conn.commit();
                  success = true;
               } catch (RDFParseException e) {
                  ENCOUNTERED_PARSE_ERROR = true;
                  logger.finer("failed.");
               } catch (RepositoryException e) {
               	logger.finer("failed.");
               } catch (MalformedURLException e) {
               	logger.finer("failed.");
               } catch (IOException e) {
               	logger.finer("failed.");
               } catch (Exception e) {
                  logger.finer("failed.");
               }
            }
         }
      } catch (RepositoryException e1) {
         e1.printStackTrace();
      } finally {
         logger.finer("load attempts: " + attempts);
         if( conn != null ) {
            try {
               logger.finer("load size " + conn.size(context) + "in context " + context);
               conn.close();
            } catch (RepositoryException e) {
               e.printStackTrace();
            }
         }
      }
   }

   public static long size(Repository linksViasRep) {
      long size = 0;
      try {
         RepositoryConnection conn = linksViasRep.getConnection();
         size = conn.size();
         conn.close();
      } catch (RepositoryException e) {
         e.printStackTrace();
      }
      return size;
   }
}