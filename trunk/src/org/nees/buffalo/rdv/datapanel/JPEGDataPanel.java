/*
 * RDV
 * Real-time Data Viewer
 * http://nees.buffalo.edu/software/RDV/
 * 
 * Copyright (c) 2005 University at Buffalo
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

package org.nees.buffalo.rdv.datapanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.Extension;
import org.nees.buffalo.rdv.rbnb.Channel;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class JPEGDataPanel extends AbstractDataPanel {
	
	static Log log = LogFactory.getLog(JPEGDataPanel.class.getName());

	JPEGPanel image;
	JPanel panel;
				
 	boolean keepAspectRatio;
 	
 	double displayedImageTime;
 	 	
	public JPEGDataPanel() {
		super();
		
		keepAspectRatio = true;
		
		displayedImageTime = -1;
	
		initImage();

		setDataComponent(panel);
	}
			
	private void initImage() {
		panel = new JPanel();
		
		panel.setLayout(new BorderLayout());		

		image = new JPEGPanel();

		panel.add(image, BorderLayout.CENTER);	
	}
	
	public boolean supportsMultipleChannels() {
		return false;
	}
	
	public boolean setChannel(String channelName) {
		if (!isChannelSupported(channelName)) {
			return false;
		}
		
		if (super.setChannel(channelName)) {
			clearImage();
			return true;
		} else {
			return false;
		}		
	}
	
	private boolean isChannelSupported(String channelName) {
		Channel channel = rbnbController.getChannel(channelName);
		String mimeType = channel.getMimeType();
    
    //make broken apps work
    if (mimeType == null && channelName.endsWith(".jpg")) {
      return true;
    }
        
		Extension extension = dataPanelManager.getExtension(this.getClass());
		if (extension != null) {
			ArrayList mimeTypes = extension.getMimeTypes();
			for (int i=0; i<mimeTypes.size(); i++) {
				String mime = (String)mimeTypes.get(i);
        if (mime.equals(mimeType)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void postData(ChannelMap channelMap) {
		super.postData(channelMap);
	}

	public void postTime(double time) {
		super.postTime(time);
		
		if (channelMap == null) {
			//no data to display yet
			return;
		}
		
		Iterator it = channels.iterator();
		if (!it.hasNext()) {
			//no channels to post to
			return;
		}
			
		String channelName = (String)it.next();

		try {			
			int channelIndex = channelMap.GetIndex(channelName);
			
			//See if there is an data for us in the channel map
			if (channelIndex == -1) {
				return;
			}
			
			if (channelMap.GetType(channelIndex) != ChannelMap.TYPE_BYTEARRAY) {
				log.error("Expected byte array for JPEG data in channel " + channelName + ".");
				return;
			}
			
			int imageIndex = -1;
			
			// TODO replace with function in the Abstract class
			double[] times = channelMap.GetTimes(channelIndex);
			for (int i=times.length-1; i>=0; i--) {
				// TODO we could add a check for the duration as
				//      as a lower bound here
				if (times[i] <= time) {
					imageIndex = i;
					break;
				}
			}
			
			if (imageIndex == -1) {
				//no data in this time for us to display
				return;
			}
			
			double imageTime = times[imageIndex];
			if (imageTime == displayedImageTime) {
				//we are already displaying this image
				return;
			}
			
			byte[][] imageData = channelMap.GetDataAsByteArray(channelIndex);
			
			if (imageData.length > 0) {
				//draw image on screen
 				image.update(imageData[imageIndex]);
 				
				//update the image index currently displayed for this channel map
				displayedImageTime = imageTime;
			} else{
				log.error("Data array empty for channel " + channelName + ".");	 
			}

		} catch (Exception e) {
			log.error("Failed to receive data for channel " + channelName + ".");
			e.printStackTrace();
		}

	}
	
	private void clearImage() {
		image.clear();
		displayedImageTime = -1;
	}

	void clearData() {
		//TODO should we clear here?
		//clearImage();
	}
	
	public String toString() {
		return "JPEG Data Panel";
	}
	
	class JPEGPanel extends JComponent {
		private Image image;
		private VolatileImage volatileImage;
		
		private boolean newFrame;	
		
		public JPEGPanel() {
			super();
			
			image = null;
			volatileImage = null;

			newFrame = false;
		}
		
		private void createBackBuffer() {
		    if (volatileImage != null) {
				volatileImage.flush();
				volatileImage = null;
		    }
			volatileImage = createVolatileImage(image.getWidth(null), image.getHeight(null));
			
			copyFrame();		
		}
		
		private void copyFrame() {
			Graphics2D gVolatile = (Graphics2D)volatileImage.getGraphics();
			synchronized(this) {
				gVolatile.drawImage(image, 0, 0, null);
				newFrame = false;
			}
			gVolatile.dispose();
		}

		public final void paintComponent(Graphics g1) {
			Graphics2D g = (Graphics2D)g1;
			
			if (image == null) {
				g.setBackground(Color.BLACK);
				g.clearRect(0, 0, getWidth(), getHeight());
				return; 
			}
			
			if (volatileImage == null || newFrame) {
				createBackBuffer();
			}
			
			do {
				int valCode = volatileImage.validate(getGraphicsConfiguration());
				
				if (valCode == VolatileImage.IMAGE_RESTORED) {
					copyFrame();
				} else if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
					createBackBuffer();
				}
								
				// scale both width and height
				int componentWidth = getWidth();
				int componentHeight = getHeight();
				
				g.setBackground(Color.BLACK);
				g.clearRect(0, 0, componentWidth, componentHeight);
				
				int imageWidth = volatileImage.getWidth();
				int imageHeight = volatileImage.getHeight();

				float widthScale = componentWidth/(float)imageWidth;
				float heightScale = componentHeight/(float)imageHeight;
				if (keepAspectRatio && widthScale != heightScale) {
					widthScale = heightScale = Math.min(widthScale, heightScale);
				}
				int scaledWidth = (int)(imageWidth * widthScale);
				int scaledHeight = (int)(imageHeight * heightScale);
				int widthOffset = (componentWidth - scaledWidth)/2;
				int heightOffset = (componentHeight - scaledHeight)/2;
				AffineTransform af = new AffineTransform(widthScale, 0f, 0f, heightScale, widthOffset, heightOffset);
				g.drawImage(volatileImage, af, this);
			} while (volatileImage.contentsLost());						
		}
			
		public void update(byte[] imageData) {
			Image newImage = new ImageIcon(imageData).getImage();
			showImage(newImage);			
		}
		
		public void update(String imageFileName) {
			Image newImage = new ImageIcon(imageFileName).getImage();
			showImage(newImage);		
		}
		
		public void clear() {
			image = null;
			repaint();
		}
		
		private void showImage(Image newImage) {
			synchronized(this) {	
				if (image != null) {
					image.flush();
					image = null;
				}
				image = newImage;
				newFrame = true;
			}

			repaint();			
		}
		
		public Dimension getPreferredSize() {
			Dimension dimension;
			if (image != null) {
				dimension = new Dimension(image.getWidth(this), image.getHeight(this));
			} else {
				dimension = new Dimension(0, 0);
			}
					
			return dimension;
		}
		
		public Dimension getMinimumSize() {
			return new Dimension(0,0);
		}
		
		public Dimension getDisplayedImageSize() {
			float widthScale = getWidth()/(float)image.getWidth(null);
			float heightScale = getHeight()/(float)image.getHeight(null);
			if (keepAspectRatio && widthScale != heightScale) {
				widthScale = heightScale = Math.min(widthScale, heightScale);
			}
			int scaledWidth = (int)(volatileImage.getWidth() * widthScale);
			int scaledHeight = (int)(volatileImage.getHeight() * heightScale);

			return new Dimension(scaledWidth, scaledHeight);
		}
		
	}

}