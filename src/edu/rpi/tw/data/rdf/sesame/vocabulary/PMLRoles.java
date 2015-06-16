package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * 
 */
public class PMLRoles {
   
   public static final ValueFactory vf = ValueFactoryImpl.getInstance();
   
   public static final String P21_NAMESPACE  = "http://inference-web.org/2.1exper/pml-provenance.owl#";
   
   // EXPERIMENTAL
   public final static URI HAS_ANTECEDENT_ROLE = vf.createURI(P21_NAMESPACE+"hasAntecedentRole");
   public final static URI ANTECEDENT_ROLE     = vf.createURI(P21_NAMESPACE+"AntecedentRole");
   public final static URI HAS_ANTECEDENT      = vf.createURI(P21_NAMESPACE+"hasAntecedent");
   public final static URI HAS_ROLE            = vf.createURI(P21_NAMESPACE+"hasRole");
   
   public static final URI INPUT      = vf.createURI("http://inference-web.org/registry/ROLE/Input.owl#",     "Input");
   public static final URI PARAMETERS = vf.createURI("http://inference-web.org/registry/ROLE/Parameters.owl#","Parameters");
}