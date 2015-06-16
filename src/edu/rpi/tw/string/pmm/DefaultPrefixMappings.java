package edu.rpi.tw.string.pmm;

import javax.naming.NamingException;

import edu.rpi.tw.data.csv.valuehandlers.ResourceValueHandler;
import edu.rpi.tw.data.rdf.sesame.string.PrettyTurtleWriter;
import edu.rpi.tw.data.rdf.sesame.vocabulary.BTE;
import edu.rpi.tw.data.rdf.sesame.vocabulary.PML3;
import edu.rpi.tw.data.rdf.sesame.vocabulary.SIO;

/**
 * 
 */
public class DefaultPrefixMappings extends PrefixMappings {

   public static PrefixMappings pmap = new DefaultPrefixMappings();
   
   public static PrefixMappings getInstance() {
      return pmap;
   }
   
   public DefaultPrefixMappings() {
      super();
      //addPrefix("http://localhost/",                                                                           "local");
      addPrefix("http://www.w3.org/1999/xhtml/vocab#",                                                           "xhv");
      addPrefix("http://www.w3.org/1999/02/22-rdf-syntax-ns#",                                                   "rdf");
      addPrefix("http://www.w3.org/2000/01/rdf-schema#",                                                        "rdfs");
      addPrefix("http://www.w3.org/2000/10/swap/pim/contact#",                                                   "con");
      addPrefix("http://www.w3.org/2000/10/swap/pim/doc#",                                              "swap-pim-doc");
      addPrefix("http://www.w3.org/2000/09/xmldsig#",                                                        "xmldsig");
      //addPrefix("http://www.w3.org/2001/XMLSchema#",                                                              "xs");
      addPrefix("http://www.w3.org/2001/XMLSchema#",                                                             "xsd");
      addPrefix("http://www.w3.org/TR/xmlschema-2/#",                                                           "xsd2");
      addPrefix("http://www.w3.org/2002/07/owl#",                                                                "owl");
      addPrefix("http://www.w3.org/2003/11/swrl#",                                                              "swrl");
      addPrefix("http://www.w3.org/2003/g/data-view#",                                                         "grddl");
      addPrefix("http://www.w3.org/2003/06/sw-vocab-status/ns#",                                                  "vs");    
      addPrefix("http://www.w3.org/2003/01/geo/wgs84_pos#",                                                      "wgs");
      addPrefix("http://www.w3.org/2004/02/skos/core#",                                                         "skos");
      addPrefix("http://www.w3.org/2008/05/skos#",                                                             "skos8");
      addPrefix("http://www.w3.org/2004/02/skos/extensions#",                                                  "skose");
      addPrefix("http://www.w3.org/2004/03/trix/swp-2/",                                                         "swp");
      addPrefix("http://www.w3.org/2004/09/fresnel#",                                                            "fnl");
      addPrefix("http://www.w3.org/ns/sparql-service-description#",                                               "sd");
      addPrefix("http://www.w3.org/2011/http#",                                                                 "htir");
      addPrefix("http://www.w3.org/2011/http-headers#",                                                       "hthdrs");
      addPrefix("http://www.w3.org/ns/prov#",                                                                   "prov");
      addPrefix("http://www.w3.org/ns/org#",                                                                     "org");
      addPrefix("http://www.w3.org/ns/auth/acl#",                                                                "acl");

      
      addPrefix("http://www.daml.org/services/owl-s/1.2/generic/Expression.owl#",                              "owlse");
      addPrefix("http://www.daml.org/services/owl-s/1.2/generic/ObjectList.owl#",                           "owlslist");

      addPrefix("http://www.w3.org/2006/time#",                                                    "time");

      addPrefix("http://xmlns.com/foaf/0.1/",                                                      "foaf");

      addPrefix("http://rdfs.org/sioc/ns#",                                                        "sioc");
      
      
      //addPrefix("http://purl.org/dc/elements/1.1/",                                                "dc");
      addPrefix("http://purl.org/dc/terms/",                                                                 "dcterms");
      addPrefix("http://purl.org/dc/dcmitype/",                                                             "dcmitype");
      addPrefix("http://purl.org/dc/dcam/",                                                                     "dcam");
      addPrefix("http://dublincore.org/usage/terms/history/#",                                             "dchistory");
      //addPrefix("http://purl.org/dc/terms/",                                                       "purldc");
      addPrefix("http://purl.org/dc/elements/1.1/",                                                "dce");
      

      addPrefix("http://rdfs.org/ns/void#",                                                        "void");
      addPrefix("http://purl.org/NET/scovo#",                                                      "scovo");

      addPrefix("http://dbpedia.org/resource/",                                                    "dbpedia");
      addPrefix("http://dbpedia.org/ontology/",                                                    "dbpont");
      addPrefix("http://dbpedia.org/property/",                                                    "dbprop");
      addPrefix("http://www.geonames.org/ontology#",                                              "geonames");
      addPrefix("http://rdf.freebase.com/ns/",                                                     "frbse");
      
      addPrefix("http://open.vocab.org/terms/",                                                    "ov");

      addPrefix("http://inference-web.org/2.0/ds.owl#",                                            "pmlds");
      addPrefix("http://inference-web.org/2.0/pml-provenance.owl#",                                "pmlp");
      addPrefix("http://inference-web.org/2.1/pml-provenance.owl#",                                           "PMLEXP");
      addPrefix("http://inference-web.org/2.0/pml-justification.owl#",                             "pmlj");
      addPrefix("http://inference-web.org/2.0/pml-trust.owl#",                                     "pmlt");      
      addPrefix("http://sweet.jpl.nasa.gov/ontology/units.owl#",                                   "sweet");
      addPrefix("http://protege.stanford.edu/system#",                                             "ptj");
      addPrefix("http://protege.stanford.edu/kb#",                                                 "kb");
      addPrefix("http://www.owl-ontologies.com/",                                                  "oo");
      addPrefix("http://scot-project.org/scot/ns#",                                                "scot");

      addPrefix("http://pajek.org/data/ERDOS992.NET#",                                             "pjk92");
      addPrefix("http://pajek.org/data/ERDOS991.NET#",                                             "pjk91");
      addPrefix("http://pajek.org/data/ERDOS982.NET#",                                             "pjk82");
      addPrefix("http://pajek.org/data/ERDOS981.NET#",                                             "pjk81");
      addPrefix("http://pajek.org/data/ERDOS972.NET#",                                             "pjk72");
      addPrefix("http://pajek.org/data/ERDOS971.NET#",                                             "pjk71");

      addPrefix("http://www.visual-literacy.org/periodic_table/terms/",                            "vl");
      //addPrefix("http://www.semanticdesktop.org/ontologies/nfo/#",                                 "nfo");
      addPrefix("http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#",                       "nfo");
      addPrefix("http://copper.ist.psu.edu/oai/oai_citeseer/",                                     "oai_citeseer");
      
      addPrefix("http://www.holygoat.co.uk/owl/redwood/0.1/tags/",                                 "hlygt");
      //addPrefix("http://www.holygoat.co.uk/owl/redwood/0.1/tags/",                                 "tag");
      addPrefix("http://semantic-mediawiki.org/swivt/1.0#",                                        "swivt");
      addPrefix("http://www.daml.org/2003/02/fips55/fips-55-ont#",                                 "fips");
      addPrefix("http://www.daml.org/2003/02/fips55/location-ont#",                                "fipsloc");


      addPrefix("http://www.daml.ri.cmu.edu/ont/USRegionState.daml#",                              "cmuregional");
      addPrefix("http://www.daml.org/2001/03/daml+oil#",                                           "damloil");
      addPrefix("http://linuxproj.ecs.soton.ac.uk/~bh704/cia/#",                                   "ftbk");
      addPrefix("http://linuxproj.ecs.soton.ac.uk/cia/units/#",                                   "acunits");
      addPrefix("http://linuxproj.ecs.soton.ac.uk/cia/orgs/#",                                    "acorgs");
      addPrefix("http://jena.hpl.hp.com/2005/11/Assembler#",                                      "ja");
      addPrefix("http://hawxgame.us.ubi.com/ns/",                                                 "hawx");
      addPrefix("http://polyscheme.cogsci.rpi.edu/ns/",                                           "prt");
      addPrefix("http://polyscheme.cogsci.rpi.edu/ns/variable/",                                  "Q");
      addPrefix("http://polyscheme.cogsci.rpi.edu/input/",                                        "in");
      addPrefix("http://polyscheme.cogsci.rpi.edu/ns/world#",                                     "w");
      addPrefix("http://dataportal.ucar.edu/schemas/vsto.owl#",                                   "vsto");
      addPrefix("http://workarea/schemas/whoi.owl#",                                              "whoi");
      addPrefix("http://tw.rpi.edu/wiki/Special:URIResolver/",                                    "tw");  
      addPrefix("http://tw.rpi.edu/wiki/Special:URIResolver/Property-3A",                         "twp");  
      addPrefix("http://sweet.jpl.nasa.gov/2.0/",                                                 "swt");
      //addPrefix("http://polyscheme.cogsci.rpi.edu/example#",                                      "eg");
      addPrefix("http://www.airport-technology.com/icao-codes/ns/",                               "icao");
      addPrefix("http://purl.org/goodrelations/v1#",                                              "goodrel");
      addPrefix("http://usefulinc.com/ns/doap#",                                                  "doap");
      //addPrefix("http://www.w3.org/2001/vcard-rdf/3.0#",                                          "vcard");
      addPrefix("http://www.w3.org/2006/vcard/ns#",                                               "vcard");
      //addPrefix("http://data-gov.tw.rpi.edu/vocab/",                                              "twdg");
      //addPrefix("http://data-gov.tw.rpi.edu/vocab/p/",                                            "twdgp");
      //addPrefix("http://data-gov.tw.rpi.edu/data-gov/property/",                                  "twdgp");
      addPrefix("http://data-gov.tw.rpi.edu/data-gov/",                                            "twdg");
      addPrefix("http://data-gov.tw.rpi.edu/data-gov/dataset/",                                    "twdgs");

      addPrefix("http://data-gov.tw.rpi.edu/data-gov/value/",                                      "twdgo");
      addPrefix("http://escience.rpi.edu/schemas/whoi_vents.owl#",                                "vent");
      addPrefix("http://4dgeo.whoi.edu/data/",                                                    "whoidata");
      addPrefix("http://4dgeo.whoi.edu/data/CSVtoRDF_7ccad328-899c-4056-a9d6-2b19c9ecd507/",      "ventxls");
      
      addPrefix("http://example.org/data#",                                                        "eg");
      addPrefix("http://example.org/ont#",                                                         "ont");
      addPrefix("http://sw.deri.org/2005/08/conf/cfp.owl#",                                        "cfp");
      addPrefix("http://www.w3.org/People/Berners-Lee/card#",                                      "tbl");
      addPrefix("http://mged.sourceforge.net/ontologies/MGEDOntology.owl#",                        "mage");
      addPrefix("http://provenance.rpi.edu/ontologies/mageprovenance.owl#",                        "rpiprov");
      addPrefix("http://openprovenance.org/ontology#",                                             "oprov");
      addPrefix("http://magetab2rdf.googlecode.com/svn/trunk/ontologies/mage-om.owl#",             "mage2r");
      addPrefix("http://escience.rpi.edu/schemas/development/whoi_classes.owl#",                   "rpiwhoi");
      addPrefix("http://provenance.rpi.edu/proofs/scdb/use1/","case1");
      addPrefix("http://provenance.rpi.edu/proofs/scdb/use2/","case2");
      addPrefix("http://provenance.rpi.edu/proofs/scdb/use3/","case3");

      //addPrefix("http://data-gov.tw.rpi.edu/vocab/conversion/",                                    "conversion");
      addPrefix("http://purl.org/twc/vocab/conversion/",                                           "conversion");
      
      addPrefix("http://topics.nytimes.com/top/reference/timestopics/people",                      "nytimespeople");
      addPrefix("http://data.nytimes.com/elements/",                                               "nyt");
      addPrefix("http://data-gov.tw.rpi.edu/2009/data-gov-twc.rdf",                                "twcdg");
      addPrefix("http://purl.oclc.org/NET/muo/muo#",                                               "muo");
      addPrefix("http://purl.oclc.org/NET/muo/ucum/unit/",                                         "ucum-u");
      addPrefix("http://purl.oclc.org/NET/muo/ucum/",                                              "ucum");
      addPrefix("http://data-gov.tw.rpi.edu/source/whitehouse-gov/dataset/visitor-records/version/2010-Mar-26/","wh");
      addPrefix("http://data-gov.tw.rpi.edu/source/dfid-gov-uk/dataset/sid-2009/version/2009-Nov-10/",          "dfid");
      addPrefix("http://purl.org/vocab/vann/",                                                                  "vann");
      
      addPrefix("http://logd.tw.rpi.edu/source/SSS/dataset/DDD/vocab/",                                        "vocab");
      addPrefix("http://logd.tw.rpi.edu/source/SSS/dataset/DDD/vocab/raw/",                                      "raw");

      addPrefix("http://logd.tw.rpi.edu/source/SSS/dataset/DDD/",                                                "ddd");
      addPrefix("http://logd.tw.rpi.edu/source/SSS/dataset/DDD/version/VVV/",                                    "vvv");
      addPrefix("http://www.ontologydesignpatterns.org/ont/web/irw.owl#",                                        "irw");
      addPrefix("http://data.semanticweb.org/ns/swc/ontology#",                                                  "src");
      addPrefix("http://data.semanticweb.org/conference/iswc/2010/",                                        "iswc2010");
      addPrefix("http://data.semanticweb.org/",                                                            "semweborg");
      addPrefix("http://swrc.ontoware.org/ontology#",                                                           "swrc");
      addPrefix("http://www.cs.vu.nl/~mcaklein/onto/swrc_ext/2005/05#",                                     "swrc_ext");
      addPrefix("http://www.w3.org/2002/12/cal/ical#",                                                          "ical");
      addPrefix("http://prismstandard.org/namespaces/basic/2.0/",                                              "prism");
      //addPrefix("http://prismstandard.org/namespaces/1.2/basic/",                                              "prism");
      addPrefix("http://swan.mindinformatics.org/ontologies/1.2/collections/",                                  "swan");
      addPrefix("http://purl.org/vocab/frbr/core#",                                                         "frbrcore");
      addPrefix("http://purl.org/spar/cito/",                                                                   "cito");
      addPrefix("http://purl.org/spar/fabio/",                                                                 "fabio");
      addPrefix("http://purl.org/spar/biro/",                                                                   "biro");
      addPrefix("http://purl.org/spar/c4o/",                                                                     "c4o");
      addPrefix("http://purl.obolibrary.org/obo/",                                                              "pobo");
      addPrefix("http://www.geneontology.org/formats/oboInOwl#",                                               "oboio");
      addPrefix("http://www.ifomis.org/bfo/1.1#",                                                                "bfo");
      addPrefix("http://www.ifomis.org/bfo/1.1/snap#",                                                      "bfo-snap");
      addPrefix("http://www.ifomis.org/bfo/1.1/span#",                                                      "bfo-span");
      addPrefix("http://purl.org/obo/owl/CHEBI#",                                                         "pobo-chebi");
      addPrefix("http://purl.org/obo/owl/PRO#",                                                             "pobo-pro");
      addPrefix("http://purl.org/obo/owl/GO#",                                                               "pobo-go");
      addPrefix("http://purl.org/obo/owl/OBO_REL#",                                                         "pobo-rel");
      addPrefix("http://obofoundry.org/ro/ro.owl#",                                                           "obo-ro");
      //addPrefix("http://www.obofoundry.org/ro/ro.owl#",                                                       "obo-ro");
      addPrefix("http://logd.tw.rpi.edu/source/fludb-org/dataset/animal-surveillance/vocab/enhancement/1/",       "e1");
      addPrefix("http://logd.tw.rpi.edu/source/fludb-org/provenance/animal-surveillance/avian/version/2010-Nov-30/","avian");
      addPrefix("http://tw.rpi.edu/schema/",                                                                  "twcweb");
      addPrefix("http://www.w3.org/2001/sw/hcls/ns/transmed/",                                              "transmed");

      //addPrefix(PMM.namespace.getName(),                                                     PMM.namespace.getPrefix());
      addPrefix("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#",                                          "d2rq");
      addPrefix("http://knoesis.wright.edu/provenir/provenir.owl#",                                         "provenir");
      addPrefix("http://purl.org/net/provenance/ns#",                                                    "hartig-prov");
      addPrefix("http://xmlns.com/wot/0.1/",                                                                     "wot");
      addPrefix("http://sw.nokia.com/WebArch-1/",                                                           "webarch1");
      addPrefix("http://swan.mindinformatics.org/ontologies/1.2/pav/",                                           "pav");
      addPrefix("http://purl.org/vocab/changeset/schema#",                                                 "changeset");
      addPrefix("http://multimedialab.elis.ugent.be/ontologies/PREMIS2.0/v1.0/premis.owl#",                   "premis");
      addPrefix("http://openprovenance.org/model/opmo#",                                                        "opmo");
      addPrefix("http://purl.org/net/opmv/ns#",                                                                 "opvm");
      //addPrefix("http://mondeca.com/foaf/voaf#",                                                                "voaf");
      addPrefix("http://purl.org/vocommons/voaf#",                                                              "voaf");
      addPrefix("tag:eric@w3.org:2009/tmo/translator#",                                                         "ERIC");
      addPrefix("http://purl.org/cpr/0.75#",                                                                     "cpr");
      addPrefix("http://indivo.org/vocab/xml/documents#",                                                 "indivo-xml");
      addPrefix("http://omdoc.org/ontology#",                                                                  "omdoc");
      addPrefix("http://spinrdf.org/sp#",                                                                       "spin");
      addPrefix("http://purl.org/linked-data/cube#",                                                              "qb");
      addPrefix("http://logd.tw.rpi.edu/source/epa-gov-mcmahon-ethan/dataset/environmental-reports/epatax/T1","epataxon1");
      addPrefix("http://logd.tw.rpi.edu/source/epa-gov-mcmahon-ethan/dataset/environmental-reports/epatax/T3","epataxon3");
      addPrefix("http://logd.tw.rpi.edu/source/rpi-edu-lebot/dataset/ivj2011-vistheory/version/2011-Feb-27/",     "vt");
      addPrefix("http://purl.org/court/def/2009/coin#",                                                         "coin");
      // VIVO:
      addPrefix("http://vivoweb.org/ontology/core#",                                                        "vivocore");
      addPrefix("http://vitro.mannlib.cornell.edu/ns/vitro/0.7#",                                              "vitro");
      addPrefix("http://vivoweb.org/ontology/provenance-support#",                                          "vivoprov");
      addPrefix("http://purl.org/ontology/bibo/",                                                               "bibo");
      addPrefix("http://purl.org/NET/c4dm/event.owl#",                                                          "c4dm");
      addPrefix("http://aims.fao.org/aos/geopolitical.owl#",                                             "fao-geopoli");
      addPrefix("http://vitro.mannlib.cornell.edu/ns/vitro/public#",                                        "vitropub");
      addPrefix("http://vivoweb.org/ontology/scientific-research#",                                          "vivosci");
      addPrefix("http://purl.org/twc/cabig/model/HINTS2005-1.owl#",                                        "hints2005");
      addPrefix("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#",                                           "EVS");
      addPrefix("http://purl.org/twc/cabig/annotations#",                                               "twcabigannot");
      addPrefix("http://purl.org/ontology/olo/core#",                                                            "olo");
      addPrefix("http://purl.org/ontology/mo/",                                                                   "mo");
      addPrefix("http://purl.org/vocab/bio/0.1/",                                                                "bio");
      addPrefix("http://www.w3.org/2006/03/test-description#",                                                "w3test");
      addPrefix("http://govwild.org/ontology/GWOntology.owl#",                                               "govwild");
      addPrefix("http://www.govwild.org/GWOntology.owl#",                                                 "govwwwwild");
      addPrefix("http://govwild.org/ontology/0.3/GWOntology.rdf#",                                         "govwild03");
      addPrefix("http://www.w3.org/ns/earl.rdf#",                                                               "earl");
      addPrefix("http://sweet.jpl.nasa.gov/2.2/repr.owl#",                                                   "swt-rep");
      addPrefix("http://sweet.jpl.nasa.gov/2.2/reprSciUnits.owl#",                                       "swt-sciunit");
      addPrefix("http://sweet.jpl.nasa.gov/2.2/reprMath.owl#",                                              "swt-math");
      addPrefix("http://sweet.jpl.nasa.gov/2.2/reprMathOperation.owl#",                                   "swt-mathop");
      addPrefix("http://www.daml.org/services/owl-s/1.2/Service.owl#",                                          "owls");
      addPrefix("http://www.daml.org/services/owl-s/1.2/Process.owl#",                                          "owlp");
      addPrefix("http://www.daml.org/services/owl-s/1.2/Grounding.owl#",                                        "owlg");
      addPrefix("http://www.daml.org/services/owl-s/1.2/Profile.owl#",                                    "owlsprofle");
      addPrefix("http://trust.utep.edu/visko/ontology/visko-service-v3.owl#",                               "visko3sv");
      addPrefix("http://trust.utep.edu/visko/ontology/visko-operator-v3.owl#",                              "visko3op");
      addPrefix("http://trust.utep.edu/visko/ontology/visko-view-v3.owl#",                                  "visko3vw");

      // for plunk analysis: should go away:
      /*addPrefix("http://trust.utep.edu",                                                                        "utep");
      addPrefix("http://escience.rpi.edu",                                                                  "escience");
      addPrefix("http://www.rpi.edu/~michaj6",                                                               "michaj6");
      addPrefix("http://inference-web.org",                                                                    "iworg");
      addPrefix("http://logd.tw.rpi.edu",                                                                       "logd");
      addPrefix("http://inference-web.org/proofs/wino/",                                                        "wino");
      addPrefix("http://escience.rpi.edu/2010/mlso/PML/",                                                     "spcdis");*/
      // ^^ plunk analysis
      addPrefix("http://www.w3.org/ns/dcat#",                                                                   "dcat");
      addPrefix("http://data.lirmm.fr/ontologies/vdpp#",                                                        "vdpp");
      addPrefix("http://purl.org/void/provenance/ns/",                                                         "voidp");
      addPrefix("http://w3.org/ProvenanceOntology.owl#",                                                         "pil");
      addPrefix("http://purl.org/yabo/frbr/",                                                               "yabofrbr");
      addPrefix("http://www.mygrid.org.uk/mygrid-moby-service#",                                                "moby");
      addPrefix("http://www.mygrid.org.uk/ontology#",                                                         "mygrid");
      addPrefix("http://purl.org/twc/ontology/frir.owl#",                                                       "frir");
      //addPrefix("http://dvcs.w3.org/hg/prov/raw-file/tip/ontology/ProvenanceOntology.owl#",                     "prov");
      addPrefix("http://dvcs.w3.org/hg/prov/raw-file/tip/ontology/examples/ontology-extensions/crime-file/instances/example-1/crime.owl#","eg1");
      addPrefix("http://dvcs.w3.org/hg/prov/raw-file/tip/ontology/examples/ontology-extensions/crime-file/crime.owl#","ext");
      addPrefix("http://www.rdfabout.com/rdf/usgov/geo/us/",                                              "govtrackus");
      addPrefix("http://escience.rpi.edu/ontology/sesf/s2s/2/0/",                                                "s2s");
      addPrefix("http://www.ordnancesurvey.co.uk/ontology/Datatypes.owl#",                                    "osukdt");
      //addPrefix("http://www.ordnancesurvey.co.uk/ontology/Datatypes.owl#",                                  "ord-data");
      addPrefix("http://www.w3.org/ns/formats/",                                                             "formats");
      addPrefix("http://linkedevents.org/ontology/",                                                            "lode");
      addPrefix("http://www.loa-cnr.it/ontologies/DUL.owl#",                                                     "dul");
      //addPrefix("http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#",                                        "dul");
      addPrefix("http://purl.org/NET/cidoc-crm/core#",                                                     "cidoc-crm");
      addPrefix("http://purl.org/marl/ns#",                                                                     "marl");
      addPrefix("http://protege.stanford.edu/plugins/owl/dc/protege-dc.owl#",                              "protegedc");
      //addPrefix("http://protege.stanford.edu/plugins/owl/dc/protege-dc.owl#",                              "dcprotege");
      addPrefix("http://www.nanopub.org/nschema#",                                                           "nanopub");
      addPrefix("http://purl.org/twc/vocab/datafaqs#",                                                      "datafaqs");
      addPrefix("http://purl.org/twc/vocab/vsr#",                                                                "vsr");
      addPrefix(SIO.BASE_URI,  SIO.PREFIX);
      addPrefix(BTE.BASE_URI,  BTE.PREFIX);
      addPrefix(PML3.BASE_URI, PML3.PREFIX);

      addPrefix("http://yago-knowledge.org/resource/",                                                          "yago");
      addPrefix("http://rdf.alchemyapi.com/rdf/v1/s/aapi-schema#",                                              "aapi");
      this.addPrefix("http://purl.org/twc/vocab/datacarver#", "crv");
      this.addPrefix("http://purl.org/twc/vocab/centrifuge#", "centrifuge");
   }
   
   public static final String USAGE = "DefaultPrefixMappings [prefix [prefix]*] | endpoint named_graph ";
   /**
    * usage: DefaultPrefixMappings prefix [prefix]*
    * 
    * Usage:
    *    java edu.rpi.tw.string.pmm.DefaultPrefixMappings
    *        whoidata      : http://4dgeo.whoi.edu/data/
    *        ventxls       : http://4dgeo.whoi.edu/data/CSVtoRDF_7ccad328-899c-4056-a9d6-2b19c9ecd507/
    *        fao-geopoli   : http://aims.fao.org/aos/geopolitical.owl#
    *        ...
    *        
    *    java edu.rpi.tw.string.pmm.DefaultPrefixMappings prefix.cc
    *        (similar to above, but from prefix.cc not the pre-defined in the class)
    *        
    *    java edu.rpi.tw.string.pmm.DefaultPrefixMappings prov
    *        prov : http://www.w3.org/ns/prov#
    *    
    *    java edu.rpi.tw.string.pmm.DefaultPrefixMappings demo http://www.w3.org/ns/prov#Activity
    *        canAbbreviate: true
    *        bestNamespaceFor: http://www.w3.org/ns/prov#
    *        bestPrefixFor: prov
    *        bestQNameFor: prov:Activity
    *        bestLocalNameFor: Activity
    *        bestLabelFor: Activity
    *        bestQNameRef: prov:Activity
    *        tryQName: prov:Activity
    *        expandQNameString: http://www.w3.org/ns/prov#Activity
    */
   public static void main(String[] args) {
      PrefixMappings pmap = DefaultPrefixMappings.getInstance();
      if( args.length == 0 ) {
         System.out.println(PrettyTurtleWriter.prefixMappingAsString(pmap.getNamespaces()));
      }else if( args.length > 1 && "demo".equals(args[0]) ) {
         for( int i=1; i<args.length; i++ ) {
         	System.out.println("canAbbreviate: "+pmap.canAbbreviate(args[i]));
         	System.out.println("bestNamespaceFor: "+pmap.bestNamespaceFor(args[i]));
         	System.out.println("bestPrefixFor: "+pmap.bestPrefixFor(args[i]));
         	System.out.println("bestQNameFor: "+pmap.bestQNameFor(args[i]));
         	System.out.println("bestLocalNameFor: "+pmap.bestLocalNameFor(args[i]));
         	System.out.println("bestLabelFor: "+pmap.bestLabelFor(args[i]));
         	System.out.println("bestQNameRef: "+pmap.bestQNameRef(args[i]));
         	System.out.println("tryQName: "+pmap.tryQName(args[i]));
         	System.out.println("expandQNameString: "+pmap.expandQName(args[i]));
         }
      }else if( args.length == 1 && "prefix.cc".equals(args[0]) ) {
         pmap = new PrefixMappings("http://prefix.cc/popular/all.file.vann");
         System.out.println(PrettyTurtleWriter.prefixMappingAsString(pmap.getNamespaces()));
      }else if( args.length == 2 && ResourceValueHandler.isURI(args[0])) {
         pmap = new PrefixMappings(args[0],args[1]);
         System.out.println(PrettyTurtleWriter.prefixMappingAsString(pmap.getNamespaces()));
      }else {
         
         for( int i=0; i<args.length; i++ ) {
            if( pmap.usesAbbreviation(args[i]) ) {
               try {
                  System.out.println(PrettyTurtleWriter.printNamespace(pmap.getNamespace(args[i])));
               } catch (NamingException e) {
                  e.printStackTrace();
               }
            }
         }
      }
   }
}