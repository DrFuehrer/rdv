/*
 * Copyright (c) 2005 University at Buffalo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  o The above copyright notice and this permission notice shall be included
 *    in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.nees.buffalo.rdv.rbnb;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

/**
 * A class to fetch metadata from the server and post it to listeners. Methods
 * are also included to access metadata for individual channels.
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public class MetadataManager {
    
  static Log log = LogFactory.getLog(MetadataManager.class.getName());
    
  private RBNBController rbnbController;

  private ArrayList metadataListeners;
  private boolean updatingMetadata;
  
  private HashMap units;
  
  private ChannelTree ctree;
    
	public MetadataManager(RBNBController rbnbController) {
    this.rbnbController = rbnbController;

		metadataListeners =  new ArrayList();
    addMetadataListener(rbnbController);
        
		updatingMetadata = false;
		
		units = new HashMap();
		
		ctree = ChannelTree.EMPTY_TREE;
	}   
	
	public boolean updateMetadataBackground() {
		if (updatingMetadata) return true;
		
		new Thread(new Runnable() {
			public void run() {
				updateMetadata();
			}
		}, "MetadataManager").start();    
		
		return true;
	}
	
	public boolean updateMetadata() {
		if (updatingMetadata) return true;
		
		updatingMetadata = true;
		
		log.info("Updating channel listing.");
		
		Sink metadataSink = new Sink();
		try {
			metadataSink.OpenRBNBConnection(rbnbController.getRBNBConnectionString(), "RDVMetadata");
		} catch (SAPIException e) {
			log.error("Failed to connect to RBNB server.");
			metadataSink.CloseRBNBConnection();
			updatingMetadata = false;
			return false;   
		}
		
		try {
			metadataSink.RequestRegistration();
		} catch (SAPIException e) {
			log.error("Failed to request channel listing.");
			metadataSink.CloseRBNBConnection();
			updatingMetadata = false;
			return false;
		}
		
		ChannelMap cmap = null;
		try {
			cmap = metadataSink.Fetch(0);
		} catch (SAPIException e) {
			log.error("Failed to fetch list of available channels.");
			metadataSink.CloseRBNBConnection();
			updatingMetadata = false;
			return false;
		}
		
		log.info("Received list of available channels.");
        
        //let local channels update their metadata
		rbnbController.getLocalChannelManager().updateMetadata(cmap);
		
		//create metadata channel tree
		ctree = ChannelTree.createFromChannelMap(cmap);
		
		//subscribe to all units channels
		ChannelMap unitsChannelMap = new ChannelMap();
		try {
			unitsChannelMap.Add("_Units/*");
		} catch (SAPIException e) {
			e.printStackTrace();
		}
		
		//get the latest unit information
		try {
			metadataSink.Request(unitsChannelMap, 0, 0, "newest");
		} catch (SAPIException e) {
			e.printStackTrace();
		}
		
		//fetch the unit channel data
		try {
			unitsChannelMap = metadataSink.Fetch(-1);
		} catch (SAPIException e) {
			e.printStackTrace();
		}
		
		metadataSink.CloseRBNBConnection();
		
		String[] channels = unitsChannelMap.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			String channelName = channels[i];
			String parent = channelName.substring(channelName.lastIndexOf("/")+1);
			int channelIndex = unitsChannelMap.GetIndex(channelName);
			String[] data = unitsChannelMap.GetDataAsString(channelIndex);
			String newestData = data[data.length-1];
			String[] channelTokens = newestData.split("\t|,");
			for (int j=0; j<channelTokens.length; j++) {
				String[] tokens = channelTokens[j].split("=");
				if (tokens.length == 2) {
					String channel = parent + "/" + tokens[0].trim();
					String unit = tokens[1].trim();
					units.put(channel, unit);
                    log.info("Set unit (" + unit + ") for channel " + channel + "(the old way).");
				} else {
					log.error("Invalid unit string: " + channelTokens[j] + ".");
				}
			}
		}
        
        //get units from channel metadata
        for (int i=0; i<cmap.NumberOfChannels(); i++) {
          String userMetadata = cmap.GetUserInfo(i);
          if (userMetadata.length() > 0) {
            String channelName = cmap.GetName(i);
            log.info("User metadata for " + channelName + ": " + userMetadata + ".");
            String[] userMetadataTokens = userMetadata.split("\t|,");
            for (int j=0; j<userMetadataTokens.length; j++) {
              String[] tokens = userMetadataTokens[j].split("=");
              if (tokens.length == 2) {
                if (tokens[0].equals("units")) {
                  String unit = tokens[1].trim();
                  units.put(channelName, unit);
                  log.info("Set unit (" + unit + ") for channel " + channelName + ".");
                } else {
                  log.info("Received unknown user metadata element for channel " + channelName + ": " + userMetadataTokens[j]);
                }
              } else {
                log.warn("Invalid user metadata element: " + userMetadataTokens[j] + ".");
              }
            }            
          }
        }        

		fireMetadataUpdated(cmap);
		
		updatingMetadata = false;
		
		return true;
	}
  
  public ChannelTree getMetadataChannelTree() {
    return ctree;
  }
	
	public Channel getChannel(String channelName) {
		ChannelTree.Node node = ctree.findNode(channelName);
		if (node != null) {
			String mime = node.getMime();
			String unit = getUnit(channelName);
			Channel channel = new Channel(channelName, mime, unit);
			return channel;
		} else {
			return null;
		}
	}   
	
	public String getUnit(String channel) {
		return (String)units.get(channel);
	}   
	
	public void addMetadataListener(MetadataListener listener) {
		metadataListeners.add(listener);
	}
	
	public void removeMetadataListener(MetadataListener listener) {
		metadataListeners.remove(listener);
	}
	
	private void fireMetadataUpdated(ChannelMap channelMap) {
		MetadataListener listener;
		for (int i=0; i<metadataListeners.size(); i++) {
			listener = (MetadataListener)metadataListeners.get(i);
			listener.channelListUpdated(channelMap);
		}
	}
}
