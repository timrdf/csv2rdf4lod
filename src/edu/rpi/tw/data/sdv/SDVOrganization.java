package edu.rpi.tw.data.sdv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.rpi.tw.string.NameFactory;

/**
 * Determines "SDV" organization of a file path based on directory conventions.
 * 
 * See https://github.com/timrdf/csv2rdf4lod-automation/wiki/SDV-organization
 *     https://github.com/timrdf/csv2rdf4lod-automation/wiki/Directory-Conventions
 *     
 *                 Conversion Root ----\____/
 * /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu
 * /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves                        *
 * /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version
 * /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1   *
 * /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/source
 * /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/manual
 * /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/automatic
 * /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/source/part-1
 * 
 *            Conversion Root ----\____/
 * E:\subversion\my-project\data\source\my-organization\my-canvases\version\mine
 */
public class SDVOrganization {

   private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SDVOrganization.class.getName());
   
   /**
    *              source         dataset                 version                something
    * "/source/" + source + "/" + dataset + "/version/" + version + "/source/" + 
    *                                                             + "/manual/" 
    *                                                             + "/automatic/" 
    *                                                             + "/publish/" 
    */
   
   //                  /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves
   //                                                                                 /version/experiment-1/source/part-1
   public static Pattern beyondPattern = Pattern.compile("^(.*/)source/([^/]+)/([^/]+)/version/([^/]+)/([^/]+)");
   
   /**
    *              source         dataset                 version 
    * "/source/" + source + "/" + dataset + "/version/" + version + ""$
    *                                                             + "/"$
    */
   public static Pattern cockpitPattern = Pattern.compile("^(.*/)source/([^/]+)/([^/]+)/version/([^/]+)/?");
   protected String version = null;
   
   /**
    * 
    */
   public static Pattern datasetPattern = Pattern.compile("^(.*/)source/([^/]+)/([^/]+)/?");
   private String dataset = null;
   
   /**
    * 
    */
   public static Pattern sourcePattern  = Pattern.compile("^(.*/)source/([^/]+)/?");
   private String source = null;
   
   
   // The path that we are analyzing.
   protected String filepath = null;
   protected String root     = null;
   protected String baseURI  = null;
   
   /**
    * 
    * @param baseURI
    * @param sourceID
    * @param datasetID
    * @param versionID
    */
   public SDVOrganization(String baseURI, String sourceID, String datasetID, String versionID) {
      super();
      this.baseURI = baseURI;
      this.source  = sourceID;
      this.dataset = datasetID;
      this.version = versionID;
   }
   
   /**
    * 
    * @param filepath
    */
   public SDVOrganization(String filepath) {
      this(filepath, null);
   }
   
   /**
    * 
    * @param filepath
    * @param baseURI
    */
   public SDVOrganization(String filepath, String baseURI) {
      super();
      if( filepath != null && filepath.length() > 0 ) {
         //String uriFilePath = new File(filepath).toURI().toString();
         //logger.fine(filepath + " -> File -> URI: "+ uriFilePath);
         this.filepath = filepath;
         type();
      }
      if( baseURI != null && baseURI.length() > 0 ) {
         this.baseURI = baseURI;
      }
   }
   
   /**
    * Taken directly from "is-pwd-a.sh --types"
    * 
    * See https://github.com/timrdf/csv2rdf4lod-automation/blob/master/bin/util/is-pwd-a.sh#L21
    *
    */
   public enum DirectoryType {
      dev                  ("cr:dev"),                   // Not a production data directory
      
      data_root            ("cr:data-root"),             //
      
      source               ("cr:source"),                //
      directory_of_datasets("cr:directory-of-datasets"), //
      dataset              ("cr:dataset"),               //
      directory_of_versions("cr:directory-of-versions"), //
      conversion_cockpit   ("cr:conversion-cockpit"),    //
      
      beyond_cockpit       ("cr:beyond-cockpit"),        //
      
      unknown              ("unknown");                  //
      
      private final String label;
      DirectoryType(String label) {
         this.label = label;
      }
      public String toString() {
         return this.label;
      }
   }
   
   /**
    * Suit https://github.com/timrdf/csv2rdf4lod-automation/blob/master/bin/cr-pwd-type.sh
    * 
    * @return 
    */
   public DirectoryType type() {
      
      DirectoryType type = null;
      
      if( this.filepath != null ) {
         Matcher matcher = beyondPattern.matcher(this.filepath);
         while( matcher.find() ) {
            this.root    = matcher.group(1);
            this.source  = matcher.group(2);
            this.dataset = matcher.group(3);
            this.version = matcher.group(4);
            type = SDVOrganization.DirectoryType.beyond_cockpit;
         }
   
         if( type == null ) {
            matcher = cockpitPattern.matcher(this.filepath);
            while( matcher.find() ) {
               this.root    = matcher.group(1);
               this.source  = matcher.group(2);
               this.dataset = matcher.group(3);
               this.version = matcher.group(4);
               type = SDVOrganization.DirectoryType.conversion_cockpit;
            }
         }
         
         if( type == null ) {
            matcher = datasetPattern.matcher(this.filepath);
            while( matcher.find() ) {
               this.root    = matcher.group(1);
               this.source  = matcher.group(2);
               this.dataset = matcher.group(3);
               type = SDVOrganization.DirectoryType.dataset;
            }
         } 
            
         if( type == null ) {
            matcher = sourcePattern.matcher(this.filepath);
            while( matcher.find() ) {
               this.root    = matcher.group(1);
               this.source  = matcher.group(2);
               type = SDVOrganization.DirectoryType.source;
            }
         }
      }
      
      if( type == null ) {
         logger.warning("Could not determine SDV directory type for file path \""+this.filepath+"\"\n");
      }else {
         logger.fine("File path is a " + type + " " + this.filepath);
      }
      
      return type != null ? type : SDVOrganization.DirectoryType.unknown;
   }
   
   /**
    * 
    * @return
    */
   public String getConversionRoot() {
      return this.root;
   }
   
   /**
    * 
    * @return
    */
   public String getPathConversionRoot() {
      return getConversionRoot();
   }
   
   /**
    * 
    */
   public String getURIBase() {
      return this.baseURI;
   }
   
   /**
    * 
    * @return the conversion:source_identifier if it appears, null o/w.
    */
   public String getSourceID() {
      return this.source;
   }
   
   /**
    * 
    * @return
    */
   public String getPathUpToSource() {
      return getConversionRoot() + NameFactory.slashIfThere("source", this.source);
   }
   
   /**
    * 
    * @return
    */
   public String getURIUpToSource() {
      return this.baseURI != null && this.baseURI.startsWith("http") 
             ? this.baseURI + "/" + NameFactory.slashIfThereShort("source", this.source) 
             : null;
   }
   
   /**
    * 
    * @return the conversion:dataste_identifier if it appears, null o/w.
    */
   public String getDatasetID() {
      return this.dataset;
   }
   
   /**
    * 
    * @return
    */
   public String getPathUpToDataset() {
      return getPathUpToSource() + NameFactory.slashIfThere(this.dataset);
   }
   
   /**
    * 
    * @return
    */
   public String getURIUpToDataset() {
      return getURIUpToSource() != null 
             ? getURIUpToSource() + "/" + NameFactory.slashIfThereShort("dataset", this.dataset) 
             : null;
   }
   
   /**
    * 
    * @param id
    */
   public void overrideVersionID(String id) {
      this.version = id;
   }
   
   /**
    * 
    * @return the conversion:version_identifier if it appears, null o/w.
    */
   public String getVersionID() {
      return this.version != null && this.version.length() == 0 ? null : this.version;
   }
   
   /**
    * 
    * @return
    */
   public String getPathUpToVersion() {
      return getPathUpToDataset() != null 
             ? getPathUpToDataset() + NameFactory.slashIfThere("version", this.version) 
             : null;
   }
   
   /**
    * 
    * @return
    */
   public String getURIUpToVersion() {
      return getURIUpToDataset() != null 
             ? getURIUpToDataset() + "/" + NameFactory.slashIfThereShort("version", this.version) 
             : null;
   }
   
   /**
    * 
    * @return
    */
   public String getURIUpToVersion(String versionID) {
      return getURIUpToDataset() != null 
             ? getURIUpToDataset() + "/" + NameFactory.slashIfThereShort("version", versionID) 
             : null;
   }
   
   
   /**
    * 
    */
   @Override
   public String toString() {
      return 
            this.filepath +"\n"+
            this.baseURI + "\n"+
            type() + "\n" +
            "root    " + this.root + "\n"+
            "source  " + this.source + "\n"+
            "dataset " + this.dataset + "\n"+
            "version " + this.version + "\n"+
            this.getPathUpToVersion() + "\n"+
            this.getURIUpToVersion();
   }
   
   /**
    * 
    * @param args
    */
   public static void main(String args[]) {
      
      if( args.length == 0 ) {
         String[] paths = {
            "/Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu",
            "/Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves",
            "/Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version",
            "/Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1",
            "/Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/source",
            "/Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/manual",
            "/Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/automatic",
            "/Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/source",
            "/Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/source/part-1"
         };
         for( int i = 0; i < paths.length; i++ ) {
            process(paths[i]);
            process(paths[i]+"/");
         }
      }else {
         for( int i = 0; i < args.length; i++ ) {
            SDVOrganization sdv = new SDVOrganization(args[i]);
            System.out.println(sdv.type());
         }
      }
      /*
       source:             /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu
       source:             /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/
       dataset:            /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves
       dataset:            /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/
       dataset:            /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version
       dataset:            /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/
       conversion_cockpit: /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1
       conversion_cockpit: /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/
       beyond_cockpit:     /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/source
       beyond_cockpit:     /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/source/
       beyond_cockpit:     /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/manual
       beyond_cockpit:     /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/manual/
       beyond_cockpit:     /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/automatic
       beyond_cockpit:     /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/automatic/
       beyond_cockpit:     /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/source
       beyond_cockpit:     /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/source/
       beyond_cockpit:     /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/source/part-1
       beyond_cockpit:     /Users/me/projects/twc-ieeevis/data/source/ieeevis-tw-rpi-edu/data-carves/version/experiment-1/source/part-1/
       */
   }
   
   /**
    * For use by main()
    * 
    * @param path
    */
   private static void process(String path) {
      SDVOrganization sdv = new SDVOrganization(path);
      System.out.println(sdv.type() + ": " + path);
      System.out.println("  to source:  " + sdv.getPathUpToSource());
      System.out.println("  to dataset: " + sdv.getPathUpToDataset());
      System.out.println("  to version: " + sdv.getPathUpToVersion());
   }
}