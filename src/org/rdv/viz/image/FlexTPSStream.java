/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2007 Palta Software
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

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.SSLHandshakeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class FlexTPSStream {
  private String host;
  private String feed;
  private String stream;

  private String gaSession;
  
  private boolean pan;
  private boolean tilt;
  private boolean zoom;
  private boolean focus;
  private boolean iris;
  
  private final String ROBOTIC_PAN_LEFT = "ctrl=rpan&amp;value=left";
  private final String ROBOTIC_PAN_RIGHT = "ctrl=rpan&amp;value=right";
  private final String ROBOTIC_TILT_UP = "ctrl=rtilt&amp;value=up";
  private final String ROBOTIC_TILT_DOWN = "ctrl=rtilt&amp;value=down";
  private final String ROBOTIC_HOME = "ctrl=home";
  private final String ROBOTIC_ZOOM_IN = "ctrl=rzoom&amp;value=in";
  private final String ROBOTIC_ZOOM_OUT = "ctrl=rzoom&amp;value=out";
  private final String ROBOTIC_FOCUS_NEAR = "ctrl=rfocus&amp;value=near";
  private final String ROBOTIC_FOCUS_FAR = "ctrl=rfocus&amp;value=far";
  private final String ROBOTIC_FOCUS_AUTO = "ctrl=focus&amp;value=auto";    
  private final String ROBOTIC_IRIS_CLOSE = "ctrl=riris&amp;value=close";
  private final String ROBOTIC_IRIS_OPEN = "ctrl=riris&amp;value=open";
  private final String ROBOTIC_IRIS_AUTO = "ctrl=iris&amp;value=auto";
  
  public FlexTPSStream(String host, String feed, String stream, String gaSession) {
    this.host = host;
    this.feed = feed;
    this.stream = stream;
    
    if (gaSession == null || gaSession.length() == 0) {
      this.gaSession = "";
    } else {
  	  this.gaSession = "&amp;GAsession=" + gaSession;
    }
    
    pan = tilt = zoom = focus = iris = false;
    
    loadStream();
  }
  
  private String getBaseURL() {
    return "https://" + host + "/feeds/" + feed + "/" + stream;
  }
  
  private void loadStream() {
    String streamURL = getBaseURL() + gaSession;
    
    Document document;
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      document = documentBuilder.parse(streamURL);
    } catch (SSLHandshakeException e) {
  	  // SSL exception will happen if the flexTPS server 
  	  // certificate is not recognized but this won't be problem
  	  return;
    } catch (IOException e) {
      e.printStackTrace();
      return;
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      return;
    } catch (SAXException e) {
      e.printStackTrace();
      return;
    }

    XPath xp = XPathFactory.newInstance().newXPath();
    
    try {
      Node roboticNode = (Node)xp.evaluate("/feeds/feed/stream/robotic", document, XPathConstants.NODE);
      if (roboticNode == null) {
        return;
      }
      NodeList roboticNodes = roboticNode.getChildNodes();
      for (int i=0; i<roboticNodes.getLength(); i++) {
        Node node = (Node)roboticNodes.item(i);
        if (node.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        
        String control = node.getNodeName();
        boolean enabled = node.getTextContent().equals("true");
        
        if (control.equals("zoom")) {
          zoom = enabled;
        } else if (control.equals("tilt")) {
          tilt = enabled;
        } else if (control.equals("focus")) {
          focus = enabled;
        } else if (control.equals("iris")) {
          iris = enabled;
        } else if (control.equals("pan")) {
          pan = enabled;
        }
      }
    } catch (XPathExpressionException e) {
      e.printStackTrace();
      return;
    }
  }
  
  private void executeRoboticCommand(String command) {
    URL cameraURL = null;
    try {
      cameraURL = new URL(getBaseURL() + "/robotic/?" + command + gaSession);
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return;
    }
    
    URLConnection cameraConnection = null;
    try {
      cameraConnection = cameraURL.openConnection();
    } catch (SSLHandshakeException e) {
  	  // SSL exception will happen if the flexTPS server 
  	  // certificate is not recognized but this won't be problem
  	  return;
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    
    try {
      cameraConnection.connect();
    } catch (SSLHandshakeException e) {
  	  // SSL exception will happen if the flexTPS server 
  	  // certificate is not recognized but this won't be problem
  	  return;
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    
    try {
      ((HttpURLConnection)cameraConnection).getResponseMessage();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }    
  
  public boolean canPan() {
    return pan;
  }
  
  public void panLeft() {
    if (!pan) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_PAN_LEFT);
  }
  
  public void panRight() {
    if (!pan) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_PAN_RIGHT);
  }
  
  public void pan(int p) {
    if (!pan) {
      return;
    }
    
    String command = "ctrl=apan&amp;imagewidth=100&amp;value=" + p + ",0";
    executeRoboticCommand(command);
  }
  
  public boolean canTilt() {
    return tilt;
  }
  
  public void tiltUp() {
    if (!tilt) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_TILT_UP);
  }
  
  public void tiltDown() {
    if (!tilt) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_TILT_DOWN);
  }
  
  public void tilt(int t) {
    if (!tilt) {
      return;
    }

    String command = "ctrl=atilt&amp;imageheight=100&amp;value=0," + t;
    executeRoboticCommand(command);
  }
  
  public void goHome() {
    if (!pan || !tilt) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_HOME);
  }
  
  public void center(Point clickLocation, Dimension imageDimension) {
    if (!pan || !tilt) {
      return;
    }
    
    String command = "ctrl=center&amp;imageheight=" + imageDimension.height +
      "&amp;imagewidth=" + imageDimension.width +
      "&amp;value=?" + clickLocation.x + "," + clickLocation.y;
    executeRoboticCommand(command);
  }
  
  public boolean canZoom() {
    return zoom;
  }
  
  public void zoomIn() {
    if (!zoom) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_ZOOM_IN);
  }
  
  public void zoomOut() {
    if (!zoom) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_ZOOM_OUT);
  }
  
  public void zoom(int z) {
    if (!zoom) {
      return;
    }
    
    String command = "ctrl=azoom&amp;imagewidth=100&amp;value=" + z + ",0";
    executeRoboticCommand(command);
  }

  public boolean canFocus() {
    return focus;
  }
  
  public void focusNear() {
    if (!focus) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_FOCUS_NEAR);
  }
  
  public void focusFar() {
    if (!focus) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_FOCUS_FAR);
  }
  
  public void focus(int f) {
    if (!focus) {
      return;
    }

    String command = "ctrl=afocus&amp;imagewidth=100&amp;value=" + f + ",0";
    executeRoboticCommand(command);      
  }
  
  public void focusAuto() {
    if (!focus) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_FOCUS_AUTO);
  }
  
  public boolean canIris() {
    return iris;
  }
  
  public void irisClose() {
    if (!iris) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_IRIS_CLOSE);
  }
  
  public void irisOpen() {
    if (!iris) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_IRIS_OPEN);
  }
  
  public void iris(int i) {
    if (!iris) {
      return;
    }

    String command = "ctrl=airis&amp;imagewidth=100&amp;value=" + i + ",0";
    executeRoboticCommand(command);

  }
  
  public void irisAuto() {
    if (!iris) {
      return;
    }
    
    executeRoboticCommand(ROBOTIC_IRIS_AUTO);
  }
  
  public boolean canDoRobotic() {
    return (pan == true || tilt == true || zoom == true ||
        focus == true || iris == true);
  }
}