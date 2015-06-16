package edu.rpi.tw.data.csv;

import java.io.PrintStream;

/**
 * "This" dataset is a LayerDataset.
 */
public interface NamespaceCalculator {
   
   /**
    * Replace occurrences of "[/sdv]","[/sd]", "[/s]", and "[e]" with the dataset-naming parameters
    * source_identifier, dataset_identifier, dataset_version, and enhancement_identifier.
    * 
    * @param template
    * @return
    */
   String fillTemplate(String template);
   
   
   
   // Namespaces
   
   
   
   /**
    * Template "[/s]"
    * 
    * @return e.g.
    */
   String getNamespaceOfSource();
   
   /**
    * Template "[/sd]"
    */
   String getNamespaceOfAbstractDataset();

   /**
    * Template "[/sD]"
    * 
    * @return
    */
   String getNamespaceOfAbstractSubdataset();

   /**
    * 
    * @return
    */
   String getNamespaceOfVocab();
   
   /**
    * 
    * @return
    */
   String getNamespaceOfRawProperty();
   
   /**
    * 
    * @return
    */
   String getNamespaceOfEnhancementProperty();


   /**
    * 
    * @return
    */
   String getNamespaceOfVersionedProvenance();
   


   // URIs
   
   
   /**
    * 
    */
   String getURIOfDatasetSource();
   
   /**
    * 
    * @return
    */
   String getURIOfSiteLevelVoID();
   
   /**
    * 
    * @return
    */
   String getURIOfSiteLevelDataset();
   
   /**
    * 
    * @return
    */
   String getURIOfAbstractDataset();
   
   /**
    * Excludes subject discriminator
    * @return
    */
   String getURIOfVersionedDataset();
   
   /**
    * Includes subject discriminator
    * 
    * @return
    */
   String getURIOfVersionedDiscriminatedDatasetLayer();
   
   /**
    * 
    * @return
    */
   String getURIOfDatasetType();
   
   /**
    * 
    * @return
    */
   String getURIOfDiscriminator();

   /**
    * 
    * @return
    */
   String getURIOfDatasetSameAsSubset();
   
   /**
    * 
    * @return
    */
   String getURIOfDatasetMetaData();
   
   
   /**
    * 
    * @return
    */
   String getURIOfLayerDatasetSample();
   
   /**
    * 
    * @return
    */
   String getURIOfLayerDataset();

   /**
    * 
    * @return
    */
   String getURIOfDumpFileSubsetSameAs();
   
   
   
   // Filespaces
   
   
   
   /**
    * 
    * @return
    */
   String getFilespaceOfVersionedProvenance();
   
   
   
   // Dumpfile URIs
   
   
   
   /**
    * 
    * @return
    */
   String getURIOfDumpFileVersionedLayer();

   /**
    * 
    * @return
    */
   String getURIOfDumpFileVersioned();
   
   /**
    * 
    * @return
    */
   String getURIOfDumpFileSubsetMeta();
   
   /**
    * 
    * @return
    */
   String getURIOfDumpFileDatasetSample();
   
   
   
   // Pages
   
   
   
   /**
    * 
    */
   String getURIOfDatasetSourcePage();
   
   /**
    * 
    * @return
    */
   String getURIOfDatasetPage();

   // cross-cut versions:
   
   /**
    * 
    * @return
    */
   String getURIOfDatasetSameAsSubsetPage();
   
   /**
    * 
    * @return
    */
   String getURIOfDatasetMetaDataPage();
   
   // -- void version hierarchy
   
   /**
    * 
    */
   String getURIOfVersionedDatasetPage();
   
   /**
    * 
    * @return
    */
   String getURIOfLayerDatasetSamplePage();
   
   /**
    * 
    */
   String getURIOfVersionedDatasetLayerPage();
   
   
   /**
    * Print samples of all namespaces provided by this NamespaceCalculator.
    * 
    * @param stream - the stream to print to.
    */
   void printNamespaces(PrintStream stream);
}