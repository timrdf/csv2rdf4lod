package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;

import edu.rpi.tw.data.rdf.sesame.query.QueryletProcessor;
import edu.rpi.tw.data.rdf.utils.pipes.Constants;

/**
 * 
 */
public class MultiplierQuerylet extends    ColumnEnhancementQuerylet<Set<Double>> {

   private double multiplier = 1;
   protected HashSet<Double> multipliers;
   
   public MultiplierQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
      this.multipliers = new HashSet<Double>();
   }

   @Override
   public String getQueryString(Resource context) {
      this.multiplier = 1;
      this.addNamespace("rdfs","xsd", columnPrefix(), "conversion");
      
      String select       = "?multiplier";
      String graphPattern = "?col "+columnPO()+";                  \n"+
                            "     conversion:multiplier ?multiplier . ";
      String orderBy      = "";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      double mult = Double.parseDouble(bindingSet.getValue("multiplier").stringValue());
      this.multipliers.add(mult);
      this.multiplier *= mult;
      System.err.println(getClass().getSimpleName() + "("+this.csvColumnIndex+") ."+this.multiplier+".");
   }
   
   /**
    * Return the multiplication of all multipliers.
    * 
    * @return
    */
   public double getMultiplier() {
      return this.multiplier;
   }

   /**
    * Return all multipliers found.
    */
   @Override
   public Set<Double> get() {
      return this.multipliers;
   }
   
   public static final String USAGE = "MultiplierQuerylet - csvColumnIndex [csvColumnIndex...]";
   /**
    * 
    * @param args - 
    */
   public static void main(String[] args) {

      if( args.length < 2 ) {
         System.err.println("usage: "+USAGE);
         System.exit(1);
      }
      Repository repository = Constants.getPipeRepository();

      for( int i = 1; i < args.length; i++ ) {
         MultiplierQuerylet handler = new MultiplierQuerylet(null,Integer.parseInt(args[i]));
         System.err.println(handler.getQueryString());
         QueryletProcessor.processQuery(repository, handler);
         System.out.println(handler.getMultiplier());
      }
   }
}