package org.nees.rbnb.marker;

import java.util.Properties;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.InvalidPropertiesFormatException;


/**
 * A class that extends the java Properties (@see java.util.Properties) in order to manage
 * MarkerEvent(s) and to populate EventMarker channels in a DataTurbine
 * 
 *
 */
public class MarkerEvent extends Properties {

  public static final String MIME_TYPE = "x-neesEvent";
  
  public static Color startColor = Color.green;
  public static Color stopColor = Color.red;
  public static Color noteColor = Color.white;
  public static Color danielBlue = Color.blue;
  
  public MarkerEvent() {

    this.setProperty("mimeType", MIME_TYPE);
    
  }

  public void setFromEventXml(String eventXml) throws InvalidPropertiesFormatException, IOException {
    
    ReaderToStream inStream = new ReaderToStream(new StringReader(eventXml));
    this.loadFromXML(inStream);  // converts the xml event string to this object properties
  
  }
  
  public String toEventXmlString() throws IOException {
    
    String xml = this.toPropertiesXmlString(); 
    
    return xml;
  }
  
  
  private String toPropertiesXmlString() throws IOException {
      StringWriter sw = new StringWriter ();
      this.storeToXML((OutputStream)(new WriterToStream(sw)), null);  // converts this object properties to an xml event string
      return sw.toString ();
  }

  
  
  /**
   * Helper inner class to transform a StringWriter to an OutputStream 
   *
   */
  private class WriterToStream extends OutputStream {
      StringWriter itsStringWriter;
      WriterToStream (StringWriter sw)
      {
          itsStringWriter = sw;
      }
      public void write (int ch) throws IOException
      {
          itsStringWriter.write (ch);
      }
  }

  /**
   * Helper inner class to transform a StringReader to an InputStream
   *
   */
  
  private class ReaderToStream extends InputStream {
      StringReader itsStringReader;
      ReaderToStream (StringReader sw)
      {
          itsStringReader = sw;
      }
      public int read () throws IOException
      {
          return itsStringReader.read  ();
      }
  }

  
}
