/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
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

package org.rdv.viz.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataPanelManager;
import org.rdv.DataViewer;
import org.rdv.Extension;
import org.rdv.auth.Authentication;
import org.rdv.auth.AuthenticationEvent;
import org.rdv.auth.AuthenticationListener;
import org.rdv.auth.AuthenticationManager;
import org.rdv.datapanel.AbstractDataPanel;
import org.rdv.rbnb.Channel;
import org.rdv.rbnb.Player;

import com.rbnb.sapi.ChannelMap;

/**
 * A visualization extension for viewing images.
 * 
 * @author Jason P. Hanley
 */
public class ImageViz extends AbstractDataPanel implements AuthenticationListener {
  
  /** the logger for this class */
  static Log log = LogFactory.getLog(ImageViz.class.getName());
  
  /** the data panel property for the scale */
  private static final String DATA_PANEL_PROPERTY_SCALE = "scale";
  
  /** the data panel property for the navigation panel visibility */
  private static final String DATA_PANEL_PROPERTY_SHOW_NAVIGATION_IMAGE = "showNavigationImage";
  
  /** the data panel property for the robotic controls visibility */
  private static final String DATA_PANEL_PROPERTY_HIDE_ROBOTIC_CONTROLS = "hideRoboticControls";

  ImagePanel image;
  JPanel panel;
  JPanel topControls;
  JPanel irisControls;
  JPanel focusControls;  
  JPanel zoomControls;
  JPanel tiltControls;
  JPanel panControls;
  JCheckBoxMenuItem autoScaleMenuItem;
  JCheckBoxMenuItem showNavigationImageMenuItem;
  JCheckBoxMenuItem hideRoboticControlsMenuItem;
  
  MouseInputAdapter clickMouseListener;
 	
 	double displayedImageTime;
  byte[] displayedImageData;
  
  FlexTPSStream flexTPSStream;
 	 	
	public ImageViz() {
		super();
		
		displayedImageTime = -1;
	
		initUI();

		setDataComponent(panel);
	}
  
  public void openPanel(final DataPanelManager dataPanelManager) {
    super.openPanel(dataPanelManager);
    
    AuthenticationManager.getInstance().addAuthenticationListener(this);
  }
  
  public void closePanel() {
    super.closePanel();
    
    AuthenticationManager.getInstance().removeAuthenticationListener(this);
  }
  
  private void initUI() {
    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    
    initImage();
    
    topControls = new JPanel();
    topControls.setLayout(new BorderLayout());
    panel.add(topControls, BorderLayout.NORTH);
    
    initIrisControls();   
    initFocusControls();
    initZoomControls();
    initTiltControls();
    initPanControls();

    initPopupMenu();
  }
			
	private void initImage() {
		image = new ImagePanel();
    image.setBackground(Color.black);
    image.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) {
        String propertyName = pce.getPropertyName();
        if (propertyName.equals(ImagePanel.AUTO_SCALING_PROPERTY)) {
          boolean autoScaling = (Boolean)pce.getNewValue();
          autoScaleMenuItem.setSelected(autoScaling);
          if (autoScaling) {
            properties.remove(DATA_PANEL_PROPERTY_SCALE);
          }
        } else if (propertyName.equals(ImagePanel.SCALE_PROPERTY) && !image.isAutoScaling()) {
          properties.setProperty(DATA_PANEL_PROPERTY_SCALE, pce.getNewValue().toString());
        } else if (propertyName.equals(ImagePanel.NAVIGATION_IMAGE_ENABLED_PROPERTY)) {
          boolean showNavigationImage = (Boolean)pce.getNewValue();
          showNavigationImageMenuItem.setSelected(showNavigationImage);
          if (showNavigationImage) {
            properties.setProperty(DATA_PANEL_PROPERTY_SHOW_NAVIGATION_IMAGE, "true");
          } else {
            properties.remove(DATA_PANEL_PROPERTY_SHOW_NAVIGATION_IMAGE);
          }
        }
      }
    });
    
    clickMouseListener = new MouseInputAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          Point imagePoint = image.panelToScaledImagePoint(e.getPoint());
          if (imagePoint != null) {
            center(imagePoint);
          }
        }
      }   
    };
	}
  
  private void initIrisControls() {
    irisControls = new JPanel();
    irisControls.setLayout(new BorderLayout());
    
    JButton closeIrisButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-close.gif"));
    closeIrisButton.setBorder(null);
    closeIrisButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        irisClose();
      }
    });
    irisControls.add(closeIrisButton, BorderLayout.WEST);
    
    JButton irisControlButton = new StrechIconButton(DataViewer.getIcon("icons/ptz/robotic_control-irisbar-medium.gif"));
    irisControlButton.setBorder(null);
    irisControlButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        JButton button = (JButton)e.getComponent();
        int iris = Math.round(100f*e.getPoint().x/button.getWidth());
        iris(iris);
      }
    });    
    irisControls.add(irisControlButton, BorderLayout.CENTER);
    
    JPanel irisControlsRight = new JPanel();
    irisControlsRight.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    
    JButton openIrisButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-open.gif"));
    openIrisButton.setBorder(null);
    openIrisButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        irisOpen();
      }
    });
    irisControlsRight.add(openIrisButton);
    
    JButton autoIrisButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-auto.gif"));
    autoIrisButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        irisAuto();
      }
    });
    autoIrisButton.setBorder(null);
    irisControlsRight.add(autoIrisButton);
    
    irisControls.add(irisControlsRight, BorderLayout.EAST);
  }
  
  private void initFocusControls() {
    focusControls = new JPanel();
    focusControls.setLayout(new BorderLayout());
    
    JButton nearFocusButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-near.gif"));
    nearFocusButton.setBorder(null);
    nearFocusButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        focusNear(); 
      }
    });
    focusControls.add(nearFocusButton, BorderLayout.WEST);
    
    JButton focusControlButton = new StrechIconButton(DataViewer.getIcon("icons/ptz/robotic_control-focusbar-medium.gif"));
    focusControlButton.setBorder(null);
    focusControlButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        JButton button = (JButton)e.getComponent();
        int focus = Math.round(100f*e.getPoint().x/button.getWidth());
        focus(focus);
      }
    });

    focusControls.add(focusControlButton, BorderLayout.CENTER);
    
    JPanel focusControlsRight = new JPanel();
    focusControlsRight.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

    JButton farFocusButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-far.gif"));
    farFocusButton.setBorder(null);
    farFocusButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        focusFar();
      }
    });
    focusControlsRight.add(farFocusButton);    

    JButton autoFocusButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-auto.gif"));
    autoFocusButton.setBorder(null);
    autoFocusButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        focusAuto();
      }
    });
    focusControlsRight.add(autoFocusButton);
    
    focusControls.add(focusControlsRight, BorderLayout.EAST);
  }
  
  private void initZoomControls() {
    zoomControls = new JPanel();
    zoomControls.setLayout(new BorderLayout());
    
    JButton zoomOutButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-minus.gif"));
    zoomOutButton.setBorder(null);
    zoomOutButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        zoomOut();
      }
    });    
    zoomControls.add(zoomOutButton, BorderLayout.WEST);
        
    JButton zoomControlButton = new StrechIconButton(DataViewer.getIcon("icons/ptz/robotic_control-zoombar-medium.gif"));
    zoomControlButton.setBorder(null);
    zoomControlButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        JButton button = (JButton)e.getComponent();
        int zoom = Math.round(100f*e.getPoint().x/button.getWidth());
        zoom(zoom);
      }
    });
    zoomControls.add(zoomControlButton, BorderLayout.CENTER);

    JPanel topRightControls = new JPanel();
    topRightControls.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));        
    
    JButton zoomInButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-plus.gif"));
    zoomInButton.setBorder(null);
    zoomInButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        zoomIn();
      }
    });    
    topRightControls.add(zoomInButton);
    
    JButton topHomeButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-home.gif"));
    topHomeButton.setBorder(null);
    topHomeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        home();
      }
    });
    topRightControls.add(topHomeButton);    
    
    zoomControls.add(topRightControls, BorderLayout.EAST);    
  }
  
  private void initTiltControls() {
    tiltControls = new JPanel();
    tiltControls.setLayout(new BorderLayout());
    
    JButton tiltUpButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-up.gif"));
    tiltUpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        tiltUp();
      }      
    });
    tiltUpButton.setBorder(null);
    tiltControls.add(tiltUpButton, BorderLayout.NORTH);
    
    JButton tiltControlButton = new StrechIconButton(DataViewer.getIcon("icons/ptz/robotic_control-tiltbar-medium.gif"));
    tiltControlButton.setBorder(null);
    tiltControlButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        JButton button = (JButton)e.getComponent();
        int tilt = Math.round(100f*e.getPoint().y/button.getHeight());
        tilt(tilt);
      }
    });    
    tiltControls.add(tiltControlButton, BorderLayout.CENTER);
    
    JButton tiltDownButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-down.gif"));
    tiltDownButton.setBorder(null);
    tiltDownButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        tiltDown();
      }
    });
    tiltControls.add(tiltDownButton, BorderLayout.SOUTH);    
  }
  
  private void initPanControls() {
    panel.add(image, BorderLayout.CENTER);
    
    panControls = new JPanel();
    panControls.setLayout(new BorderLayout());
    
    JButton panLeftButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-left.gif"));
    panLeftButton.setBorder(null);
    panLeftButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        panLeft();
      }
    });
    panControls.add(panLeftButton, BorderLayout.WEST);
    
    JButton panControlButton = new StrechIconButton(DataViewer.getIcon("icons/ptz/robotic_control-panbar-medium.gif"));
    panControlButton.setBorder(null);
    panControlButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        JButton button = (JButton)e.getComponent();
        int pan = Math.round(100f*e.getPoint().x/button.getWidth());
        pan(pan);
      }
    });    
    panControls.add(panControlButton, BorderLayout.CENTER);
    
    JPanel bottomRightControls = new JPanel();
    bottomRightControls.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    
    JButton panRightButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-right.gif"));
    panRightButton.setBorder(null);
    panRightButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        panRight();
      }
    });    
    bottomRightControls.add(panRightButton);
    
    JButton bottomHomeButton = new JButton(DataViewer.getIcon("icons/ptz/robotic_control-home.gif"));
    bottomHomeButton.setBorder(null);
    bottomHomeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        home();
      }
    });    
    bottomRightControls.add(bottomHomeButton);    
    
    panControls.add(bottomRightControls, BorderLayout.EAST);    
  }
  
  private void initPopupMenu() {
    final JPopupMenu popupMenu = new JPopupMenu();
    
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
    
    final JMenuItem zoomInMenuItem = new JMenuItem("Zoom in");
    zoomInMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        image.zoomIn();
      }
    });
    popupMenu.add(zoomInMenuItem);
    
    final JMenuItem zoomOutMenuItem = new JMenuItem("Zoom out");
    zoomOutMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        image.zoomOut();
      }
    });
    popupMenu.add(zoomOutMenuItem);

    popupMenu.addSeparator();

    autoScaleMenuItem = new  JCheckBoxMenuItem("Auto scale", true);
    autoScaleMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setAutoScale(autoScaleMenuItem.isSelected());
      }      
    });
    popupMenu.add(autoScaleMenuItem);
    
    JMenuItem resetScaleMenuItem = new JMenuItem("Reset scale");
    resetScaleMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        image.setScale(1);
      }
    });
    popupMenu.add(resetScaleMenuItem);
    
    showNavigationImageMenuItem = new JCheckBoxMenuItem("Show navigation image", image.isNavigationImageEnabled());
    showNavigationImageMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        setShowNavigationImage(showNavigationImageMenuItem.isSelected());
      }
    });
    popupMenu.add(showNavigationImageMenuItem);
    
    hideRoboticControlsMenuItem = new JCheckBoxMenuItem("Disable robotic controls", false);
    hideRoboticControlsMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setRoboticControls();
      }
    });

    // enable the save image popup if an image is being displayed
    popupMenu.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
        boolean enable = displayedImageData != null;
        saveImageMenuItem.setEnabled(enable);
        copyImageMenuItem.setEnabled(enable);
        printImageMenuItem.setEnabled(enable);
        zoomInMenuItem.setEnabled(enable);
        zoomOutMenuItem.setEnabled(enable);
        
        if (flexTPSStream != null) {
          popupMenu.add(hideRoboticControlsMenuItem);
        } else {
          popupMenu.remove(hideRoboticControlsMenuItem);
        }
      }
      public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}
      public void popupMenuCanceled(PopupMenuEvent arg0) {}
    });
    
    // set component popup and mouselistener to trigger it
    panel.setComponentPopupMenu(popupMenu);
    image.setComponentPopupMenu(popupMenu);
    panel.addMouseListener(new MouseInputAdapter() {});    
  }
  
  private void addRoboticControls() {
    if (flexTPSStream == null) {
      return;
    }
    
    if (flexTPSStream.canIris()) {
      topControls.add(irisControls, BorderLayout.NORTH);
    } else {
      topControls.remove(irisControls);
    }
    
    if (flexTPSStream.canFocus()) {
      topControls.add(focusControls, BorderLayout.CENTER);
    } else {
      topControls.remove(focusControls);
    }
    
    if (flexTPSStream.canZoom()) {
      topControls.add(zoomControls, BorderLayout.SOUTH);  
    } else {
      topControls.remove(zoomControls);
    }
    
    if (flexTPSStream.canTilt() && flexTPSStream.canPan()) {
      panel.add(tiltControls, BorderLayout.EAST);
      panel.add(panControls, BorderLayout.SOUTH);
    } else {
      panel.remove(tiltControls);
      panel.remove(panControls);      
    }
    
    panel.revalidate();
    
    image.removeMouseListener(clickMouseListener);
    image.addMouseListener(clickMouseListener);
  }
  
  private void removeRoboticControls() {
    topControls.remove(irisControls);
    topControls.remove(focusControls);
    topControls.remove(zoomControls);
    panel.remove(tiltControls);
    panel.remove(panControls);
    panel.revalidate();
    
    image.removeMouseListener(clickMouseListener);
  }
  
  private void setRoboticControls() {
    if (hideRoboticControlsMenuItem.isSelected()) {
      properties.setProperty(DATA_PANEL_PROPERTY_HIDE_ROBOTIC_CONTROLS, "true");
      removeRoboticControls();      
    } else {
      properties.remove(DATA_PANEL_PROPERTY_HIDE_ROBOTIC_CONTROLS);
      if (state == Player.STATE_MONITORING) {
        addRoboticControls();
      } else {
        removeRoboticControls();
      }      
    }
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
  
  private void setShowNavigationImage(boolean showNavigation) {
    image.setNavigationImageEnabled(showNavigation);
  }
  
  private void setAutoScale(boolean autoScale) {
    image.setAutoScaling(autoScale);
  }

  private void panLeft() {
    if (flexTPSStream != null) {
      flexTPSStream.panLeft();
    }
  }
  
  private void panRight() {
    if (flexTPSStream != null) {
      flexTPSStream.panRight();
    }    
  }
  
  private void pan(int pan) {
    if (flexTPSStream != null) {
      flexTPSStream.pan(pan);
    }
  }

  private void tiltUp() {
    if (flexTPSStream != null) {
      flexTPSStream.tiltUp();
    }    
  }
  
  private void tiltDown() {
    if (flexTPSStream != null) {
      flexTPSStream.tiltDown();
    }        
  }
  
  private void tilt(int tilt) {
    if (flexTPSStream != null) {
      flexTPSStream.tilt(tilt);
    }
  }
  
  private void home() {
    if (flexTPSStream != null) {
      flexTPSStream.goHome();
    }
  }
  
  private void center(Point p) {
    if (flexTPSStream != null) {
      flexTPSStream.center(p, image.getScaledImageSize());
    }
  }
  
  private void zoomIn() {
    if (flexTPSStream != null) {
      flexTPSStream.zoomIn();
    }    
  }
  
  private void zoomOut() {
    if (flexTPSStream != null) {
      flexTPSStream.zoomOut();
    }        
  }
  
  private void zoom(int zoom) {
    if (flexTPSStream != null) {
      flexTPSStream.zoom(zoom);
    }
  }
  
  private void irisClose() {
    if (flexTPSStream != null) {
      flexTPSStream.irisClose();
    }
  }
  
  private void irisOpen() {
    if (flexTPSStream != null) {
      flexTPSStream.irisOpen();
    }  
  }
  
  private void iris(int iris) {
    if (flexTPSStream != null) {
      flexTPSStream.iris(iris);
    }
  }
  
  private void irisAuto() {
    if (flexTPSStream != null) {
      flexTPSStream.irisAuto();
    }    
  }
  
  private void focusNear() {
    if (flexTPSStream != null) {
      flexTPSStream.focusNear();
    }
  }
  
  private void focusFar() {
    if (flexTPSStream != null) {
      flexTPSStream.focusFar();
    }    
  }
  
  private void focus(int focus) {
    if (flexTPSStream != null) {
      flexTPSStream.focus(focus);
    }    
  }
  
  private void focusAuto() {
    if (flexTPSStream != null) {
      flexTPSStream.focusAuto();
    }
  }
  
  private void setupFlexTPSStream() {
    String channelName = (String)channels.iterator().next();

    Channel channel = rbnbController.getChannel(channelName);
    if (channel == null) {
      return;
    }
    
    String host = channel.getMetadata("flexTPS_host");
    String feed = channel.getMetadata("flexTPS_feed");
    String stream = channel.getMetadata("flexTPS_stream");

    // If user successfully login, gaSession should be valid
    Authentication auth = AuthenticationManager.getInstance().getAuthentication();
    String gaSession = null;
    if (auth != null) {
      gaSession = auth.get("session");
    }
    
    if (host != null && feed != null && stream != null) {
      flexTPSStream = new FlexTPSStream(host, feed, stream, gaSession);
      
      if (flexTPSStream.canDoRobotic()) {
        setRoboticControls();
      } else {
        flexTPSStream = null;
        removeRoboticControls();
      }
    }
  }
  
	public boolean supportsMultipleChannels() {
		return false;
	}
	
	public boolean setChannel(String channelName) {
		if (!isChannelSupported(channelName)) {
			return false;
		}
		
		return super.setChannel(channelName);
	}
	
	private boolean isChannelSupported(String channelName) {
		Channel channel = rbnbController.getChannel(channelName);
    if (channel == null) {
      return false;
    }
    
		String mimeType = channel.getMetadata("mime");
        
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
  
  protected void channelAdded(String channelName) {
    clearImage();
    setupFlexTPSStream();
  }
  
  protected void channelRemoved(String channelName) {
    clearImage();
    flexTPSStream = null;
    removeRoboticControls();
  }
	
	public void postData(ChannelMap channelMap) {
		super.postData(channelMap);
	}

	public void postTime(double time) {
		super.postTime(time);
    
    postImage();
    
    //clear stale images
    if (displayedImageTime != -1 && (displayedImageTime <= time-timeScale || displayedImageTime > time)) {
      clearImage();
    }
  }
  
  private void postImage() {
		if (channelMap == null) {
			//no data to display yet
			return;
		}
		
		Iterator it = channels.iterator();
		if (!it.hasNext()) {
			//no channels to post to
			return;
		}
			
		final String channelName = (String)it.next();
		
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
				if (times[i] <= time && times[i] > time-timeScale) {
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
        final InputStream in = new ByteArrayInputStream(displayedImageData);
        
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            try {
              image.setImage(ImageIO.read(in));
            } catch (IOException e) {
              log.error("Failed to decode image for " + channelName + ".");
              e.printStackTrace();
            }
          }
        });
 				
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
  
  public void postState(int newState, int oldState) {
    super.postState(newState, oldState);
    
    setRoboticControls();
  }
	
	private void clearImage() {
		image.setImage(null);
		displayedImageData = null;
		displayedImageTime = -1;
	}

  @Override
  public void setProperty(String key, String value) {
    super.setProperty(key, value);
    
    if (key == null) {
      return;
    }
    
    if (key.equals(DATA_PANEL_PROPERTY_SCALE)) {
      try {
        double scale = Double.parseDouble(value);
        image.setScale(scale);
      } catch (NumberFormatException e) {
        log.warn("Unable to set scale: " + value + ".");
      }
    } else if (key.equals(DATA_PANEL_PROPERTY_SHOW_NAVIGATION_IMAGE) && Boolean.parseBoolean(value)) {
      image.setNavigationImageEnabled(true);
    } else if (key.equals(DATA_PANEL_PROPERTY_HIDE_ROBOTIC_CONTROLS) && Boolean.parseBoolean(value)) {
      hideRoboticControlsMenuItem.setSelected(true);
      setRoboticControls();
    }
  }
  
  public void authenticationChanged(AuthenticationEvent event) {
    setupFlexTPSStream();
  }  
	
}