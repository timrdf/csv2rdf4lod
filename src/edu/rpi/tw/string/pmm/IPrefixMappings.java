package edu.rpi.tw.string.pmm;

/**
 * 
 */
public interface IPrefixMappings {
   
	// grep "public" edu/rpi/tw/string/pmm/IPrefixMappings.java | grep -v interface | sed 's/(//' | awk '{print "System.out.println(\""$3": \"+pmap."$3"(args[i]));"}' 
	
   /**
    * 
    * @param uri
    * @return
    */
   public boolean canAbbreviate( String uri );
   
   /**
    * If no namespace prefix found, return empty string.
    * 
    * @param uri - e.g. http://my.name/ns#me
    * @return the longest namespace prefix that matches 'uri'. (e.g. http://my.name/ns# not http://my.name/).
    */
   public String bestNamespaceFor( String uri );
   
   /**
    * 
    * @param uri
    * @return
    */
   public String bestPrefixFor( String uri );
   
   /**
    * 
    * @param uri
    * @return
    */
   public String bestQNameFor( String uri );
   
   /**
    * 
    * @param uri
    * @return
    */
   public String bestQNameRef( String uri );
   
   /**
    * 
    * @param uri
    * @return a CURIE if one can be created, or the input URI if not.
    */
   public String tryQName( String uri );
   
   /**
    * 
    * @param uri
    * @return
    */
   public String bestLocalNameFor( String uri );
   
   /**
    * 
    * @param uri
    * @return
    */
   public String bestLabelFor( String uri );
   
   /**
    * 
    * @param qname
    * @return
    */
   public String expandQName( String qname );

   /**
    * If the curie can't be created, fallback to bestLocalNameFor.
    * 
    * @param uri
    * @return
    */
   String tryQNameFallbackLocal(String uri);

   /**
    * If the curie can't be created, fallback to bestNamespaceFor + bestLocalNameFor.
    * 
    * @param uri
    * @return
    */
   String tryQNameFallbackDomainAndLocal(String uri);
}