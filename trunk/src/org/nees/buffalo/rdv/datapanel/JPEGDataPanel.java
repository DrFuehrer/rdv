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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.VolatileImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;

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
  JCheckBoxMenuItem scaleMenuItem;
 	
 	double displayedImageTime;
  byte[] displayedImageData;
 	 	
	public JPEGDataPanel() {
		super();
		
		displayedImageTime = -1;
	
		initImage();

		setDataComponent(panel);
	}
			
	private void initImage() {
		panel = new JPanel();
		
		panel.setLayout(new BorderLayout());		

		image = new JPEGPanel();

		panel.add(image, BorderLayout.CENTER);
    
    JPopupMenu popupMenu = new JPopupMenu();
    
    // create a popup to copy an image to the clipboard
    final JMenuItem copyImageMenuItem = new JMenuItem("Copy");
    copyImageMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        copyImage();
      }
    });
    popupMenu.add(copyImageMenuItem);

    popupMenu.addSeparator();

    // create a popup to save an image
    final JMenuItem saveImageMenuItem = new JMenuItem("Save as...");
    saveImageMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        saveImage();
      }
    });
    popupMenu.add(saveImageMenuItem);    

    popupMenu.addSeparator();

    // create a popup to copy an image to the clipboard
    final JMenuItem printImageMenuItem = new JMenuItem("Print...");
    printImageMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        printImage();
      }
    });
    popupMenu.add(printImageMenuItem);

    popupMenu.addSeparator();

    scaleMenuItem = new  JCheckBoxMenuItem("Scale", true);
    scaleMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setScaled(scaleMenuItem.isSelected());
      }      
    });
    popupMenu.add(scaleMenuItem);

    // enable the save image popup if an image is being displayed
    popupMenu.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
        boolean enable = displayedImageData != null;
        saveImageMenuItem.setEnabled(enable);
        copyImageMenuItem.setEnabled(enable);
        printImageMenuItem.setEnabled(enable);
      }
      public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}
      public void popupMenuCanceled(PopupMenuEvent arg0) {}
    });
    
    // set component popup and mouselistener to trigger it
    panel.setComponentPopupMenu(popupMenu);
    panel.addMouseListener(new MouseInputAdapter() {});
	}
  
  /**
   * If an image is currently being displayed, save it to a file. This will
   * bring up a file chooser to select the file.
   */
  private void saveImage() {
    saveImage(null);
  }

  /**
   * If an image is currently being displayed, save it to a file. This will
   * bring up a file chooser to select the file.
   * 
   * @param directory the directory to start the file chooser in
   */
  private void saveImage(File directory) {
    if (displayedImageData != null) {
      JFileChooser chooser = new JFileChooser();
      
      // create default file name
      String channelName = (String)channels.iterator().next();
      String fileName = channelName.replace("/", " - ");
      if (!fileName.endsWith(".jpg")) {
        fileName += ".jpg";
      }
      chooser.setSelectedFile(new File(directory, fileName));
      
      // create filter to only show files that end with '.jpg'
      chooser.setFileFilter(new FileFilter() {
        public boolean accept(File f) {
          return (f.isDirectory() || f.getName().toLowerCase().endsWith(".jpg"));
        }
        public String getDescription() {
          return "JPEG Image Files";
        }
      });
      
      // show dialog
      int returnVal = chooser.showSaveDialog(null);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File outFile = chooser.getSelectedFile();
        
        // prompt for overwrite if file already exists
        if (outFile.exists()) {
          int overwriteReturn = JOptionPane.showConfirmDialog(null,
              outFile.getName() + " already exists. Do you want to replace it?",
              "Replace image?",
              JOptionPane.YES_NO_OPTION);
          if (overwriteReturn == JOptionPane.NO_OPTION) {
            saveImage(outFile.getParentFile());
            return;
          }
        }

        // write image file
        try {
          FileOutputStream out = new FileOutputStream(outFile);
          out.write(displayedImageData);
          out.close();
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null,
              "Filed to write image file.",
              "Save Image Error",
              JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }        
      }      
    }
  }

  /**
   * Copy the currently displayed image to the clipboard. If no image is being
   * displayed, nothing will be copied to the clipboard.
   */
  private void copyImage() {
    // get the displayed image
    Image displayedImage = image.getImage();
    if (displayedImage == null) {
      return;
    }
    
    // get the system clipboard
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    // create the transferable to transfer an image
    ImageSelection contents = new ImageSelection(displayedImage);
    
    // set the clipboard contents to the image transferable
    clipboard.setContents(contents, null);
  }  

  /**
   * Print the displayed image. If no image is being displayed, this will method
   * will do nothing.
   */
  private void printImage() {
    // get the displayed image
    final Image displayedImage = image.getImage();
    if (displayedImage == null) {
      return;
    }

    // setup a print job
    PrinterJob printJob = PrinterJob.getPrinterJob();
    
    // set the renderer for the image
    printJob.setPrintable(new Printable() {
      public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
        //we only have one page to print
        if (pageIndex != 0) {
          return Printable.NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D)g;
        
        // move to corner of imageable page
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        // get page dimensions
        double pageWidth = pageFormat.getImageableWidth();
        double pageHeight = pageFormat.getImageableHeight();
        
        // get image dimensions
        int imageWidth = displayedImage.getWidth(null);
        int imageHeight = displayedImage.getHeight(null);

        // get scale factor for image
        double widthScale = pageWidth/imageWidth;
        double heightScale = pageHeight/imageHeight;
        double scale = Math.min(widthScale, heightScale);
        
        // draw image with width and height scaled to page
        int scaledWidth = (int)(scale*imageWidth);
        int scaledHeight = (int)(scale*imageHeight);
        g2d.drawImage(displayedImage, 0, 0, scaledWidth, scaledHeight, null);
       
        return Printable.PAGE_EXISTS;
      }

    });
    
    // set the job name to the channel name (plus jpg extension)
    // this is used as a hint for a file name when printing to file
    String channelName = (String)channels.iterator().next();
    String jobName = channelName.replace("/", " - ");
    if (!jobName.endsWith(".jpg")) {
      jobName += ".jpg";
    }
    printJob.setJobName(jobName);
    
    // show the print dialog and print if ok clicked
    if (printJob.printDialog()) {
      try { 
        printJob.print();
      } catch(PrinterException pe) {
        JOptionPane.showMessageDialog(null,
            "Failed to print image.",
            "Print Image Error",
            JOptionPane.ERROR_MESSAGE);        
        pe.printStackTrace();
      }
    }
  }
  
  private void setScaled(boolean scale) {
    image.setScaled(scale);
    scaleMenuItem.setSelected(scale);
    properties.setProperty("scale", Boolean.toString(scale));
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
    if (channel == null) {
      return false;
    }
    
		String mimeType = channel.getMetadata("mime");
    
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
				// save raw image data
        displayedImageData = imageData[imageIndex];
        
				//draw image on screen
 				image.update(displayedImageData);
 				
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
  
  public void setProperty(String key, String value) {
    super.setProperty(key, value);
    
    if (key != null && key.equals("scale") && Boolean.parseBoolean(value) == false) {
      setScaled(false);
    }
  }  
	
	public String toString() {
		return "JPEG Data Panel";
	}
	
	class JPEGPanel extends JComponent {
		private Image image;
		private VolatileImage volatileImage;
		
		private boolean newFrame;

    /**
     * Controls scaling of the drawn image.
     */
    private boolean scale;

    /**
     * Controls the scaling of the drawn image by keep the ratio between the
     * width and height the same.
     */
    private boolean keepAspectRatio;

		public JPEGPanel() {
			super();
			
			image = null;
			volatileImage = null;

			newFrame = false;

      scale = true;
      keepAspectRatio = true;
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

        // get component dimensions
				int componentWidth = getWidth();
				int componentHeight = getHeight();
				
				g.setBackground(Color.BLACK);
				g.clearRect(0, 0, componentWidth, componentHeight);
				
        // get image dimensions
				int imageWidth = volatileImage.getWidth();
				int imageHeight = volatileImage.getHeight();

        // get dimensions to draw image
        int scaledWidth;
        int scaledHeight;
        if (scale) {
          float widthScale = componentWidth/(float)imageWidth;
          float heightScale = componentHeight/(float)imageHeight;
  				if (keepAspectRatio && widthScale != heightScale) {
  					widthScale = heightScale = Math.min(widthScale, heightScale);
  				}
          scaledWidth = (int)(imageWidth * widthScale);
          scaledHeight = (int)(imageHeight * heightScale);
        } else {
          scaledWidth = imageWidth;
          scaledHeight = imageHeight;
        }

        // calculate offsets to center image
				int widthOffset = (componentWidth - scaledWidth)/2;
				int heightOffset = (componentHeight - scaledHeight)/2;
        
        // draw image
				g.drawImage(volatileImage, widthOffset, heightOffset, scaledWidth, scaledHeight, null);
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
    
    public Image getImage() {
      return image;
    }
    
    /**
     * If set the image will be scaled to the container. If not the image will
     * be displayed in it's original size and may be clipped if too large.
     * <p>
     * The default is to scale the image.
     * 
     * @param scale if true, scale the image to the container
     */
    public void setScaled(boolean scale) {
      if (this.scale != scale) {
        this.scale = scale;
        repaint();
      }
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