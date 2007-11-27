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

package org.nees.buffalo.rdv.datapanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.nees.audio.AudioPlayer;
import org.nees.buffalo.rdv.DataPanelManager;
import org.nees.buffalo.rdv.DataViewer;
import org.nees.video.VLCLauncher;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * A data panel to display a webpage. This data panel does not use the RBNB
 * server in any way, and instead opens webpages specified by the user.
 * 
 * This class uses a JEditorPane to display the webpage, so it is limited to
 * whatever HTML this supports, which is very limited.
 * 
 * @author Jason P. Hanley
 *
 */
public class WebDataPanel extends AbstractDataPanel {

  /**
   * The main panel.
   */
  private JPanel panel;
  
  /**
   * The panel with the address bar and controls.
   */
  private JPanel topPanel;
  
  /**
   * The URL field
   */
  private JTextField locationField;
  
  /**
   * The component to display the webpage.
   */
  private JEditorPane htmlRenderer;
  
  /**
   * The menu item for the popup menu that controls the visibility of the
   * address bar.
   */
  private JCheckBoxMenuItem showAddressBarMenuItem; 

  /**
   * The menu item to reload the page
   */
  JMenuItem reloadMenuItem;
  
  /**
   * The menu containing items for auto reloading
   */
  JMenu autoReloadMenu;  
  
  /**
   * The menu item to control auto reolading
   */
  JCheckBoxMenuItem enableAutoReloadMenuItem;

  /**
   * Indicator for auto reloading
   */
  private boolean autoReloadEnabled;
  
  /**
   * How often (in milliseconds) to reload the page
   */
  private long autoReloadTime;
  
  /**
   * The timer that does the reloading
   */
  private Timer autoReloadTimer;
  
  /**
   * Creates the web data panel with no webpage displayed.
   */
  public WebDataPanel() {
    autoReloadEnabled = false;
    autoReloadTime = 60*1000;
    
    // the main panel
    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    
    // the panel with the controls
    topPanel = new JPanel();
    topPanel.setLayout(new BorderLayout(5,0));
    topPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0,0,1,0,Color.lightGray),
        BorderFactory.createEmptyBorder(5,5,5,5)));

    topPanel.add(new JLabel("Address"), BorderLayout.WEST);

    // the address text field
    locationField = new JTextField();
    locationField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        loadPage();
      }      
    });
    topPanel.add(locationField, BorderLayout.CENTER);
    
    JButton goButton = new JButton("Go", DataViewer.getIcon("icons/go.gif"));
    goButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        loadPage();
      }      
    });
    topPanel.add(goButton, BorderLayout.EAST);
    
    panel.add(topPanel, BorderLayout.NORTH);
    
    // the component to render the HTML
    htmlRenderer = new JEditorPane();
    htmlRenderer.setEditable(false);
    htmlRenderer.setContentType("text/html");
    htmlRenderer.addPropertyChangeListener("page", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) {
        pageLoaded();
      }
    });
    
    // Listener for hypertext events for our htmlRenderer
    htmlRenderer.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent evt) {
    	  if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {  // a hyperlink is clicked on the page
          URL url = evt.getURL();
          String location;
          if (url == null) {
            location = evt.getDescription();
          } else {
            location = url.toString();
          }
          loadExternalPage(location);
    	  }
      }  
    });

    // the scroll bar for the HTML renderer
    JScrollPane scrollPane = new JScrollPane(htmlRenderer,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBorder(null);
    panel.add(scrollPane, BorderLayout.CENTER);

    // popup menu for panel
    JPopupMenu popupMenu = new JPopupMenu();
    
    showAddressBarMenuItem = new  JCheckBoxMenuItem("Show address bar", true);
    showAddressBarMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        showAddressBar(showAddressBarMenuItem.isSelected());
      }      
    });
    popupMenu.add(showAddressBarMenuItem);
    
    popupMenu.addSeparator();
    
    reloadMenuItem = new JMenuItem("Reload");
    reloadMenuItem.setEnabled(false);
    reloadMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        loadPage();
      }      
    });
    popupMenu.add(reloadMenuItem);
    
    autoReloadMenu = new JMenu("Reload every");
    autoReloadMenu.setEnabled(false);
    
    enableAutoReloadMenuItem = new JCheckBoxMenuItem("Enable");
    enableAutoReloadMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if (enableAutoReloadMenuItem.isSelected()) {
          startAutoReload();
        } else {
          stopAutoReload();
        }
      }
    });
    autoReloadMenu.add(enableAutoReloadMenuItem);
    
    autoReloadMenu.addSeparator();
    
    JRadioButtonMenuItem reloadTimeMenuItem;
    ButtonGroup reloadTimeButtonGroup = new ButtonGroup();
    
    reloadTimeMenuItem = new JRadioButtonMenuItem("15 seconds");
    reloadTimeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setAutoReloadTime(15*1000);
        startAutoReload();
      }      
    });
    autoReloadMenu.add(reloadTimeMenuItem);
    reloadTimeButtonGroup.add(reloadTimeMenuItem);
    
    reloadTimeMenuItem = new JRadioButtonMenuItem("30 seconds");
    reloadTimeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setAutoReloadTime(30*1000);
        startAutoReload();
      }      
    });    
    autoReloadMenu.add(reloadTimeMenuItem);
    reloadTimeButtonGroup.add(reloadTimeMenuItem);

    reloadTimeMenuItem = new JRadioButtonMenuItem("1 minute");
    reloadTimeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setAutoReloadTime(60*1000);
        startAutoReload();
      }      
    });    
    reloadTimeMenuItem.setSelected(true);
    autoReloadMenu.add(reloadTimeMenuItem);
    reloadTimeButtonGroup.add(reloadTimeMenuItem);

    reloadTimeMenuItem = new JRadioButtonMenuItem("5 minutes");
    reloadTimeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setAutoReloadTime(5*60*1000);
        startAutoReload();
      }      
    });    
    autoReloadMenu.add(reloadTimeMenuItem);
    reloadTimeButtonGroup.add(reloadTimeMenuItem);

    reloadTimeMenuItem = new JRadioButtonMenuItem("15 minutes");
    reloadTimeMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setAutoReloadTime(15*60*1000);
        startAutoReload();
      }      
    });    
    autoReloadMenu.add(reloadTimeMenuItem);
    reloadTimeButtonGroup.add(reloadTimeMenuItem);
    
    popupMenu.add(autoReloadMenu);

    // set component popup and mouselistener to trigger it
    panel.setComponentPopupMenu(popupMenu);
    htmlRenderer.setComponentPopupMenu(popupMenu);
    panel.addMouseListener(new MouseInputAdapter() {});
    
    // set the component for this data panel with AbstractDataPanel
    setDataComponent(panel);
  }
  
  /**
   * Controls the visibility of the address bar.
   * 
   * @param showAddressBar  show the address bar if true, hide it otherwise
   */
  private void showAddressBar(boolean showAddressBar) {
    if (topPanel.isVisible() != showAddressBar) {
      topPanel.setVisible(showAddressBar);
      showAddressBarMenuItem.setSelected(showAddressBar);
      properties.setProperty("showAddressBar", Boolean.toString(showAddressBar));
    }
  }
  
  /**
   * Set the amount of time between relaoding when auto reload is on.
   * 
   * @param time  the tiem between reloading in milliseconds
   */
  private void setAutoReloadTime(long time) {
    if (autoReloadTime != time) {
      autoReloadTime = time;
  
      if (autoReloadTime == 15*1000) {
        autoReloadMenu.getItem(2).setSelected(true);
      } else if (autoReloadTime == 30*1000) {
        autoReloadMenu.getItem(3).setSelected(true);
      } else if (autoReloadTime == 60*1000) {
        autoReloadMenu.getItem(4).setSelected(true);
      } else if (autoReloadTime == 5*60*1000) {
        autoReloadMenu.getItem(5).setSelected(true);
      } else if (autoReloadTime == 15*60*1000) {
        autoReloadMenu.getItem(6).setSelected(true);
      }
      
      properties.setProperty("autoReloadTime", Long.toString(autoReloadTime));
      
      if (autoReloadEnabled) {
        startAutoReload();
      }
    }        
  }
  
  /**
   * Start the automatic reloading of the current page.
   */
  private void startAutoReload() {
    autoReloadEnabled = true;
    
    if (autoReloadTimer != null) {
      autoReloadTimer.cancel();
      autoReloadTimer = null;
    }    
    
    enableAutoReloadMenuItem.setSelected(true);
    
    TimerTask reloadTimerTask = new TimerTask() {
      public void run() {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            loadPage();
          }
        });
      }
    };

    autoReloadTimer = new Timer();
    autoReloadTimer.scheduleAtFixedRate(reloadTimerTask, autoReloadTime, autoReloadTime);
    
    properties.setProperty("autoReload", "true");    
  }

  /**
   * Stop the automatic reloading of the current page.
   */  
  private void stopAutoReload() {
    autoReloadEnabled = false;

    if (autoReloadTimer != null) {
      autoReloadTimer.cancel();
      autoReloadTimer = null;
    }
    
    enableAutoReloadMenuItem.setSelected(false);
    
    properties.remove("autoReload");
  }
  
  /**
   * Loads the webpage URL specified in the address text field.
   */
  private void loadPage() {
    String location = locationField.getText();
    
    try {
      location = URLDecoder.decode(location, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    
    if (!location.matches("^[a-zA-Z]+://.*")) {
      // assume http if no protocol is specified
      location = "http://" + location;
    }    

    loadPage(location);
  }
  
  /**
   * Load the webpage URL specified by <code>location</code>.
   * 
   * @param location  the webpage URL to load
   */
  private void loadPage(String location) {
    // clear out old document
    htmlRenderer.setDocument(htmlRenderer.getEditorKit().createDefaultDocument());

     // clear description
    setDescription(null);

    try {
      htmlRenderer.setPage(new URL(location));
    } catch (Exception e) {
      locationField.selectAll();
      JOptionPane.showMessageDialog(null,
          "Failed to load page: " + e.getMessage() + ".",
          "Error loading page", JOptionPane.ERROR_MESSAGE);
      
      stopAutoReload();
      reloadMenuItem.setEnabled(false);
      autoReloadMenu.setEnabled(false);
    }
      
    htmlRenderer.requestFocusInWindow();
  }
  
  /**
   * Load the webpage URL specified by <code>location</code> in an external
   * program.
   * 
   * @param location  the webpage URL to load
   */
  private void loadExternalPage(String location) {
    try {
      if (location.startsWith("udp")) {
        VLCLauncher.launchVLC(location.toString());
      } else if (location.startsWith("http://")){
        boolean isAudio = false;
        
        // see if this is a icecast/shoutcast stream
        try {
          URL url = new URL (location);
          URLConnection connection = url.openConnection();
          
          String contentType = connection.getHeaderField("Content-Type");
          
          // detect icecast streams by content type
          if (contentType != null && contentType.compareToIgnoreCase("audio/mpeg") == 0) {
            isAudio = true;
          } else {
            // detect shoutcast streams - they start with ICY
            byte[] buffer = new byte[3];
            connection.getInputStream().read(buffer);
            connection.getInputStream().close();
            String protocol = new String(buffer);
            if (protocol.compareToIgnoreCase("ICY") == 0) {
              isAudio = true;
            }
          }
        } catch (Exception e) {} // ignore all exceptions in audio stream detection
        
        if (isAudio) {
          AudioPlayer.getInstance().setURL(new URL(location));
        } else {        
          DataViewer.browse(new URL(location));
        }
      } else {
        DataViewer.browse(new URL(location));
      }
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null,
          "Unable to open the URL in an external program.",
          "Web Data Panel Error", JOptionPane.ERROR_MESSAGE);
    }
  }  
  
  /**
   * Called when the page has been loaded. Sets the page title.
   */
  private void pageLoaded() {
    String location = htmlRenderer.getPage().toString();
    try {
      properties.setProperty("location", URLEncoder.encode(location, "UTF-8"));
    } catch (UnsupportedEncodingException ignore) { }
    
    locationField.setText(location);
    
    setDescription(getTitle());
    
    reloadMenuItem.setEnabled(true);
    autoReloadMenu.setEnabled(true);
  }
  
  public void openPanel(final DataPanelManager dataPanelManager) {
    super.openPanel(dataPanelManager);

    // set preferred size for detached mode
    component.setPreferredSize(new Dimension(800,600));
    
    locationField.requestFocusInWindow();
  }
  
  /**
   * Cleanup up the data panel.
   */
  public void closePanel() {
    super.closePanel();
    
    if (autoReloadTimer != null) {
      autoReloadTimer.cancel();
      autoReloadTimer = null;
    }    
  }

  /**
   * Always returns false since this data panel doesn't support channels.
   * 
   * @return always false
   */
  public boolean setChannel(String channelName) {
    return false;
  }

  /**
   * Always returns false since this data panel doesn't support channels.
   * 
   * @return always false
   */
  public boolean addChannel(String channelName) {
    return false;
  }

  void clearData() {}

  public boolean supportsMultipleChannels() {
    return false;
  }
  
  /**
   * Returns the title of the webpage being displayed, or null of none is being
   * displayed.
   * 
   * @returns the title of the webpage, or null if none is being displayed
   */
  String getTitle() {
    return (String)htmlRenderer.getDocument().getProperty("title");
  }
  
  public void setProperty(String key, String value) {
    super.setProperty(key, value);
    
    if (key == null) {
      return;
    }
     
    if (key.equals("location")) {
      loadPage(value);  
    } else if (key.equals("showAddressBar")) {
      showAddressBar(Boolean.parseBoolean(value));
    } else if (key.equals("autoReload")) {
      startAutoReload();
    } else if (key.equals("autoReloadTime")) {
      setAutoReloadTime(Long.parseLong(value));
    }
  }
  
  public String toString() {
    return "Web Data Panel";
  }  
}