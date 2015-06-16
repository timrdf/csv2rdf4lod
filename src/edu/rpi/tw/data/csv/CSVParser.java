package edu.rpi.tw.data.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.csvreader.CsvReader;

import edu.rpi.tw.data.csv.querylets.column.ColumnEnhancementQuerylet;

/**
 * "554","37570",", PP. 94 , 96","31017",", PP. 94 , 96","C:\Messages\HUID\Biological Chronology 1990-97.txt"
 * 
 * Does not work. Produces
 * 
 * :concept_ PP. 94  a :Concept; :mentioned_in :document_554; rdfs:label " 96" .
 * 
 */
public class CSVParser implements CSV {

   private static Logger logger = Logger.getLogger(CSVParser.class.getName());
   
   public static Integer NO_SAMPLE_LIMIT = -1;
   
   /**
    * 
    * @param reader
    * @return
    * @throws IOException
    */
   public static Collection<CSVRecord> doReader(BufferedReader reader) throws IOException {
      return doReader(reader, false, 0);
   }

   /**
    * 
    * @param reader
    * @return
    * @throws IOException
    */
   public static Collection<CSVRecord> doReader(BufferedReader reader, int headerDelay) throws IOException {
      return doReader(reader, false, headerDelay);
   }

   /**
    * @param reader
    * @param stripQuotes
    * @param headerLine  - -1: there is no header; make surrogate headers  // TODO: change to just header line number 1 based.
    *                       1: do not delay parsing the header (it is first)
    *                       2: the first line does not contain the header, it is on the 2nd line.
    *                       3: the first 2 lines do not contain the header, it is on the 3rd line.
    *                       @deprecated
    */
   public static Collection<CSVRecord> doReader(BufferedReader reader,
                                                boolean stripQuotes, int headerLine) throws IOException {
      
      boolean headersPresent = headerLine >= 0; // 0: do not delay parsing the header
      
      int lineNum = 1;
      
      Collection<CSVRecord> recs = new ArrayList<CSVRecord>();
      
      // Read the top matter (titles, etc)
      StringBuffer top = new StringBuffer();
      String line;
      while (headersPresent && lineNum < headerLine && (line = reader.readLine()) != null) {
         //System.err.println("consuming top matter: "+line);
         top.append(line+"\n");
         lineNum++;
      }
      //if( top.length() > 0 ) System.err.println("TOP CONTENT: "+top); // TODO: can't return this b/c this is a static method.
      
      // Read the header row.
      List<String> headers = null;
      if( headersPresent ) {
         headers = CSVParser.doHeader(reader, stripQuotes);
         lineNum++;
      }else {
         headers = new ArrayList<String>();
      }

      // Read each data row.
      while ((line = reader.readLine()) != null) {
         lineNum++;
         List<String> data = tokenizeCSVLine(line, stripQuotes);
         
         // If data extends into columns for which no headers were defined.
         for(int h=headers.size(); h < data.size(); h++) {
            headers.add("column "+(h+1));
            System.err.println(ColumnEnhancementQuerylet.REPORT_INDENT + "No header provided for column "+(h+1)+
                               "; making surrogate column name. WARNING: extra cells omitted in conversion.");
         }
         
         CSVRecord rec = new CSVRecord(headers);
         
         Iterator<String> iter = headers.iterator();
         for(int col = 0; col < data.size() && iter.hasNext(); col++) {
            rec.addItem(col, data.get(col));
            //System.err.println("("+lineNum+", "+col+") "+headers.get(col)+" = ."+data.get(col)+".");
         }
         recs.add(rec);
      }
      return recs;
   }

   // TODO: problems with comma right before ending quote.
   // TODO: data-gov 1450 line 4766: 
   // Minnesota,,"GROUP HEALTH INC,",HealthPartners Freedom Plan,1876 Cost,Clinical Pharmacy Program Manager,Rehrauer,Daniel,J,1-952-967-5133,,,daniel.j.rehrauer@healthpartners.com,8170 33rd Avenue South,PO Box 1309,Minneapolis,MN,55440-1309
   // TODO: data-gov 1492
   // 1551,09/16/2004,Hurricane,Florida,Santa Rosa,"EAST MILTON WATER SYSTEM INC,",No,4,"$20,984.20"
   
   /**
    * visitRecords using custom parser. Has but where values end in commas:    ,"My Company, Inc.,",
    * 
    * @deprecated - replaced by {@link #visitRecords(BufferedReader, char, Integer, Set, Integer, Integer, RowHandler)} b/c it uses http://javacsv.sourceforge.net/
    * 
    * @param reader
    * @param stripQuotes
    * @param headerLine  - -1: there is no header; make surrogate headers  // TODO: change to just header line number 1 based.
    *                       1: do not delay parsing the header (it is first)
    *                       2: the first line does not contain the header, it is on the 2nd line.
    *                       3: the first 2 lines do not contain the header, it is on the 3rd line.
    */
   public static void visitRecords(BufferedReader reader, boolean stripQuotes, 
                                   Integer headerLine, Integer dataStartRow, Integer dataEndRow, RowHandler visitor) 
      throws IOException {
      
      boolean headersPresent = headerLine >= 0; // 0: do not delay parsing the header
      
      int lineNum = 0;
      String line;
      
      // Read the top matter (titles, etc)
      StringBuffer top = new StringBuffer();
      while (headersPresent && lineNum+1 < headerLine && (line = reader.readLine()) != null) {
         lineNum++;
         //System.err.println("consuming top matter: "+line);
         top.append(line+"\n");
      }

      visitor.visitTopMatter(top.toString());
      
      // Read the header row.
      List<String> headers = null;
      if( headersPresent ) {
         //System.err.println("headers present");
         headers = CSVParser.doHeader(reader, stripQuotes);
         lineNum++;

         for( int c = 0; c < headers.size(); c++ ) {
            visitor.visitHeader(lineNum, c+1, headers.get(c));
         }
      }else {
         headers = new ArrayList<String>();
      }
      
      // Skip rows between header and dataStartRow
      while( (dataStartRow == null || lineNum+1 < dataStartRow) && (line = reader.readLine()) != null ) {
         lineNum++;
         //System.err.println(lineNum+": skipping between header "+headerLine+" and dataStartRow "+dataStartRow);
      }
      
      // Read each data row.
      while( (dataEndRow == null || lineNum < dataEndRow) && (line = reader.readLine()) != null ) {
         lineNum++;
         List<String> data = tokenizeCSVLine(line, stripQuotes);
         
         // If data extends into columns for which no headers were defined.
         for( int h=headers.size(); h < data.size(); h++ ) {
            System.err.println(ColumnEnhancementQuerylet.REPORT_INDENT + "No header provided for column "+(h+1)+"; "+
                                                                         "making surrogate column name.");
            headers.add("column "+(h+1));
            visitor.visitHeader(lineNum,h+1,headers.get(h));
         }
         
         CSVRecord rec = new CSVRecord(headers);
         
         for(int col = 0; col < data.size(); col++) {
            rec.addItem(col, data.get(col));
            System.err.println("("+lineNum+", "+col+") "+headers.get(col)+" = ."+data.get(col)+".");
         }
         //System.err.println("Parser: "+dataEndRow+" "+lineNum);
         visitor.visit(rec,lineNum);
      }
      
      StringBuffer bottom = new StringBuffer();
      while ((line = reader.readLine()) != null) {
         //System.err.println("consuming top matter: "+line);
         bottom.append(line+"\n");
      }
      visitor.visitBottomMatter(bottom.toString());
   }
   
   /**
    * visitRecords using CSV parser from http://javacsv.sourceforge.net/
    * 
    * see https://github.com/timrdf/csv2rdf4lod-automation/wiki/conversion:HeaderRow
    * 
    * @param bufferedReader         - where to read table from.
    * @param delimiter              - between cell values.
    * @param expectLargeValues      - 
    * @param headerRow              - one based row number for where headers are.
    * @param headerRowDisplacements - set of integer displacements from 'headerRow' that also contain header info.
    * @param dataStartRow           - one based "the first (inclusive) row that contains data."
    * @param dataEndRow             - one based "the last  (inclusive) row that contains data."
    * @param sampleSize             - 
    * @param visitor                - who to give the row record to.
    * @throws IOException 
    */
   // THIS one is currently called by CSVtoRDF (Jul 05 2012)
   // public static void visitRecords(BufferedReader bufferedReader, char delimiter, boolean expectLargeValues,
   public static void visitRecords(InputStream csvStream, char delimiter, boolean expectLargeValues, Charset charset,
                                   Integer headerRow, Set<Long> headerRowDisplacements, 
                                   Integer dataStartRow, Integer dataEndRow, Integer sampleLimit,
                                   RowHandler visitor) throws IOException {
      
      // https://github.com/timrdf/csv2rdf4lod-automation/issues/277
      CsvReader csvReader = new CsvReader(csvStream, delimiter, charset);
      csvReader.setSkipEmptyRecords(false);
      if( expectLargeValues ) {
         csvReader.setSafetySwitch(false);
      }
      
      boolean headersPresent = headerRow > 0; // 0: do not delay parsing the header
      List<String> headers = new ArrayList<String>();
      
      // Read the top matter (titles, etc)
      StringBuffer top = new StringBuffer();
      while (headersPresent && 
             csvReader.getCurrentRecord()+2 < headerRow && // +3 b/c csvReader is zero-based, starts at -1, and we are looking to next.
             csvReader.readRecord()) {
         //logger.finest("consuming top matter on line "+(csvReader.getCurrentRecord()+1)+" before header line ("+headerRow+"): "+csvReader.getRawRecord());
         top.append(csvReader.getRawRecord()+"\n");
         // Cache rows that are referenced relative to the header. TODO: check that this works
         if( headerRowDisplacements.contains(headerRow - csvReader.getCurrentRecord()+1) ) {
            visitor.visitRelativeToHeader(populateRecord(headers,csvReader,visitor), csvReader.getCurrentRecord()+1);
         }
      }

      visitor.visitTopMatter(top.toString());
      
      // Read the header row.
      int numRowsBeforeData = 0; // just used to shortcut parsing when samples are being made.
      if( headersPresent ) {
         csvReader.readRecord();
         System.err.println("headers present at row "+(csvReader.getCurrentRecord()+1)+": "+csvReader.getRawRecord()+"\n");
         
         String[] values = csvReader.getValues();

         for( int c = 0; c < values.length; c++ ) {
            String field = values[c];
            logger.finest("header: "+c+" " +field);
            headers.add(field);
            visitor.visitHeader(csvReader.getCurrentRecord()+1, c+1, field);
            numRowsBeforeData++; // just used to shortcut parsing when samples are being made.
         }
      }
      
      // Skip rows between header and dataStartRow
      while( (dataStartRow == null || csvReader.getCurrentRecord()+2 < dataStartRow) && // +2, 1 for 0-based and 1 for look ahead.
              csvReader.readRecord() ) {
         System.err.println((csvReader.getCurrentRecord()+1)+": skipping between header "+headerRow+" and "+
                            "dataStartRow "+dataStartRow+" "+csvReader.getRawRecord());
         // Cache rows that are referenced relative to the header.
         if( headerRowDisplacements.contains(headerRow - csvReader.getCurrentRecord()+1) ) {
            visitor.visitRelativeToHeader(populateRecord(headers,csvReader,visitor), csvReader.getCurrentRecord()+1);
         }
         numRowsBeforeData++; // just used to shortcut parsing when samples are being made.
      }
      
      // Read each data row.
      int rowcountdebug = 1;
      try {
         while( (dataEndRow == null || csvReader.getCurrentRecord()+1 +1< dataEndRow) && // second +1 added apr 2011: "to specify the last (inclusive) row that contains data."
                 csvReader.readRecord() ) {
            rowcountdebug++;
            //logger.finest("Parser: "+dataEndRow+" "+csvReader.getCurrentRecord()+1);
            
            CSVRecord record = populateRecord(headers, csvReader, visitor);
            
            // Cache rows that are referenced relative to the header.
            if( headerRowDisplacements != null && 
                headerRowDisplacements.contains(headerRow - csvReader.getCurrentRecord()+1) ) {
               visitor.visitRelativeToHeader(record, csvReader.getCurrentRecord()+1);
            }
           
            // Visit as normal.
            visitor.visit(record, csvReader.getCurrentRecord()+1);
            
            // Nothing else will be recognized, so just return. (*5 in case some rows didn't produce a sample).
            if( sampleLimit != null && sampleLimit > 0 && csvReader.getCurrentRecord() > (5*sampleLimit+numRowsBeforeData) ) {
               logger.finest(csvReader.getCurrentRecord() + " > " + 5*sampleLimit + " => returning");
               return;
            }
         }
      }catch (java.io.IOException e) {
         System.err.println("exception on row : " + rowcountdebug);
         e.printStackTrace();
      }
      
      StringBuffer bottom = new StringBuffer();
      while( csvReader.readRecord() ) {
         logger.finest("consuming top matter: "+(csvReader.getColumnCount()+1));
         bottom.append(csvReader.getRawRecord()+"\n");
      }
      visitor.visitBottomMatter(bottom.toString());
   }
   
   /**
    * Pulled from {@link #visitRecords(BufferedReader, char, Integer, Set, Integer, Integer, RowHandler)}
    * b/c called in a few places b/c of visitRelativeHeader.
    * 
    * @param headers
    * @param csvReader
    * @param visitor
    * @return
    */
   private static CSVRecord populateRecord(List<String> headers, CsvReader csvReader, RowHandler visitor) {
      // If data extends into columns for which no headers were defined.
      for( int h = headers.size(); h < csvReader.getColumnCount(); h++ ) {
         System.err.println(ColumnEnhancementQuerylet.REPORT_INDENT + "No header provided for column " + (h + 1) + "; making surrogate column name.");
         headers.add("column " + (h + 1));
         visitor.visitHeader(csvReader.getCurrentRecord() + 1, h + 1, headers.get(h));
      }
      
      CSVRecord record = new CSVRecord(headers);
      
      for( int col = 0; col < csvReader.getColumnCount(); col++ ) {
         try {
            logger.finest("csvReader.get("+col+"): >>>" + csvReader.get(col)+"<<<");
            record.addItem(col, csvReader.get(col));
         }catch (IOException e) {
            e.printStackTrace();
         }
         // System.err.println("("+(csvReader.getColumnCount()+1)+", "+col+") "+headers.get(col)+" = ."+csvReader.get(col)+".");
      }
      return record;
   }
   
   /**
    * 
    * @param reader
    * @param stripQuotes
    * @return
    * @throws IOException
    */
   private static List<String> doHeader(BufferedReader reader, boolean stripQuotes) throws IOException {
      String line;
      List<String> headers = null;

      if ((line = reader.readLine()) != null) {
         headers = tokenizeCSVLine(line, stripQuotes);
      }
      return headers;
   }

   /**
    * This method focuses on getting the exact value for each field in the line.
    * It does not remove initial and final quotes.
    * It does not unescape escaped quotes within the field.
    * It does not unescape escaped commas within the field.
    * 
    * All of these should be done after tokenizing the line.
    * 
    * @param line - the line from the csv file.
    * @param stripQuotes - if true, strip initial and final double quotes.
    * @return
    */
   private static List<String> tokenizeCSVLine(String line, boolean stripQuotes) {
      System.err.println("TOKENIZING: "+line);
      
      // Naive split of line by comma. Results in too many fields if field contains comma.
      String[] data = line.split(",");
      for(int i = 0; i < data.length; i++) {
         System.err.println("   "+i+": "+data[i]);
      }

      ArrayList<String> tokens = new ArrayList<String>();
      int naiveTokenIndex = 0;
      while (naiveTokenIndex < data.length) {
         if (data[naiveTokenIndex].length() > 0 && data[naiveTokenIndex].charAt(0) == '\"') {
            // Skip the token index to the last field that was merged.
            naiveTokenIndex = dealWithQuotedToken(naiveTokenIndex, data, tokens, stripQuotes);
         } else {
            tokens.add(data[naiveTokenIndex]);
         }
         // TODO: an alternative way is to escape commas instead of quoting the entire field.
         naiveTokenIndex++;
      }
      
      System.err.println("   TOKENS:");
      for(int j = 0; j < tokens.size(); j++) {
         System.err.println("      "+j+": "+tokens.get(j));
      }
      return tokens;
   }

   /**
    * "March" through 'data' starting at index 'd' and concatenate values until it is the correct token.
    * Add the token to 'tokens' and return the last index of 'data' that was used to create the token.
    * 
    * This method merges fields that belong together, but were separated by a naive split(",").
    * Adds a single token to 'tokens' and returns the index of the last field used to create the token added.
    * 
    * Side effect: Modifies 'tokens' by adding a single element (token).
    * 
    * @param d - the index of 'data' to start creating a token to add to 'tokens'.
    * @param data - the array of naively-parse tokens (using split(",")).
    * @param tokens - the List to add the correctly-parsed token.
    * @param stripQuotes
    * 
    * @return - the index of the last field in 'data' that was used to create the token added to 'tokens'.
    */
   private static int dealWithQuotedTokenTODO(int d, String[] data, List<String> tokens, boolean stripQuotes) {
      
      StringBuffer token = new StringBuffer(data[d]);
      
      // Append more naively-parsed tokens if appropriate.
      while ( (d+1) < data.length 
              &&
              data[d].charAt(0) == '\"' 
              &&
              !(
               data[d].length() > 1 ||
               data[d].charAt(data[d].length() - 1) != '\"'
              )
            ) {
         d++;
         token.append(","+data[d]);
      }

      if (stripQuotes) {
         tokens.add(stripQuotes(token.toString()));
      } else {
         tokens.add(token.toString());
      }

      return d;
      // TODO: a better way to do this is to just return the index of the last element of 'data' and let the caller
      // iterate to concatenate and add to 'tokens'.
   }
   
   /**
    * This method merges fields that belong together, but were separated by a naive split(",").
    * Adds a single token to 'tokens'
    * 
    * Side effect: Modifies tokens data structure.
    */
   private static int dealWithQuotedToken(int idx, String[] data, List<String> tokens, boolean stripQuotes) {
      String token = data[idx];
      while ( ((idx+1) < data.length) &&
              ((data[idx].length() <= 1) ||
               (data[idx].charAt(data[idx].length() - 1) != '\"')
              )
            )
      {
         token = token.concat(",");
         token = token.concat(data[idx+1]);
         idx++;
      }

      if (stripQuotes) {
         tokens.add(stripQuotes(token));
      } else {
         tokens.add(token);
      }

      return idx;
   }
   
   /**
    * 
    * @param string
    * @return
    */
   private static String stripQuotes(String string) {
      if (string.charAt(0) == '\"' && string.charAt(string.length()-1) == '\"') {
         return string.substring(1, (string.length() - 1));
      } else {
         return string;
      }
   }
}