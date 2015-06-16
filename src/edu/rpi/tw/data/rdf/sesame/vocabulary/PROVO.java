package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * 
 */
public class PROVO extends DefaultVocabulary {

   public static final ValueFactory vf = ValueFactoryImpl.getInstance();
   	
   public static String BASE_URI = "http://www.w3.org/ns/prov#";
   public static String PREFIX   = "prov";
   
   @Override
   public String getBaseURI() {
      return BASE_URI;
   }

   @Override
   public String getPrefix() {
      return PREFIX;
   }
   
   public static final Namespace namespace = new NamespaceImpl(PREFIX,BASE_URI);
   
   public static final URI wasInfluencedBy     = vf.createURI(BASE_URI, "wasInfluencedBy");
   public static final URI Influence           = vf.createURI(BASE_URI, "Influence");
   public static final URI entity              = vf.createURI(BASE_URI, "entity");
   public static final URI activity            = vf.createURI(BASE_URI, "activity");
   public static final URI agent               = vf.createURI(BASE_URI, "agent");
   
   public static final URI Entity              = vf.createURI(BASE_URI, "Entity");
   public static final URI value               = vf.createURI(BASE_URI, "value");
   public static final URI wasDerivedFrom      = vf.createURI(BASE_URI, "wasDerivedFrom");
   public static final URI wasGeneratedBy      = vf.createURI(BASE_URI, "wasGeneratedBy");
   public static final URI wasAssociatedWith   = vf.createURI(BASE_URI, "wasAssociatedWith");
   public static final URI wasGeneratedAtTime  = vf.createURI(BASE_URI, "wasGeneratedAtTime");
   public static final URI wasAttributedTo     = vf.createURI(BASE_URI, "wasAttributedTo");
   public static final URI specializationOf    = vf.createURI(BASE_URI, "specializationOf");
   public static final URI alternateOf         = vf.createURI(BASE_URI, "alternateOf");
   public static final URI hadLocation         = vf.createURI(BASE_URI, "atLocation"); // ERROR: it's NOT "had"
   public static final URI atLocation          = vf.createURI(BASE_URI, "atLocation");
   public static final URI generatedAtTime     = vf.createURI(BASE_URI, "generatedAtTime");
   public static final URI invalidatedAtTime   = vf.createURI(BASE_URI, "invalidatedAtTime");

   public static final URI Plan                = vf.createURI(BASE_URI, "Plan");
   public static final URI Role                = vf.createURI(BASE_URI, "Role");
   public static final URI Location            = vf.createURI(BASE_URI, "Location");
   
   public static final URI Activity            = vf.createURI(BASE_URI, "Activity");
   public static final URI used                = vf.createURI(BASE_URI, "used");
   public static final URI generated           = vf.createURI(BASE_URI, "generated");
   public static final URI startedAtTime       = vf.createURI(BASE_URI, "startedAtTime");
   public static final URI endedAtTime         = vf.createURI(BASE_URI, "endedAtTime");

   public static final URI Agent               = vf.createURI(BASE_URI, "Agent");
   public static final URI Person              = vf.createURI(BASE_URI, "Person");
   public static final URI Organization        = vf.createURI(BASE_URI, "Organization");
   public static final URI SoftwareAgent       = vf.createURI(BASE_URI, "SoftwareAgent");
   
   public static final URI actedOnBehalfOf     = vf.createURI(BASE_URI, "actedOnBehalfOf");
   public static final URI qualifiedDelegation = vf.createURI(BASE_URI, "qualifiedDelegation");
   public static final URI Delegation          = vf.createURI(BASE_URI, "Delegation");
   
   public static final URI atTime              = vf.createURI(BASE_URI, "atTime");
   
   public static final URI hadPlan             = vf.createURI(BASE_URI, "hadPlan");
   
   public static final URI Collection          = vf.createURI(BASE_URI, "Collection");
   
   public static final URI hadMember           = vf.createURI(BASE_URI, "hadMember");
   
   
   // PROV-AQ
   
   public static final URI has_provenance      = vf.createURI(BASE_URI, "has_provenance");
}