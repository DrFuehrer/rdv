package org.nees.rbnb;

/**
* @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author Paul Hubbard <hubbard@sdsc.edu>
 * @author Terry E. Weymouth <weymouth@umich.edu>
 * @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * @since 050928
 * Copyright (c) 2005, NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * All rights reserved. See full notice in the source, at the end of the file.
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 * $HeadURL$ 
 */

import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.rbnb.marker.EventMarker;

public class DataTurbine
{
  //~ Static fields/initializers ---------------------------------------------
  
  static Log log = LogFactory.getLog (DataTurbine.class.getName ());
  
  private static final String SERVER_NAME  = "localhost:3333";
  private static final String CHANNEL_NAME = "image";
  private static final String PICTURE_PATH = "./image/";
  
  //~ Instance fields --------------------------------------------------------
  
  private String serverName    = SERVER_NAME;
  private String channelName   = CHANNEL_NAME;
  private String picturePath   = PICTURE_PATH;
  private boolean debugging    = false;
  
  private String      name;
  private Source      source;
  private byte[]      fileBuffer;
  private int         channelId = -1;
  private ChannelMap  cmap;
  
  public static int RBNB_CACHE_SIZE   = 5000; 
  public static int RBNB_ARCHIVE_SIZE = 10000;
  public boolean keepRbnbDataArchive = false;
  public boolean keepRbnbDataCache = true;
  /** A variable to specify the archive mode as described in @see com.rbnb.sapi.Source
    * none   - no Archive is made.
    * load   - load an archive, but do not allow any further writing to it.
    * create - create an archive.
    * append - load an archive, but allow writing new data to it.
    */
  public static String RBNB_ARCHIVE_MODE = "append";
  //~ Constructors -----------------------------------------------------------
  
  public DataTurbine (String name) {
    /* Add in a hook for ctrl-c's and other abrupt death. */
    Runtime.getRuntime ().addShutdownHook (new Thread () {
      public void run () {
        try {
          if (keepRbnbDataArchive || keepRbnbDataCache) {
            closeAndKeep ();
          } else {
            close ();
          }
          log.debug ("Shutdown hook.");
        } catch (Exception e) {
          log.error ("Unexpected error closing DataTurbine.");
        }
      }
    });
    this.name = name;
  }
  
  /** A destructor that will close the RBNB connection
    cache and archive the ring buffer. */
  protected void finalize () {
    try {
      if (keepRbnbDataArchive || keepRbnbDataCache) {
        this.closeAndKeep ();
      } else {
        this.close ();
      }
    } catch (Exception e) {
      log.error ("Unexpected error closing DataTurbine.");
    }
  }
  
  /** A mutator method to set the DataTurbine server name to connect to.
    * @param the name of the DataTurbine server */
  public void setServerName (String newServerName) {
    this.serverName = newServerName;
  }
  
  /** A mutator method to set the name of this DataTurbine client.
    * @param the name of this DataTurbine client */
  public void setName (String newName) {
    this.name = newName;
  }
  
  /** An accessor to get the @return com.rbnb.sapi.Source */
  public Source getSource () {
    return this.source;
  }
  
  public ChannelMap getChannelMap () {
    return this.cmap;
  } 
  
  /** A method that provides
    * @return useful debugging information */
  public String getDebugString () {
    String retVal =  "serverName:\t" + serverName + "\n" +
    "channelName:\t" + channelName + "\n" +
    "picturePath:\t" + picturePath + "\n" +
    "name:\t\t" + name + "\n" +
    "source:\t\t" + source + "\n" +
    "channelId:\t" + channelId + "\n" +
    "cmap:\t\t" + cmap;      
    return retVal;
  }
  
  //~ Methods ----------------------------------------------------------------
  /** A method that will this instance's connection to a DataTurbine server as
    * a source. */
  public void open () throws SAPIException {
    try {
      log.debug ("Turbine server: " + this.serverName);
      log.debug ("Opening turbine: " + this.name);
      this.source = new Source (RBNB_CACHE_SIZE, RBNB_ARCHIVE_MODE, RBNB_ARCHIVE_SIZE);
      this.source.OpenRBNBConnection (this.serverName, this.name);
    } catch (SAPIException sapie) {
      log.error ("Error opening turbine source: " + sapie);
      throw sapie;
    }
    log.debug ("Opening turbine channel");
    this.cmap = new ChannelMap ();
    this.cmap.PutTimeAuto ("timeofday");      
  } // open ()
  
  
  /** A method that will indocate the status of the connection to the RBNB server.
    */
  public boolean isConnected () {
    if (this.source == null) {
      return false;
    } else {
      return this.source.VerifyConnection ();
    }
  } // isConnected ()
  
  
  /**
    * Upload file to data turbine.
   */
  public void upload (String file) {
    log.debug ("Uploading file: " + file + " to " + serverName);
    loadImageFile (file);
    
    try {
      log.debug ("Adding turbine channel \"" + channelName + "\" on " + serverName);
      channelId = cmap.Add (channelName);
      this.cmap.PutTimeAuto ("timeofday");
      this.cmap.PutMime (channelId, "image/jpeg");
    } catch (Exception e) {
      log.error ("Error adding turbine channel: " + e);
    }
    
    
    try {
      log.debug ("Uploading size: " + fileBuffer.length + " bytes to " + serverName);
      this.cmap.PutDataAsByteArray (channelId, fileBuffer);
      this.source.Flush (this.cmap);
    } catch (Exception e) {
      log.error ("Error writing to turbine: " + e);
    }
  } // upload ()
  
  /**
    * Load image file from disk into buffer.
   */
  private void loadImageFile (String file) {
    try {
      File f = new File (picturePath + file);
      FileInputStream in = new FileInputStream (f);
      int fileLength = (int)f.length ();
      byte[] buffer = new byte[fileLength];
      in.read (buffer);
      fileBuffer = buffer;
    } catch (Exception e) {
      log.error ("Error loading image file: " + e);
    }
  } // loadImageFile ()
  
  /** A method that will put a
    * @param string into the DataTurbine on the
    * @param specified channel and
    * @return the index in the @see ChannelMap */
  public int putString (String theString, String targetChannel) {
    try {
      log.debug ("Putting string\n"  + theString + "\ninto channel \"" +
                 targetChannel + "\"" + " on " + serverName);
      try {
        this.channelId = this.cmap.Add (targetChannel);
        this.cmap.PutTimeAuto ("timeofday");
        this.cmap.PutMime (this.channelId, "text/plain");
      } catch (Exception e) {
        log.error ("Error adding turbine channel: " + e);
      }
      this.cmap.PutDataAsString (this.channelId, theString);
      this.source.Flush (this.cmap);
    } catch (Exception e) {
      log.error ("Error writing to turbine: " + e);
    }
    return this.channelId;
  } // putString ()
  
  /** A method that will put a
    * @param marker @see org.nees.rbnb.marker into the DataTurbine on the
    * @param specified channel and
    * @return the index in the @see ChannelMap
    * and ensure that the marker is archived by the DataTurbine. */
  public int putTempMarker (String theMarker, String targetChannel) {
    try {
      log.debug ("Putting marker\n"  + theMarker + "\ninto channel \"" +
                 targetChannel + "\"" + " on " + serverName);
      try {
        this.channelId = this.cmap.Add (targetChannel);
        this.cmap.PutTimeAuto ("timeofday");
        /** This mime type is an arbitrary extension and is needed for
          * application useage of markers (such as RDV).
          * @see org.nees.rbnb.marker */
        this.cmap.PutMime (this.channelId, EventMarker.MIME_TYPE);
      } catch (Exception e) {
        log.error ("Error adding turbine channel: " + e);
      }
      this.cmap.PutDataAsString (this.channelId, theMarker);
      this.source.Flush (this.cmap);
    } catch (Exception e) {
      log.error ("Error writing to turbine: " + e);
    }
    return this.channelId;
  } // putTempMarker ()
  
  /** A method that will put a
    * @param marker @see org.nees.rbnb.marker into the DataTurbine on the
    * @param specified channel and
    * @return the index in the @see ChannelMap
    * and ensure that the marker is archived by the DataTurbine. */
  public int putMarker (EventMarker theMarker, String targetChannel) {
    try {
      try {
        this.channelId = this.cmap.Add (targetChannel);
        
        this.cmap.PutTimeAuto ("timeofday");
        
        /** This mime type is an arbitrary extension and is needed for
          * application useage of markers (such as RDV).
          * @see org.nees.rbnb.marker */
        this.cmap.PutMime (this.channelId, EventMarker.MIME_TYPE);
      } catch (Exception e) {
        log.error ("Error adding turbine channel: " + e.getMessage());
      }
      this.cmap.PutDataAsString (this.channelId, theMarker.toEventXmlString ());
      this.source.Flush (this.cmap);
    } catch (Exception e) {
      log.error ("Error writing to turbine: " + e);
    }
    return this.channelId;
  } // putMarker ()
  
  /**
    * Close the data turbine.
   * The SAPI close methods are not defined to throw any exceptions, so the
   * generic exception is hot-potato'ed here for safety's sake.
   */
  public void close () throws Exception {
    try {
      log.debug ("Closing turbine " + serverName);
      source.CloseRBNBConnection ();
    } catch (Exception e) {
      log.error ("Error closing turbine: " + e);
      throw e;
    }
  } // close ()
  
  /**
    * Close the data turbine, caching and archiving data submitted from this
   * connection. */
  public void closeAndKeep () throws Exception {
    try {
      log.debug ("Closing turbine " + serverName + " with cache and archive.");
      //this.source.Detach ();
      this.source.CloseRBNBConnection (keepRbnbDataCache, keepRbnbDataArchive);
    } catch (Exception e) {
      log.error ("Error closing turbine: " + e);
      throw e;
    }
  } // close ()
  
  public static void main (String[] args) {
    
    DataTurbine app = new DataTurbine ("NEESit_DataTurbine_Wrapper");
    
    //////////////////////////////////////////////////////////////////////////
    /* command-line argument handling */
    Options opts = new Options ();
    CommandLineParser parser = new BasicParser();
    CommandLine cmd = null;
    
    opts.addOption ("a", false, "about");
    opts.addOption ("d", false, "enable debug output");
    opts.addOption ("n", true, "DataTurbine source name to register");
    opts.addOption ("r", true, "DataTurbine server name to connect to as a source." + 
                    " Default is to use a fake channel to act as a source for.");
    
    try {
      cmd = parser.parse (opts, args); 
    } catch (ParseException pe) {
      HelpFormatter formatter = new HelpFormatter ();
      formatter.printHelp ("DataTurbine", opts);
      System.exit (0);
    }
    
    if (cmd.hasOption ("a")) {
      System.out.println ("About: This is a program that will manage" +
                          "DataTurbine server connections.");
      System.exit (0);
    } if (cmd.hasOption ("d")) {
      app.debugging = true;
    } if (cmd.hasOption ("n")) {
      app.setName (cmd.getOptionValue ("n")); 
    } if (cmd.hasOption ("r")) {
      app.setServerName (cmd.getOptionValue ("r"));
    }
    
    /* End of command-line handling */
    //////////////////////////////////////////////////////////////////////////
    try {
      app.open ();
    } catch (SAPIException sae) {
      log.error ("Couldn't open the turbine " + sae);
    }
    
    app.putString ("DataTurbine.java_test", "_DataTurbine.java_test");
    if (app.debugging) System.out.println (app.getDebugString ());
  }
  
} // class

/* Copyright Notice:
*
* Copyright (c) 2005, NEES Cyberinfrastructure Center (NEESit), San Diego
Supercomputer Center
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
