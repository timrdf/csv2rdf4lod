package edu.rpi.tw.string;

import java.net.URI;
import java.net.URISyntaxException;

public class URIMapper {
   /**
    * Cast a String to a URI, dealing with the possible exception.
    * @param uri
    * @return
    */
   public static URI asURI(String uri) {
      URI itemURI = null;
      try {
         itemURI = new URI(uri);
      } catch (URISyntaxException e) {
         e.printStackTrace(System.err);
      }
      return itemURI;
   }
}