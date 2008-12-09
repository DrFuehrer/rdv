/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
 * Copyright (c) 2008 Palta Software
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.rdv.action;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.rdv.ui.UIUtilities;
import org.rdv.util.OpenSeesFileFilter;


/**
 * An action to import data from OpenSees XML files.
 * 
 * @author  Moji Soltani
 * @author  Jason P. Hanley
 */
public class OpenSeesDataImportAction extends DataViewerAction {
  
  /** serialization version identifier */
  private static final long serialVersionUID = -6446885350157840581L;

  /** Reader for input file  */
  private BufferedReader buffReader;
  
  /** writer for output file */
  private BufferedWriter buffWriter;

  /**
   * Constructor to show text/description
   */
  public OpenSeesDataImportAction() {
    super("Import OpenSees data file",
        "Import local data to RBNB server");
  }
 
  /**
   * Prompts the user for the input OpenSees data file and uploads the data to
   * the RBNB server.
   */
  public void actionPerformed(ActionEvent ae) {
    File inputDataFile = UIUtilities.getFile(new OpenSeesFileFilter(), "Import");
    if (inputDataFile == null) {
      return;
    }
    
    File openSeesDataFile;    
    try {
      openSeesDataFile = createOSDataFile(inputDataFile);
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null,
          "There was an error importing the OpenSees XML file.",
          "Import failed",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    String sourceName = inputDataFile.getName();
    int dotIndex = sourceName.lastIndexOf('.');
    if (dotIndex != -1) {
      sourceName = sourceName.substring(0, dotIndex);
    }
    
    ActionFactory.getInstance().getDataImportAction().importData(openSeesDataFile, sourceName, true);
  }
  
  /**
   * Method to create flat OpenSees output data file from XML input
   * 
   * @param input XML dataFile
   * @return flat output file
   * @throws IOException
   */
  private File createOSDataFile(File inputDataFile) throws IOException {
    File outputDataFile = File.createTempFile("OpenSees", ".dat");
    
    try {
      openReadWriteBuffers(inputDataFile, outputDataFile);
      
      writeOutputHeader(inputDataFile);
      
      writeOutputData(inputDataFile);
      
      closeReadWriteBuffers();
    } catch (IOException e) {
      outputDataFile.delete();
      
      throw e;
    }
    
    return outputDataFile;
  }

  /**
   * Writes all data between <Data></Data> tags to output
   * 
   * @param input XML file
   * @throws IOException
   */
  private void writeOutputData(File input) throws IOException {
    String line;

    buffReader = new BufferedReader(new FileReader(input));

    while ((line = buffReader.readLine()) != null) {

      if (!line.contains("<Data>")) {
        continue;
      } else {  

        while ((line = buffReader.readLine()) != null) {
          
          if (!line.contains("</Data>")) {
            line = line.trim().replaceAll(" ", "\t");
            writeLine(line, buffWriter);

          } else {
            break;
          }
          
        }
        
      }
    
    }
  }

  /**
   * Write Header extracted from XML input
   * 
   * @param inputFile OpesSees input File
   * @throws IOException
   */
  private void writeOutputHeader(File inputFile) throws IOException {
    // parse header string from XML file
    String header = getHeaderString(inputFile);
    if (header != null) {
      writeLine(header.trim(), buffWriter);
    }
  }
  
  /** Transformation of input header xml file using the stylesheet
   * 
   * @param input OpenSees XML file
   * @return Parsed string of the header
   */
  private String getHeaderString(File input) {
    try {
      
      Source xmlSource = new StreamSource(input);

      String styleSheet = "org/rdv/action/resources/OpenSeesHeader.xslt";
      URL stylesheetUrl = getClass().getClassLoader().getResource(styleSheet);
      
      if (stylesheetUrl == null) 
        return null;
      
      Source xsltSource = new StreamSource(stylesheetUrl.openStream());
    
      StringWriter writer = new StringWriter();
      Result result = new StreamResult(writer);
      
      // create an instance of TransformerFactory
      TransformerFactory transFact = TransformerFactory.newInstance( );
 
      Transformer trans = transFact.newTransformer(xsltSource);
  
      trans.transform(xmlSource, result);
      
      return writer.toString();
      
    } catch (TransformerConfigurationException tce) {
      tce.printStackTrace();
      return null;

    } catch (TransformerException te) {
      te.printStackTrace();
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * write a line
   * 
   * @param line to write
   * @param writer target writer
   * @throws IOException
   */
  private static void writeLine(String line, BufferedWriter writer) throws IOException {
    writer.write(line + "\n");
  }

  
  /**
   * open buffers
   * 
   * @param input File for reading
   * @param output File for writing
   * @throws FileNotFoundException
   * @throws IOException
   */
  private void openReadWriteBuffers(File input, File output) throws IOException {
    buffReader = new BufferedReader(new FileReader(input));
    buffWriter = new BufferedWriter(new FileWriter(output));    
  }

  /**
   * close buffers
   * 
   * @throws IOException
   */
  private void closeReadWriteBuffers() throws IOException {
    if (buffReader != null) {
      buffReader.close();
    }
    
    if (buffWriter != null) {
      buffWriter.close();
    }
  }
  
}