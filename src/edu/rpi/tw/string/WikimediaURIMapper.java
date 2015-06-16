package edu.rpi.tw.string;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import edu.rpi.tw.data.rdf.sesame.vocabulary.TetherlessWorld;

public class WikimediaURIMapper extends URIMapper {

   protected String prefix;

   /**
    * 
    * @param topic
    * @return
    */
   public static String map(String topic) {
      return WikimediaURIMapper.map(topic, true);
   }
   
   /**
    * @param topic
    * @param capitalize
    * @return
    */
   public static String map(String topic, boolean capitalize) {  // TODO: reconcile with xsl version
      topic = topic.replace(" ","_");   // space
      topic = topic.replace(",","-2C"); // comma
      topic = topic.replace("'","-27"); // apostrophe
      topic = topic.replace(":","-3A"); // colon
      topic = topic.replace("(","-28"); 
      topic = topic.replace(")","-29");
      topic = topic.replace("|","_");   // pipe
      topic = topic.replace("@","-40"); 
      topic = topic.replace("&","-26"); // ampersand
      topic = topic.replace("`","-60"); // back tick
      topic = topic.replace("\\?","-3F"); // question mark
      //topic = topic.replace("�","-E2-80-98");
      topic = topic.replace("%", "-25");
      topic = topic.replace("\\W$", "");
      topic = topic.replace("\\W", "_");
      topic = topic.replace("/", "");
      // Capitalize first character of local name
      if( capitalize && topic.length() > 1 ) {
         //System.out.println(topic + " " + topic.substring(0,1).toUpperCase() + topic.substring(1,topic.length()-1));
         topic = topic.substring(0,1).toUpperCase() + topic.substring(1,topic.length());
      }
      return topic;
   }
   // TODO: | char is not a valid URI character
   public static URI name(String baseURI, String localName) {
      return asURI(baseURI + WikimediaURIMapper.map(localName));
   }

   public static final String USAGE = "usage: WikimediaURIMapper {- baseURI prefix | [localName]* }";

   /**
    * - baseURI prefix
    * Read local names from stdin using the given baseURI and prefix
    * 
    * [localName]*
    * Provide a full URI for given localNames within the RL namespace.
    * 
    * @param args
    */
   public static void main(String args[]) {
      if( args.length < 1 ) {
         System.err.println(USAGE);
         System.exit(1);
      }
      
      if( "-".equalsIgnoreCase(args[0]) ) {
         String baseURI = args.length > 1 ? args[1] : "";
         String prefix  = args.length > 2 ? args[2]+":" : ":";
         if( baseURI.length() > 0 ) {
            System.out.println("@prefix "+prefix+" <"+baseURI+"> .\n");
         }
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         try {
            String identifyingLabel = null;
            while( (identifyingLabel = br.readLine()) != null ) {
               System.out.println(prefix+map(identifyingLabel)+"\n"+
                                  "   p:idlabel \""+identifyingLabel+"\";\n"+
                                  "."
               );
            }
         } catch (IOException e1) {
            e1.printStackTrace();
         }
      } else {
         for( int i = 0; i < args.length; i++ ) {
            URI itemURI = null;
            try {
               itemURI = new URI(TetherlessWorld.BASE_URI+map(args[i]));
            } catch (URISyntaxException e) {
               e.printStackTrace(System.err);
            }
            System.out.println(map(args[i]));
         }
      }
   }
}