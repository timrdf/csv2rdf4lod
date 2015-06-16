package edu.rpi.tw.string;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import edu.rpi.tw.data.rdf.sesame.vocabulary.TetherlessWorld;

/**
 * Provide names.
 */
public class NameFactory {
   
   private static HashMap<String, Integer>   typeCounts = new HashMap<String, Integer>();
   private static HashMap<String, IDManager> typeIDManagers = new HashMap<String, IDManager>();
   
   public static final String[] monthName = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
   
	private static Pattern domainPattern = Pattern.compile("(http://[^/]*)/.*$");
   private static Pattern purlPattern   = Pattern.compile("(http://purl.org/[^/]*)/.*$");

   public enum NameType  {
      /** e.g. 'type'_27_feb_2009_1235761123899_ms */
      MILLISECOND_STARTED_TO_DAY(TetherlessWorld.D + "type_ms_started_to_day"),
      
      /** e.g. 'type'_27_feb_2009_8-59_1235761123899_ms */
      MILLISECOND_STARTED_TO_MINUTE(TetherlessWorld.D + "type_ms_started_to_minute"),
      
      /** e.g. 'type'_f39cd2ad-8ff3-4fb5-b028-01bcd11557a2 */
      UUID(TetherlessWorld.D + "type_uuid"),
      
      /** e.g. 'type'_1, 'type'_2 */
      INCREMENTING(TetherlessWorld.D + "type_incrementing");
      
      private final URI datatype;

      /**
       * Create a name type.
       * @param 
       */
      NameType(String uri){
         this.datatype = ValueFactoryImpl.getInstance().createURI(uri);
      }

      /**
       * @return 
       */
      public URI getDataTypeURI()  {
         return datatype;
      }
   }
   
   public static SimpleDateFormat today              = new SimpleDateFormat("yyyy-MM-dd");
   public static SimpleDateFormat todayHumanFriendly = new SimpleDateFormat("yyyy-MMM-dd");
   
   /**
    * 
    * @param baseURI
    * @param type
    * @param nameType
    * @return
    */
   public static Resource getResource(String baseURI, String type, NameType nameType) {
      String name = getFullName(baseURI, type, nameType);
      return ValueFactoryImpl.getInstance().createURI(name);
   }
   
   
   /**
    * @param baseURI - a conversion:AbstractDatset URI, 
    *                  e.g. "http://ieeevis.tw.rpi.edu/source/us/dataset/data-carves"
    *                  
    * @param humanFriendlyMonth - name month with e.g. "Oct" instead of "10". 
    *                             date +%Y-%b-%d e.g. "2013-Oct-09"
    *                             date +%Y-%m-%d e.g. "2013-10-09"
    */
   public static String getTodayVersionName(String baseURI, boolean humanFriendlyMonth) {
      return slashIfThere(baseURI, getTodayVersionName(humanFriendlyMonth));
   }
   
   /**
    * @param baseURI - a conversion:AbstractDatset URI, 
    *                  e.g. "http://ieeevis.tw.rpi.edu/source/us/dataset/data-carves"
    *                  
    * @param humanFriendlyMonth - name month with e.g. "Oct" instead of "10". 
    *                             date +%Y-%b-%d e.g. "2013-Oct-09"
    *                             date +%Y-%m-%d e.g. "2013-10-09"
    */
   public static String getTodayVersionName(boolean humanFriendlyMonth) {
      Date now = new Date(System.currentTimeMillis());
      if( humanFriendlyMonth ) {
         return "version/" + NameFactory.todayHumanFriendly.format(now);
      }else {
         return "version/" + NameFactory.today.format(now);
      }
   }
   
   /**
    * Return name composing day, epoch, and hash, e.g. "20140123-1390489968-016e".
    * 
    * Equivalent to unix: date +%Y%m%d`-`date +%s`-`resource-name.sh | awk '{print substr($0,1,4)}'
    * 
    * e.g. 20140123-1390489968-016e
    * 
    * @param hashLength - the length of a UUID to include at the end.
    * 
    * @return
    */
   public static String getTodayEpochHashName(int hashLength) {
      long ms = System.currentTimeMillis();
      Date now = new Date(ms);
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
      String hash = NameFactory.getUUIDName().substring(0, hashLength); // TODO increase defensiveness here.
      return format.format(now) + "-" + ms + "-" + hash;
   }
   
   /**
    * Single method that takes the NameType and baseURI to produce a full URI.
    * 
    * @return a full URI.
    */
   public static String getFullName(String baseURI, String type, NameType nameType) {
      String localName = null;
      switch(nameType) {
      case UUID:
         localName = getUUIDName(type);
         break;
      case MILLISECOND_STARTED_TO_MINUTE:
         localName = getMillisecondToDayName(type);
         break;
      case MILLISECOND_STARTED_TO_DAY:
         localName = getMillisecondToDayName(type);
         break;
      case INCREMENTING:
         localName = getIncrementingName(type);
         break;
      default:
         localName = getUUIDName(type);
         break;
      }
      return baseURI + localName;
   } 

   /**
    * 
    * @param value
    * @return the a hash (e.g. MD5) of the given 'value'
    */
   public static String getMD5(String value) {
      byte[] bytesOfMessage;
      String hashtext = null;
      try {
         // http://stackoverflow.com/questions/415953/generate-md5-hash-in-java
         bytesOfMessage = (value).getBytes("UTF-8");
         MessageDigest md = MessageDigest.getInstance("MD5");
         byte[] digest = md.digest(bytesOfMessage);
         BigInteger bigInt = new BigInteger(1,digest);
         hashtext = bigInt.toString(16);
      } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
      } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
      }
      return hashtext;
   }
   
   /**
    * 
    * @param file
    * @return
    */
   public static String getMD5(File file) {
      // File message digest
      // http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
      InputStream fis;
      try {
         fis = new FileInputStream(file);
         byte data[] = DigestUtils.md5(fis);
         char md5Chars[] = Hex.encodeHex(data);
         String md5 = String.valueOf(md5Chars);
         return md5;
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return null;
   }
   
   /**
    * e.g. 'type'_27_feb_2009_1235761123899_ms
    * 
    * @param type
    * @return a URI-worthy local name. Prepend it with a baseURI.
    */
   public static String getMillisecondToDayName(String type) {
      Calendar startTime = Calendar.getInstance();
      long startTimeMS = startTime.getTimeInMillis();

      return type + "_"                                         // e.g. LoggerReport
          + startTime.get(Calendar.YEAR)             + "_"      // e.g. 2009
          + monthName[startTime.get(Calendar.MONTH)] + "_"      // e.g. feb
          + startTime.get(Calendar.DAY_OF_MONTH)     + "_"      // e.g. 27
          + startTimeMS + "_ms";                                // e.g. 1235738656
   }
   
   /**
    * 
    * @param type
    * @return
    */
   public static String getMillisecond(String type) {
   	return ""+Calendar.getInstance().getTimeInMillis();
   }
   
   /**
    * e.g. 'type'_2013-Oct-09_11-04_1381331078850_ms
    *             2013-Oct-09_11-04_1381331078850_ms (if no type given)
    *             http://www.openarchives.org/ore/1.0/datamodel
    * 
    * @param type
    * @return a URI-worthy local name. Prepend it with a baseURI.
    */
   public static String getMillisecondToMinuteName(String type) {
      Calendar startTime = Calendar.getInstance();
      long startTimeMS = startTime.getTimeInMillis();
      String typePre = type.length() > 1 ? type + "_" : "";
      return typePre                                              // e.g. LoggerReport
          + startTime.get(Calendar.YEAR)                  + "-"   // e.g. 2009
          + monthName[startTime.get(Calendar.MONTH)]      + "-"   // e.g. Feb
          + zeroPad(startTime.get(Calendar.DAY_OF_MONTH)) + "_"   // e.g. 07
          + zeroPad(startTime.get(Calendar.HOUR_OF_DAY))  + "-"   // e.g. 8:
          + zeroPad(startTime.get(Calendar.MINUTE))       + "_"   // e.g. 59
          + startTimeMS + "_ms";                                  // e.g. 1235738656
   }
   
   /**
    * URI version of {@link #getMillisecondToMinuteName(String)} tucked within a base URI.
    * 
    * @param baseURI
    * @param type
    * 
    * @return
    */
   public static URI getMillisecondToMinuteName(String baseURI, String type) {
      return ValueFactoryImpl.getInstance().createURI(baseURI +"/"+ getMillisecondToMinuteName(type));
   }
   
   /**
    * e.g. 'type'/2013/Oct/9/11-03/1381331000529_ms
    * 
    * 
    * @param type
    * @return a URI-worthy local name. Prepend it with a baseURI.
    */
   public static String getMillisecondToMinuteNameREST(String type) {
      Calendar startTime = Calendar.getInstance();
      long startTimeMS = startTime.getTimeInMillis();

      return type + "/"                                         // e.g. LoggerReport
          + startTime.get(Calendar.YEAR)                 + "/"  // e.g. 2009
          + monthName[startTime.get(Calendar.MONTH)]     + "/"  // e.g. feb
          + startTime.get(Calendar.DAY_OF_MONTH)         + "/"  // e.g. 27
          + zeroPad(startTime.get(Calendar.HOUR_OF_DAY)) + "-"  // e.g. 8:
          + zeroPad(startTime.get(Calendar.MINUTE))      + "/"  // e.g. 59
          + startTimeMS + "_ms";                                // e.g. 1235738656
   }
   
   /**
    * e.g. 'type'_f39cd2ad-8ff3-4fb5-b028-01bcd11557a2
    * 
    * @param type
    * @return a URI-worthy local name. Prepend it with a baseURI.
    */
   public static String getUUIDName(String type) {
   	String sep = type != null && type.length() > 0 ? "_" : "";
      return type + sep + UUID.randomUUID().toString();
   }
   
   /**
    * 
    * @return
    */
   public static String getUUIDName() {
   	return getUUIDName("");
   }
   
   /**
    * 
    * @param type
    * @return
    */
   public static String getIncrementingName(String type) {
      int count = 1;
      if( typeCounts.containsKey(type) ) {
         count = typeCounts.get(type);
         typeCounts.put(type, (1+count));
      }else {
         typeCounts.put(type, (1+count));
      }
      return type + "_" + count;
   }
   
   /**
    * 
    * @param type
    * @return
    */
   public static String getIncrementingName(String type, String identity) {
      // TODO: if 'identity' is given twice, return the same value...
      if( !typeIDManagers.containsKey(type) ) {
         typeIDManagers.put(type, new IDManager());
      }
      return type + typeIDManagers.get(type).getIdentifier(identity);
   }
   
   /**
    * See https://github.com/timrdf/csv2rdf4lod-automation/wiki/Naming-sparql-service-description's-sd:NamedGraph
    * 
    * Python version: https://github.com/timrdf/DataFAQs/blob/master/services/sadi/faqt/sparql-service-description/named-graphs.py
    * XSL version:    sparql-service-descriptions.xsl
    * 
    * @param endpoint
    * @param graph_name
    * @return The URI of a sd:NamedGraph with the given sd:name and within the given sd:endpoint.
    */
   public static String getSPARQLEndpointGraphName(String endpoint, String graph_name) {
   	
   	//
   	//
   	//
   	//
   	// Do NOT change the spacing in this string. It is following the canonical URI construction.
   	String query = 
   			"PREFIX sd: <http://www.w3.org/ns/sparql-service-description#> "+
   			"CONSTRUCT { ?endpoints_named_graph ?p ?o } "+
   			"WHERE { GRAPH <"+graph_name+"> { "+
   			"[] sd:url <"+endpoint+">; "+
   			"sd:defaultDatasetDescription [ sd:namedGraph ?endpoints_named_graph ] . "+
   			"?endpoints_named_graph sd:name <"+graph_name+">; ?p ?o . } }";
   	// Do NOT change the spacing in this string. It is following the canonical URI construction.
   	//
   	//
   	//
   	//
   	
   	try {
	      return endpoint + "?query=" + URLEncoder.encode(query, "ISO-8859-1");
      } catch (UnsupportedEncodingException e) {
	      e.printStackTrace();
	      return null;
      }
   }

   /**
    * See https://github.com/timrdf/csv2rdf4lod-automation/wiki/Naming-sparql-service-description's-sd:NamedGraph
    * 
    * Shell version: https://github.com/timrdf/vsr/blob/master/bin/vsr-ting.sh#L68
    * 
    * The output of this method is owl:sameAs the output of {@link #getSPARQLEndpointGraphName(String, String)}.
    * 
    * @param ourNS - the URI namespace that will contain the tiny URI for the named graph: <endpoint, graph_name>.
    * @param endpoint - the SPARQL endpoint that provides access to 'graph_name'
    * @param graph_name - the sd:name of the GRAPH {} in the SPARQL endpoint 'endpoint'
    * 
    * @return an abbreviated URI for the named graph <endpoint, graph_name> in form <ourNS>/id/named-graph/<MD5(longURI)>
    */
   public static String getSPARQLEndpointGraphNameTiny(String ourNS, String endpoint, String graph_name) {
   	/*
   	 * https://github.com/timrdf/vsr/blob/master/bin/vsr-ting.sh#L68
   	 *    ng_ugly=`resource-name.sh --named-graph $endpoint $graph_name`
       *    ng="$endpoint/id/named-graph/`md5.sh -qs "$ng_ugly"`"
   	 */
   	return ourNS + "/id/named-graph/" + getMD5(getSPARQLEndpointGraphName(endpoint,graph_name));
   }
   
   /**
    * See {@link #getSPARQLEndpointGraphNameTiny(String, String, String)}
    * 
    * @param ourNS
    * @param endpoint
    * @param graph_name
    * @return
    */
   public static String getSPARQLEndpointGraphNameTiny(Resource ourNS, Resource endpoint, Resource graph_name) {
      return getSPARQLEndpointGraphNameTiny(ourNS.stringValue(), endpoint.stringValue(), graph_name.stringValue());
   }
   
   /**
    * 
    * @param ourNS
    * @param prefix
    * @param namespace
    * @return
    */
   public static String getPrefixMappingName(String ourNS, String prefix, String namespace) {
      return ourNS + "/id/prefix/" + prefix + "/" + getMD5(namespace);
   }
   
   
   // String manipulations
   
   
   
   /**
    * 
    * @param i
    * @return
    */
   public static String zeroPad(int i) {
      return i < 10 ? "0"+Integer.toString(i) : Integer.toString(i);
   }
   
   /**
    * 
    * @param attribute
    * @param value
    * @return 'attribute'/'value' if value exists; '' if it does not.
    */
   public static String slashIfThereShort(String attribute, String value) {
      return attribute != null &&
             value     != null && value.length() > 0 ? attribute.replace("/$","") +"/"+ value
                                                     : "";
   }
   
   /**
    * This method was implemented incorrectly; it appends a trailing slash.
    * Unfortunately, too many systems call it, so it will stay as is. 
    * See {@link #slashIfThereShort(String, String)} to avoid the trailing slash.
    * 
    * @param attribute
    * @param value
    * @return 'attribute'/'value'/ if value exists; '' if it does not.
    */
   public static String slashIfThere(String attribute, String value) {
      return attribute != null &&
             value     != null && value.length() > 0 ? attribute.replace("/$","") +"/"+ value + "/" 
                                                     : "";
   }
   
   /**
    * @param value
    * @return 'value'/ if value exists; '' if it does not.
    */
   public static String slashIfThere(String value) {
      return value != null && value.length() > 0 ? value + "/" 
                                                 : "";
   }   
   
   /**
    * 
    * @param string - a string containing e.g. "_______"
    * @param toTrim - e.g. "_" to collapse all "_____" to a "_".
    */
   public static String trimChars(String string, String toTrim) {
      if( string != null ) {
         String origString = string;
         //System.err.println("trimChars: "+string+" "+toTrim+" "+string.indexOf(toTrim+toTrim));
         // Replace all double occurrences.

         for(int i = 0; string.indexOf(toTrim+toTrim) >= 0 && i<50; i++ ) {
            //System.err.println("trimming "+string);
            string = string.replaceAll(toTrim+toTrim, toTrim);
            if(i==49) 
               System.err.println("edu.rpi.tw.string.NameFactory#trimChars: infinite! -->"+origString+"<-"+
                     "->"+string+"<-- : -->"+toTrim+toTrim +"<-- : "+string.length()+" : "+string.indexOf(toTrim+toTrim));
            // TODO: chokes on "CA (Combined Cycle Steam Part)"
         }
         // Trim off beginning
         if( string.indexOf(toTrim) == 0 ) {
            string = string.substring(toTrim.length(),string.length());
         }
         //System.err.println(string + " " + string.lastIndexOf(toTrim) + " " + (string.length()-toTrim.length()));
         if( string.lastIndexOf(toTrim) >= 0 && string.lastIndexOf(toTrim) == string.length()-toTrim.length() ) {
            //System.err.println(string.substring(0,string.length()-toTrim.length()));
            string = string.substring(0,string.length()-toTrim.length());
         }
      }
      return string;
   }
   
   /**
    * 
    * @param stringWithFirstCap
    * @return
    */
   public static String lowerFirst(String stringWithFirstCap) {
      String loweredFirst = stringWithFirstCap;
      if( loweredFirst != null && loweredFirst.length() > 1 ) {
         loweredFirst = loweredFirst.substring(0, 1).toLowerCase() + loweredFirst.substring(1);
      }
      return loweredFirst;
   }
   
   /**
    * 
    * @param stringWithFirstCap
    * @return
    */
   public static String capitalizeFirst(String stringWithLowercase) {
      String upperedFirst = stringWithLowercase;
      if( upperedFirst != null && upperedFirst.length() > 1 ) {
         upperedFirst = upperedFirst.substring(0, 1).toUpperCase() + upperedFirst.substring(1);
      }
      return upperedFirst;
   }
   
   /**
    * HARTFORD HOSPITAL -> Hartford Hospital
    * USGS -> Usgs (does not behave well on acronyms)
    * 
    * @param ugly
    * @return
    */
   public static String titleCase(String ugly) {
	   String[] tokens = ugly.split("\\s");
	   String pretty = "", space = "";
	   for( int i=0; i < tokens.length; i++ ) {
		   pretty = pretty + space + capitalizeFirst(tokens[i].toLowerCase());
		   space=" ";
	   }
	   return pretty;
   }
   
   /**
    * 
    * @param label
    * @return
    */
   public static String label2URI(String label) {
      //System.err.println("label: "+label);
      //return label == null ? label : NameFactory.trimChars(label.trim().replaceAll("\\W","_"),"_");
      // Preventing dashes from being squashed to underscores:
      return label == null ? label : NameFactory.trimChars(label.trim().replaceAll("[^a-zA-Z_0-9\\-]","_"),"_");
   }
   
   /**
    * 
    * @param label
    * @return
    */
   public static String uriString2URI(String label) {
      //System.err.println("label: "+label);
      return label == null ? label : NameFactory.trimChars(label.trim().replaceAll(" ","_"),"_");
   }
   
   /**
    * java edu.rpi.tw.string.NameFactory
    * a5db289e-b03d-4f03-86d2-741e4627df40
    * 
    * java edu.rpi.tw.string.NameFactory --source-id-of http://data.melagrid.org/cowabunga/dude.html 
    * returns "data-melagrid-org"
    * 
    * java edu.rpi.tw.string.NameFactory --source-id-of n
    * <returns nothing>
    * 
    * resource-name.sh --named-graph http://ieeevis.tw.rpi.edu/sparql http://ieeevis.tw.rpi.edu/lam-2012-evaluations-7-scenarios
    * http://ieeevis.tw.rpi.edu/sparql?query=PREFIX+sd%3A+%3Chttp%3A%2F%2Fwww.w3.org%2Fns%2Fsparql-service-description%23%3E+CONSTRUCT+%7B+%3Fendpoints_named_graph+%3Fp+%3Fo+%7D+WHERE+%7B+GRAPH+%3Chttp%3A%2F%2Fieeevis.tw.rpi.edu%2Flam-2012-evaluations-7-scenarios%3E+%7B+%5B%5D+sd%3Aurl+%3Chttp%3A%2F%2Fieeevis.tw.rpi.edu%2Fsparql%3E%3B+sd%3AdefaultDatasetDescription+%5B+sd%3AnamedGraph+%3Fendpoints_named_graph+%5D+.+%3Fendpoints_named_graph+sd%3Aname+%3Chttp%3A%2F%2Fieeevis.tw.rpi.edu%2Flam-2012-evaluations-7-scenarios%3E%3B+%3Fp+%3Fo+.+%7D+%7D
    * 
    * resource-name.sh --named-graph-tiny http://datafaqs.tw.rpi.edu http://ieeevis.tw.rpi.edu/sparql http://ieeevis.tw.rpi.edu/lam-2012-evaluations-7-scenarios
    * http://datafaqs.tw.rpi.edu/id/named-graph/80f1c94b2396f699244bc09ca627e0bd
    * 
    * @param args
    */
   public static void main(String[] args) {
   	if( args.length > 0 && "--help".equals(args[0]) ) {
   	   System.out.println("--domain-of        <uri>                                   ");
         System.out.println("--named-graph                      <endpoint> <graph-name> ");
         System.out.println("--named-graph-tiny <our-namespace> <endpoint> <graph-name> ");
         System.out.println("--date-epoch-hash                                          -> "+
                           "e.g. "+NameFactory.getTodayEpochHashName(4));
   	}else	if( args.length > 1 && "--source-id-of".equals(args[0])) {
   	   String source_id = NameFactory.sourceID(NameFactory.uriDomain(args[1]+"/"));
			if( source_id != null ) {
				System.out.println(source_id);
			}
   	}else if( args.length > 0 && "--domain-of".equals(args[0]) ) {
   	   // TODO: http://data.fishesoftexas.org:8080 -> http://data.fishesoftexas.org
   		if( args.length > 1 ) {
   		   if( "-".equals(args[1]) ) {
   		      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
   		      String uri;
   		      try {
                  while ((uri = br.readLine()) != null && uri.length() != 0) {
                    String domain = NameFactory.uriDomain(uri+"/");
                    if( domain != null ) {
                       System.out.println(domain);
                    }
                  }
               } catch (IOException e) {
                  e.printStackTrace();
               }
   		   }else{
      			String domain = NameFactory.uriDomain(args[1]+"/");
      			if( domain != null ) {
      				System.out.println(domain);
      			}
   		   }
   		}
   	}else if( args.length > 0 && "--named-graph".equals(args[0]) ) {
   		if( args.length > 1 ) {
   			String endpoint   = args[1];
   			String graph_name = args[2];
   			String uri = getSPARQLEndpointGraphName(endpoint, graph_name);
   			if( uri != null ) {
   				System.out.println(uri);
   			}
   		}
   	}else if( args.length > 0 && "--named-graph-tiny".equals(args[0]) ) {
   		if( args.length > 3 ) {
   			String ourNS      = args[1];
   			String endpoint   = args[2];
   			String graph_name = args[3];
   			String uri = getSPARQLEndpointGraphNameTiny(ourNS, endpoint, graph_name);
   			if( uri != null ) {
   				System.out.println(uri);
   			}
   		}
   	}else if( args.length > 0 && "--date-epoch-hash".equals(args[0]) ) {
   	   System.out.println(NameFactory.getTodayEpochHashName(4));
   	}else {
	      String type = args.length > 0 ? args[0] : "";
	      System.out.println(getUUIDName(type));
   	}
   }
   
   /**
    * Return domain of the URI:
    * 
    * (http://[^/]*)/.*$
    * 
    * cf. TlDManager.getDomain(), which removes scheme and port.
    * 
    * @param uri
    * @return the domain of the URI
    */
   public static String uriDomain(String uri) {
   	
      // From cr-create-dataset-dirs-from-ckan.py:
      // re.sub('(http://[^/]*)/.*$','\\1',ckanAPI)
   	
   	// also does this computation: https://github.com/timrdf/csv2rdf4lod-automation/blob/master/bin/dataset/cr-isdefinedby/cr-isdefinedby.py
   	
   	String domain = null;
   	
      Matcher matcher = purlPattern.matcher(uri);
      if( matcher.matches() ) {
      	// Hack for just purl.org, since it has many domains within it (.
      	// TODO:  handling duplicated in CSVtoRDF#assertVocabularyUse:
      	domain = matcher.group(1);
      }else {
      	matcher = domainPattern.matcher(uri);
         if( matcher.matches() ) {
         	domain = matcher.group(1);
         }
      }
      return domain;
   }
   
   /**
    * 
    * @param uri
    * @return
    */
   public static String sourceID(String uri) {
   	return uri == null ? null : uri.replace("http://", "").replace("https://", "").replace(".", "-");
   }
}