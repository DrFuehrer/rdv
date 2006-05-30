/*
 * RDV
 * Real-time Data Viewer
 * http://nees.buffalo.edu/software/RDV/
 * 
 * Copyright (c) 2005-2006 University at Buffalo
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.MouseInputAdapter;

import org.nees.buffalo.rdv.DataPanelManager;
import org.nees.buffalo.rdv.DataViewer;

import java.lang.reflect.Method;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

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
   * Creates the web data panel with no webpage displayed.
   */
  public WebDataPanel() {
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
    		  if (isLinkSpecial(url.toString())) {
   				  int i = JOptionPane.showConfirmDialog(null,
   						  "This Link requires an external browser!\n"
   						  + "Proceed with launching a new window?",
   						  "Please Confirm",
   						  JOptionPane.YES_NO_CANCEL_OPTION);
    				  
   				  if (i == 0) // (constant) YES_OPTION selected
   					  loadExternalPage(url);
    		  }
    		  else
    			  loadPage(url.toString());
    	  } 
      }
    });

    // the scroll bar for the HTML renderer
    JScrollPane scrollPane = new JScrollPane(htmlRenderer,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
   * Loads the webpage URL specified in the address text field.
   */
  private void loadPage() {
    loadPage(locationField.getText());
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
      // make sure the protocol is http
      if (!location.startsWith("http://")) {
        if (location.matches("^[a-zA-Z]+://.*")) {
           throw new MalformedURLException("We don't support this protocol");
        } else {
          // assume http if no protocol is specified
          location = "http://" + location;
        }
      }      
 
      URL url = new URL(location);
	  if (isLinkSpecial(location)) {
		  int i = JOptionPane.showConfirmDialog(null,
				  "This Link requires an external browser!\n"
				  + "Proceed with launching a new window?",
				  "Please Confirm",
				  JOptionPane.YES_NO_CANCEL_OPTION);
		  
		  if (i == 0) // (constant) YES_OPTION selected
			  loadExternalPage(url);

	  } else {

 		  htmlRenderer.setPage(url);
 	      htmlRenderer.requestFocusInWindow();
 	  }
 
    } 
    catch (IOException e) {
      locationField.selectAll();
      JOptionPane.showMessageDialog(null,
          "Failed to load page: " + e.getMessage() + ".",
          "Web Data Panel Warning", JOptionPane.WARNING_MESSAGE);
      
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null,
                "Error: " + ex.getMessage(),
                "Web Data Panel Error", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  /**
   * Called when the page has been loaded. Sets the page title.
   */
  private void pageLoaded() {
    String location = htmlRenderer.getPage().toString();
    properties.setProperty("location", location);
    locationField.setText(location);
    
    setDescription(getTitle());
  }
  
  public void openPanel(final DataPanelManager dataPanelManager) {
    super.openPanel(dataPanelManager);

    // set preferred size for detached mode
    component.setPreferredSize(new Dimension(800,600));
    
    locationField.requestFocusInWindow();
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
    
    if (key != null) {
      if (key.equals("location")) {
        loadPage(value);  
      } else if (key.equals("showAddressBar")) {
        showAddressBar(Boolean.parseBoolean(value));
      }
    }
  }
  
  private void loadExternalPage(URL url) {
	  
  	// This method invokes the client's browser passing the url as argument for the target to open
    try { 
    
	  	String osName = System.getProperty("os.name");
	  	if (osName.startsWith("Mac OS")) {
	  		Class fileMgr = Class.forName("com.apple.eio.FileManager");
	  		Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
	  		openURL.invoke(null, new Object[] {url});
	  	} else if (osName.startsWith("Windows"))
	  		Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
	  	else { //assume Unix or Linux
	  		String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
	  		String browser = null;
	  		for (int count = 0; count < browsers.length && browser == null; count++)
	  			if (Runtime.getRuntime().exec( new String[] {"which", browsers[count]}).waitFor() == 0)
	  				browser = browsers[count];
	  		if (browser == null)
	  			throw new Exception("Could not find web browser");
	  		else Runtime.getRuntime().exec(new String[] {browser, url.toString()});
	  	}
    } catch(Exception ex) {
    	JOptionPane.showMessageDialog(null,
                "Error: " + ex.getMessage(),
                "Web Data Panel Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // a simple url parser to decide whether the link is a pdf or an MS document
  private boolean isLinkSpecial(String link) {

	  if (link == null) return false;
	  
	  if (link.charAt(link.length() - 4) == '.') {
		  String eXtension = link.substring(link.length() - 3); // extension of url if any 
		  if (("pdf").equalsIgnoreCase(eXtension)               // file extensions that need special handling  
					|| ("doc").equalsIgnoreCase(eXtension)
					|| ("xls").equalsIgnoreCase(eXtension))
			  return true;		  
	  }

	  return false;
  }
  
  
  public String toString() {
    return "Web Data Panel";
  }  
}
