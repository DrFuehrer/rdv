package org.nees.buffalo.rbnb.dataviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.EtchedBorder;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class JPEGDataPanel extends AbstractDataPanel {
	
	static Log log = LogFactory.getLog(JPEGDataPanel.class.getName());

	JPEGPanel image;
	JPanel panel;
				
 	boolean keepAspectRatio;
 	 	
	public JPEGDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		super(dataPanelContainer, player);
		
		keepAspectRatio = true;
	
		initImage();

		setDataComponent(panel);
		setControlBar(true);
		setDropTarget(true);
	}
			
	private void initImage() {
		panel = new JPanel();
		
		panel.setLayout(new BorderLayout());		

		image = new JPEGPanel();

		panel.add(image, BorderLayout.CENTER);	
	}
	
	public String[] getSupportedMimeTypes() {
		return new String[] {"image/jpeg", "video/jpeg"};
	}

	public boolean supportsMultipleChannels() {
		return false;
	}	
	
	public void postData(ChannelMap channelMap) {
		postData(channelMap, -1, -1);
	}

	public void postData(ChannelMap channelMap, double startTime, double duration) {
		String channelName = (String)channels.iterator().next();

		try {			
			int channelIndex = channelMap.GetIndex(channelName);
			
			//See if there is an data for us in the channel map
			if (channelIndex == -1) {
				log.error("Got channel map with no data for us (looking for channel " + channelName + ". This shouldn't happen.");
				return;
			}
			
			if (channelMap.GetType(channelIndex) != ChannelMap.TYPE_BYTEARRAY) {
				log.error("Expected byte array for JPEG data in channel " + channelName + ".");
				return;
			}
			
			int imageIndex = -1;
			
			if (startTime != -1 && duration != -1) {
				double[] times = channelMap.GetTimes(channelIndex);
				double endTime = startTime + duration;
				for (int i=0; i<times.length; i++) {
					if (times[i] >= startTime && times[i] < endTime) {
						imageIndex = i;
						break;
					}
				}
				
				if (imageIndex == -1) {
					return;
				}
			} else {
				imageIndex = 0;
			}
			
			byte[][] imageData = channelMap.GetDataAsByteArray(channelIndex);
			
			if (imageData.length > 0) {
 				image.update(imageData[imageIndex]);
			} else{
				log.error("Data array empty for channel " + channelName + ".");	 
			}

		} catch (Exception e) {
			log.error("Failed to receive data for channel " + channelName + ".");
			e.printStackTrace();
		}

	}	

	void clearData() {
		// TODO maybe implement this
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