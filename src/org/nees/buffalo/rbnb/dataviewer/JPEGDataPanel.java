package org.nees.buffalo.rbnb.dataviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
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
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * @author Jason P. Hanley
 */
public class JPEGDataPanel implements DataPanel2, PlayerChannelListener, PlayerTimeListener, PlayerStateListener, DropTargetListener {
	
	static Log log = LogFactory.getLog(JPEGDataPanel.class.getName());

	String channelName;

	JPEGPanel image;
	JPanel panel;
		
	JFrame frame;
	boolean attached;
	
	double frameRate;
	double lastTime;
	
	DataPanelContainer dataPanelContainer;
	Player player;

 	Color textColor = Color.WHITE;
 	
 	boolean keepAspectRatio;
 	boolean showFrameRate;
 	
	public JPEGDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		this.dataPanelContainer = dataPanelContainer;
		this.player = player;
		
		channelName = "";
		frameRate = -1;
		lastTime = 0;
		attached = true;
		keepAspectRatio = true;
		showFrameRate = false;
	
		initImage();
		
		player.addStateListener(this);
		player.addTimeListener(this);
		
		new DropTarget(panel, DnDConstants.ACTION_LINK, this);
	}
		
	private void initImage() {
		panel = new JPanel();
		
		panel.setBorder(new EtchedBorder());

		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();		

		image = new JPEGPanel();
		image.update("images/ub.gif");
		image.addMouseListener(new MouseInputAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
 					int x = e.getX();
 					int y = e.getY();
 					int imageWidth = image.getWidth();
 					int imageHeight = image.getHeight();
 					if (x <= 100 && y <= 25) {
 						toggleTextColor();
 					} else if (x >= imageWidth-10 && y <= 12) {
						closePanel();
					} else {
						toggleDetach();
					}
				}
			}
		});		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(0,0,0,0);
		c.anchor = GridBagConstraints.CENTER;		
		panel.add(image, c);	
	}
	
	public JComponent getComponent() {
		return panel;
	}
	
	public void setChannel(String channelName) {
		if (this.channelName.equals(channelName)) {
			return;
		} else if (this.channelName.equals("")) {
			player.subscribe(channelName, this);
		} else {
			player.unsubscribeAndSubscribe(this.channelName, channelName, this);
		}
		
		this.channelName = channelName;
		
		if (!attached) {
			frame.setTitle(channelName);
		}
	}

	public void setDomain(double domain) {}

	public void postTime(double time) {}

	public void postState(int newState, int oldState) {
		switch (newState) {
			case Player.STATE_STOPPED:
			case Player.STATE_LOADING:
			case Player.STATE_MONITORING:				
				frameRate = -1;
				lastTime = 0;
				break;
		}
	}	
		
	public void postData(ChannelMap channelMap, int channelIndex, String channelName) {
		postData(channelMap, channelIndex, channelName, -1, -1);
	}

	public void postData(ChannelMap channelMap, int channelIndex, String channelName, double startTime, double duration) {
		try {					
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
				double currentTime = System.currentTimeMillis()/1000d;
				double difference = currentTime-lastTime;
				if (lastTime != 0 && difference > 0.001) {
					if (frameRate == -1) {
						frameRate = 0;
					}
					frameRate = 0.95*frameRate + 0.05/(difference);
				}

				lastTime = currentTime;

 				image.update(imageData[imageIndex]);
			} else{
				log.error("Data array empty for channel: " + channelName + ".");	 
			}

		} catch (Exception e) {
			log.error("Failed to receive data for channel: " + channelName + " (" + channelIndex + ")");
			e.printStackTrace();
		}

	}
	
	public void closePanel() {
 		if (!channelName.equals("")) {
			player.unsubscribe(channelName, this);
		}

		if (attached) {
			dataPanelContainer.removeDataPanel(this);
		} else {
			frame.setVisible(false);
			frame.getContentPane().remove(panel);
			frame.dispose();
			frame = null;			
		}
		
		player.removeStateListener(this);
		player.removeTimeListener(this);
	}
	
	public void toggleDetach() {
		if (attached) {
			detachPanel();
		} else {
			attachPanel();
		}
	}
	
	public void detachPanel() {
		attached = false;
		dataPanelContainer.removeDataPanel(this);
		
		frame = new JFrame(channelName);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				attachPanel();
			}
		});
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void attachPanel() {
		frame.setVisible(false);
		frame.getContentPane().remove(panel);
		frame.dispose();
		frame = null;			

		dataPanelContainer.addDataPanel(this);
		attached = true;	
	}
	
 	private void toggleTextColor() {
 		if (textColor == Color.WHITE) {
 			textColor = Color.BLACK;
 		} else {
 			textColor = Color.WHITE;
 		}
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
			
			if (image == null) return; 
			
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
				float widthScale = getWidth()/(float)volatileImage.getWidth();
				float heightScale = getHeight()/(float)volatileImage.getHeight();
				if (keepAspectRatio && widthScale != heightScale) {
					widthScale = heightScale = Math.min(widthScale, heightScale);
				}
				int scaledWidth = (int)(volatileImage.getWidth() * widthScale);
				int scaledHeight = (int)(volatileImage.getHeight() * heightScale);
				float widthOffset = (getWidth() - scaledWidth)/2f;
				float heightOffset = (getHeight() - scaledHeight)/2f;
				AffineTransform af = new AffineTransform(widthScale, 0f, 0f, heightScale, widthOffset, heightOffset);
				g.drawImage(volatileImage, af, this);
				g.setColor(textColor);
				g.drawString("X", getWidth()-widthOffset-10, heightOffset+12);
				g.drawString(channelName, widthOffset+2, heightOffset+12);
				if (showFrameRate && frameRate != -1) {
					g.drawString(Double.toString(Math.round(frameRate*10d)/10d) + " fps", widthOffset+2, heightOffset+26);
				}				
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
				dimension = new Dimension(image.getWidth(this), image.getWidth(this));
			} else {
				dimension = new Dimension(0, 0);
			}
					
			return dimension;
		}
		
		public Dimension getMinimumSize() {
			return new Dimension(0,0);
		}
		
	}

	public String[] getSupportedMimeTypes() {
		return new String[] {"image/jpeg", "video/jpeg"};
	}

	public boolean supportsMultipleChannels() {
		return false;
	}

	public void addChannel(String channelName) {}

	public void removeChannel(String channelName) {}

	public void dragEnter(DropTargetDragEvent e) {}
	
	public void dragOver(DropTargetDragEvent e) {}
	
	public void dropActionChanged(DropTargetDragEvent e) {}
	
	public void drop(DropTargetDropEvent e) {
		try {
			DataFlavor stringFlavor = DataFlavor.stringFlavor;
			Transferable tr = e.getTransferable();
			if(e.isDataFlavorSupported(stringFlavor)) {
				String channelName = (String)tr.getTransferData(stringFlavor);
				e.acceptDrop(DnDConstants.ACTION_LINK);
				e.dropComplete(true);
				
				try {
					if (supportsMultipleChannels()) {
						addChannel(channelName);
					} else {
						setChannel(channelName);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				e.rejectDrop();
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
		}	
	}
	
	public void dragExit(DropTargetEvent e) {}
	
}
