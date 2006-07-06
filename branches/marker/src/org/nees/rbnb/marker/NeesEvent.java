package org.nees.rbnb.marker;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class that extends the java Properties (@see java.util.Properties) in order to manage
 * Event information to be contained in a DataTurbine marker.
 * 
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author Terry E. Weymouth <weymouth@umich.edu>
 * @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * @since 050916
 *
 * Copyright (c) 2005,6 NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * All rights reserved. See full notice in the source, at the end of the file.
 *
 */
public class NeesEvent extends Properties implements Comparable {

    public static String[] markerFieldNames = { "content", "label", "source",
                                                "timestamp", "type" };
    
    public static String[] eventTypes = { "annotation", "comment", "keep_alive",
                                          "other", "start", "stop" };
    
    public static Color startColor = Color.green; //new Color (33, 108, 38);
    public static Color stopColor = Color.red;
    public static Color noteColor = Color.white;
    public static Color danielBlue = Color.blue; //new Color (200, 235, 243);
    private static Log log = LogFactory.getLog (NeesEvent.class.getName ());
    
    public static String MIME_TYPE = "x-neesEvent";
    
    public String rbnbChannel;

    public NeesEvent () {
       super ();
       this.setProperty ("mimeType", MIME_TYPE);
    } // constructor ()
    
    public NeesEvent (String channel) {
       super ();
       this.setProperty ("mimeType", MIME_TYPE);
       this.rbnbChannel = channel;
    }
    
    /** A method that instantiates a new object by parsing @param XMl as a string. */
    
    public void setFromEventXml (String eventXml) throws TransformerException, InvalidPropertiesFormatException, IOException
    {
        StringWriter sw = new StringWriter ();
        StringReader xml = new StringReader (eventXml);
        StringReader stylesheet = new StringReader (Stylesheets.EVENTS_TO_PROPERTIES_XSL);
                        
        // Set up the transformer for the output
        TransformerFactory tFactory =
            TransformerFactory.newInstance ();
        StreamSource stylesource = new StreamSource (stylesheet); 
        Transformer transformer = tFactory.newTransformer (stylesource);

        // apply the XSL Transform
        StreamSource source = new StreamSource (xml);
        StreamResult result = new StreamResult (sw);
        transformer.transform (source, result);
        
        //log.debug ("Setting NeesEvent from XML text:\n" + sw.toString ());
        
        ReaderToStream inStream = new ReaderToStream (new StringReader (sw.toString ())); // pipes here? -- tew
        this.loadFromXML (inStream);
        
    }

    /** Wrap the standard Properties get (). */
    public String eventGet (String fieldName) {
        return (String)this.get (fieldName);
    }

    /** Nicer string formatting than the standard Hashtable toString 
     * @throws IOException 
     */
    public String toEventXmlString () throws IOException, TransformerException
    {
        StringWriter sw = new StringWriter ();
        StringReader xml = new StringReader (this.toPropertiesXmlString ()); // pipes here? - tew
        StringReader stylesheet = new StringReader (Stylesheets.PROPERTIES_TO_EVENTS_XSL);
        
        // Strip out lines that generate an error (not sure why!) - tew Sept 16, 2005
        // strip the <?xml ... ?> tag
        while ('>' != xml.read ()) {}
        // strip the <!DOCTYPE ... > tag
        while ('>' != xml.read ()) {}
                
        // Set up the transformer for the output
        TransformerFactory tFactory =
            TransformerFactory.newInstance ();
        StreamSource stylesource = new StreamSource (stylesheet); 
        Transformer transformer = tFactory.newTransformer (stylesource);

        // apply the XSL Transform
        StreamSource source = new StreamSource (xml);
        StreamResult result = new StreamResult (sw);
        transformer.transform (source, result);
        
        return sw.toString ();
    }

    private String toPropertiesXmlString () throws IOException
    {
        StringWriter sw = new StringWriter ();
        this.storeToXML ((OutputStream)(new WriterToStream (sw)), null);
        return sw.toString ();
    }
    
    public static void main (String[] args) {
        NeesEvent app = new NeesEvent ();
        
        System.out.println("Starting... ");
        try
        {
            // stuff dummy data
            for (int i = 0; i < markerFieldNames.length; i++)
            {
                app.setProperty(markerFieldNames[i],markerFieldNames[i]);
            }
            app.setProperty("timestamp","" + (((double)System.currentTimeMillis ()) / 1000.0));
            
            // get the XML and print it
            String xml = app.toEventXmlString ();
            System.out.println ("The original XML is:\n" + xml + "\n");
    
            // generate a copy from the XML, get and print it's xml
            NeesEvent copy = new NeesEvent ();
            copy.setFromEventXml (xml);
            String copyXml = copy.toEventXmlString ();
            System.out.println ("The copy XML is:\n" + copyXml + "\n");
            
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    } // main ()

    
    private class WriterToStream extends OutputStream
    {
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
    
    private class ReaderToStream extends InputStream
    {
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
    
    public int compareTo (Object ev) {
       double theDiff = Double.parseDouble (this.getProperty ("timestamp")) -
      Double.parseDouble ( ((NeesEvent)ev).getProperty ("timestamp") );
      if (0 < theDiff) {
        return 1;
      } else if (theDiff < 0) {
        return -1;
      } else { /* they must be equal (never happens, as the data being sorted
        is from a HashMap */
        return 0;
      }
    }
    
} // class


/* Copyright Notice:
*
* Copyright (c) 2005, NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
*    * Redistributions of source code must retain the above copyright notice,
* this list of conditions and the following disclaimer.
*    * Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimer in the 
* documentation and/or other materials provided with the distribution.
*    * Neither the name of the San Diego Supercomputer Center nor the names of
* its contributors may be used to endorse or promote products derived from this
* software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
