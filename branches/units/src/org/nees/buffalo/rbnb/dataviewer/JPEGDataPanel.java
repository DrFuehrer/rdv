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
public class JPEGDataPanel implements DataPanel2, PlayerChannelListener, PlayerTimeListener, PlayerStateListener, DropTargetListener {
	
	static Log log = LogFactory.getLog(JPEGDataPanel.class.getName());

	String channelName;

	JPEGPanel image;
	JPanel panel;
		
	JFrame frame;
	boolean attached;
	
	JWindow window;
	boolean maximized;
	
	double frameRate;
	double lastTime;
	
	DataPanelContainer dataPanelContainer;
	Player player;

 	boolean keepAspectRatio;
 	boolean showFrameRate;
 	
 	static boolean iconsLoaded = false;
 	static Image windowPinImage;
 	static Image windowSnapshotImage;
 	static Image windowDetachImage;
 	static Image windowMaximizeImage;
 	static Image windowCloseImage;
 	
 	static String windowPinFileName = "icons/window_pin.gif";
 	static String windowSnapshotFileName = "icons/window_snapshot.gif";
 	static String windowDetachFileName = "icons/window_detach.gif";
 	static String windowMaximizeFileName = "icons/window_maximize.gif";
 	static String windowCloseFileName = "icons/window_close.gif";
 	
 	boolean hasFocus;
 	boolean pinned;
 	boolean paused;
 	
	public JPEGDataPanel(DataPanelContainer dataPanelContainer, Player player) {
		this.dataPanelContainer = dataPanelContainer;
		this.player = player;
		
		channelName = "";
		frameRate = -1;
		lastTime = 0;
		attached = true;
		maximized = false;
		keepAspectRatio = true;
		showFrameRate = false;
		hasFocus = false;
		pinned = false;
		paused = false;
		
		if (!iconsLoaded) {
			loadIcons();
		}
	
		initImage();
		
		player.addStateListener(this);
		player.addTimeListener(this);
		
		new DropTarget(panel, DnDConstants.ACTION_LINK, this);
	}
	
	private static void loadIcons() {
		windowPinImage = new ImageIcon(windowPinFileName).getImage();
		windowSnapshotImage = new ImageIcon(windowSnapshotFileName).getImage();
		windowDetachImage = new ImageIcon(windowDetachFileName).getImage();
		windowMaximizeImage = new ImageIcon(windowMaximizeFileName).getImage();
		windowCloseImage = new ImageIcon(windowCloseFileName).getImage();
		
		iconsLoaded = true;
	}
		
	private void initImage() {
		panel = new JPanel();
		
		panel.setBorder(new EtchedBorder());

		panel.setLayout(new BorderLayout());		

		image = new JPEGPanel();
		image.addMouseListener(new MouseInputAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
 					int x = e.getX();
 					int y = e.getY();
 
 					Dimension imageDimensions = image.getDisplayedImageSize();
 					int imageWidth = imageDimensions.width;
 					int imageHeight = imageDimensions.height;
 					
 					int componentWidth = image.getWidth();
 					int componentHeight = image.getHeight();
 					
 					int widthOffset = (componentWidth - imageWidth)/2;
 					int heightOffset = (componentHeight - imageHeight)/2;
 					
 					if (x < 16 && y < 16) {
 						pinned = !pinned;
 					} else if (x >= 16 && x < 32 && y < 16) {
 						togglePause();
 					} else if (x >= componentWidth-16 && y < 16) {
						closePanel();
 					} else if (x <  componentWidth-16 && x >= componentWidth-32 && y < 16) {
						toggleMaximize();
					} else if (x <  componentWidth-32 && x >= componentWidth-48 && y < 16) {
						toggleDetach(); 												
					}
				}
			}
			
			public void mouseEntered(MouseEvent e) {
				hasFocus = true;
				image.repaint();
			}
			
			public void mouseExited(MouseEvent e) {
				hasFocus = false;
				image.repaint();
			}
		});		
		panel.add(image, BorderLayout.CENTER);	
	}
	
	public JComponent getComponent() {
		return panel;
	}
	
	public void setChannel(String channelName, String unit) {
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
		
	public void postData(ChannelMap channelMap) {
		postData(channelMap, -1, -1);
	}

	public void postData(ChannelMap channelMap, double startTime, double duration) {
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
				log.error("Data array empty for channel " + channelName + ".");	 
			}

		} catch (Exception e) {
			log.error("Failed to receive data for channel " + channelName + ".");
			e.printStackTrace();
		}

	}
	
	public void togglePause() {
		if (paused) {
			player.subscribe(channelName, this);
		} else {
			player.unsubscribe(channelName, this);
		}
		
		paused = !paused;
	}
	
	public void closePanel() {
 		if (!channelName.equals("")) {
			player.unsubscribe(channelName, this);
		}

 		if (maximized) {
 			restorePanel(false);
		} else if (!attached) {
			attachPanel(false);		 			
 		} else if (attached) {
			dataPanelContainer.removeDataPanel(this);
 		}
 		
		player.removeStateListener(this);
		player.removeTimeListener(this);
	}
	
	public void toggleDetach() {
		if (maximized) {
			window.setVisible(false);
			window.getContentPane().remove(panel);
			window.dispose();
			window = null;
			maximized = false;
		}
		
		if (attached) {
			detachPanel();
		} else {
			attachPanel(true);
		}
	}
	
	public void detachPanel() {
		attached = false;
		dataPanelContainer.removeDataPanel(this);
		
		frame = new JFrame(channelName);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closePanel();
			}
		});
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		

		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void attachPanel(boolean addToContainer) {
		if (frame != null) {
			frame.setVisible(false);
			frame.getContentPane().remove(panel);
			frame.dispose();
			frame = null;
		}
		
		if (addToContainer) {
			attached = true;
			dataPanelContainer.addDataPanel(this);
		}
	}
	
	public void toggleMaximize() {	
		if (maximized) {
			restorePanel(attached);
			if (!attached) {
				detachPanel();
			}
		} else {
			if (!attached) {
				attachPanel(false);
			}
			maximizePanel();
		}
	}
	
	public void maximizePanel() {
		maximized = true;
		dataPanelContainer.removeDataPanel(this);
		
		window = new JWindow();
		window.getContentPane().add(panel);
		window.setSize(getScreenDimensions());
		window.setVisible(true);
	}
	
	public void restorePanel(boolean addToContainer) {
		window.setVisible(false);
		window.getContentPane().remove(panel);
		window.dispose();
		window = null;

		maximized = false;
		
		if (addToContainer) {
			dataPanelContainer.addDataPanel(this);
		}
		
	}
	
	private Dimension getScreenDimensions() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		    
		for (int i=0; i<gs.length; i++) {
			DisplayMode dm = gs[i].getDisplayMode();
			int screenWidth = dm.getWidth();
			int screenHeight = dm.getHeight();
			return new Dimension(screenWidth, screenHeight);
		}
		
		return null;
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
				if (pinned || hasFocus) {
					g.setColor(Color.BLACK);
					g.fillRect(0, 0, getWidth(), 16);
					g.drawImage(windowPinImage, 0, 0, this);
					g.drawImage(windowSnapshotImage, 16, 0, this);
					g.drawImage(windowDetachImage, componentWidth-48, 0, this);
					g.drawImage(windowMaximizeImage, componentWidth-32, 0, this);
					g.drawImage(windowCloseImage, componentWidth-16, 0, this);
					g.setColor(Color.WHITE);
					g.drawString(channelName, 36, 12);
				}
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

	public String[] getSupportedMimeTypes() {
		return new String[] {"image/jpeg", "video/jpeg"};
	}

	public boolean supportsMultipleChannels() {
		return false;
	}

	public void addChannel(String channelName, String unit) {}

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
						addChannel(channelName, null);
					} else {
						setChannel(channelName, null);
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
