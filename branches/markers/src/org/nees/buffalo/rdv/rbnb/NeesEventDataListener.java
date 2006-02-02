package org.nees.buffalo.rdv.rbnb;

/**
  * A class that gets @see org.nees.rbnb.marker.NeesEvent data from a channel whose name
  * is passed in an accessor call.
  * @author Lawrence J. Miller
  * @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Cen
  ter
  * @since 960110
  * @see org.nees.buffalo.rdv.datapanel.AbstractDataPanel
  * @see org.nees.buffalo.rdv.rbnb.RBNBController
  * Copyright (c) 2005, NEES Cyberinfrastructure Center (NEESit),
  * San Diego Supercomputer Center
  * All rights reserved. See full notice in the source, at the end of the file.
*/
import com.rbnb.sapi.ChannelMap;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.rbnb.marker.NeesEvent;

public class NeesEventDataListener implements DataListener {
  static Log log = LogFactory.getLog (NeesEventDataListener.class.getName ());
  private ChannelMap channelMap;

  public NeesEventDataListener () {
    super ();
    this.channelMap = null;
  }
  
  public void postData (ChannelMap cMap) {
    this.channelMap = cMap;
  }
  
/** A method to get @return NeesEvent data from a @param input rbnb data
  * channel. */
  public NeesEvent[] getEventData (String channelName) {
    int channelIndex;
    if (channelMap == null) {
      return null;
    }
    channelIndex = this.channelMap.GetIndex (channelName);
    /*log.debug ("Got Events channel \"" + channelName + "\" with index " +
               Integer.toString (channelIndex));*/
    String[] channelData = this.channelMap.GetDataAsString (channelIndex);
    NeesEvent[] eventData = new NeesEvent[channelData.length];
    for (int i=0; i<channelData.length; i++) {
      eventData[i] = new NeesEvent ();
      try {
        eventData[i].setFromEventXml (channelData[i]);
      } catch (TransformerException te) { 
        log.error ("Java XML Error\n" + te);
        te.printStackTrace ();
      } catch (InvalidPropertiesFormatException ipfe) {
        log.error ("Java XML Error\n" + ipfe);
        ipfe.printStackTrace ();
      } catch (IOException ioe) {
        log.error ("Java IO Error\n" + ioe);
        ioe.printStackTrace ();
      }
    } // for
      return eventData;
  } // getEventData ()
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
