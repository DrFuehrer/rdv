/*
 * Created on Jun 21, 2005
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
				} else {
					log.error("Invalid unit string: " + channelTokens[j] + ".");
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
