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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RendererDataPanel extends AbstractDataPanel {
  
  static Log log = LogFactory.getLog(RendererDataPanel.class.getName());
  
  RendererComponent renderer;
  
  double lastTime;
  
  public RendererDataPanel() {
    super();
    
    lastTime = -1;
    
    renderer = new RendererComponent();
    
    setDataComponent(renderer);
  }

  public boolean supportsMultipleChannels() {
    return true;
  }
  
  public void postTime(double time) {
    super.postTime(time);
      
    if (channelMap == null) {
      //no data to display yet
      return;
    }
    
    Iterator it = channels.iterator();
    while (it.hasNext()) {      
      String xChannel = null;
      int xIndex = -1;
      String yChannel = null;
      int yIndex = -1;
      String zChannel = null;
      int zIndex = -1;
      
      xChannel = (String)it.next();
      xIndex = channelMap.GetIndex(xChannel);
      if (xIndex == -1) {
        return;
      }
      if (it.hasNext()) {
        yChannel = (String)it.next();
        yIndex = channelMap.GetIndex(yChannel);
        if (yIndex == -1) {
          return;
        }
        if (it.hasNext()) {
          zChannel = (String)it.next();
          zIndex = channelMap.GetIndex(zChannel);
          if (zIndex == -1) {
            return;
          }
        }
      }    
     
      double[] times = channelMap.GetTimes(xIndex);
      
      if (times.length == 0) {
        return;
      }
      
      int dataIndex = -1;
      
      for (int i=times.length-1; i>=0; i--) {
        if (times[i] > lastTime && times[i] <= time) {
          dataIndex = i;
          break;
        }
      }
      
      if (dataIndex != -1) {
        double x = 0;
        double y = 0;
        double z = 0;
        
        x = channelMap.GetDataAsFloat64(xIndex)[dataIndex];
        if (yChannel != null) {
          y = channelMap.GetDataAsFloat64(yIndex)[dataIndex];
          if (zChannel != null) {
            z = channelMap.GetDataAsFloat64(zIndex)[dataIndex];
          }
        }
        
        Point3D point = new Point3D(x, y, z);
        renderer.setPoint(xChannel, point);
      }
    }
    
    lastTime = time;
  }

  class RendererComponent extends JComponent {
    
    HashMap points;
    
    public RendererComponent() {
      points = new HashMap();
    }
    
    public void setPoint(String channelName, Point3D point) {
      Point3D oldPoint = (Point3D)points.put(channelName, point);
      if ((oldPoint != null && !oldPoint.equals(point)) || (oldPoint != point)) {
        repaint();
      }
    }
    
    public void clear() {
      points.clear();
      repaint();
    }
    
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      
      Set channels = points.keySet();
      Iterator it = channels.iterator();
      while (it.hasNext()) {
        Point3D point = (Point3D)points.get(it.next());
        paintComponent(g, point);
      }
    }
    
    protected void paintComponent(Graphics g, Point3D point) {
      if (point == null) {
        return;
      }
     
      double minX = -250;
      double maxX = 250;
      double minY = -250;
      double maxY = 250;
      
      int maxZ = 10;
           
      if (point.x >= minX && point.x <= maxX &&
          point.y >= minY && point.y <= maxY &&
          point.z <= maxZ) {
        int pointSize = (int)Math.round(-100/(point.z-maxZ-1));
        
        Insets insets = getInsets();      
        int width = getWidth() - insets.right - pointSize;
        int height = getHeight() - insets.bottom - pointSize;
        
        int x = (int)Math.round((point.x-minX)/(maxX-minX)*width);
        int y = (int)Math.round((point.y-minY)/(maxY-minY)*height);
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(Color.GREEN);              
        g2.fill(new Ellipse2D.Double(x+1, height-y+1, pointSize-2, pointSize-2));
        g2.setPaint(this.getForeground());
        g2.draw(new Ellipse2D.Double(x, height-y, pointSize-1, pointSize-1));        
      }
    }
    
    public Dimension getPreferredSize() {
      return new Dimension(400, 300);
    }
  }
  
  class Point3D {
    public double x, y, z;
    
    public Point3D(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }
    
    public double getX() {
      return x;
    }
    
    public double getY() {
      return y;
    }
    
    public double getZ() {
      return z;
    }
    
    public boolean equals(Point3D p) {
      if (p == null) {
        return false;
      }
      
      return x==p.x && y==p.y && z==p.z;
    }
    
    public String toString() {
      return "(" + x + "," + y + "," + z + ")";
    }
  }
}
