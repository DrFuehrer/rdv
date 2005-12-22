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
 * $URL: svn+ssh://code.nees.buffalo.edu/repository/RDV/trunk/src/org/nees/buffalo/rdv/datapanel/JFreeChartDataPanel.java $
 * $Revision: 352 $
 * $Date: 2005-12-18 14:32:16 -0500 (Sun, 18 Dec 2005) $
 * $Author: jphanley $
 */
package org.nees.buffalo.rdv.datapanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.buffalo.rdv.DataPanelManager;
import org.nees.buffalo.rdv.Extension;
import org.nees.buffalo.rdv.rbnb.Channel;
import org.nees.buffalo.rdv.rbnb.MetadataManager;
import org.nees.buffalo.rdv.rbnb.PlaybackRateListener;
import org.nees.buffalo.rdv.rbnb.RBNBController;

import com.rbnb.sapi.ChannelMap;

public class AudioDataPanel extends AbstractDataPanel implements PlaybackRateListener {
  
  static Log log = LogFactory.getLog(AudioDataPanel.class.getName());
  
  SourceDataLine audioOut;
  FloatControl gainControl;
  BooleanControl muteControl;
  
  double lastTime;
  
  JPanel panel;
  FloatSlider gainSlider;
  BooleanCheckBox muteCheckBox;
  LevelMeter meter;
  
  double playbackRate;
  
  byte level;
  
  public AudioDataPanel() {
    super();
    
    lastTime = -1;
    
    level = 0;
    
    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    
    setDataComponent(panel);
  }
  
  public void openPanel(final DataPanelManager dataPanelManager) {
    super.openPanel(dataPanelManager);
    
    rbnbController.addPlaybackRateListener(this);
  }

  public boolean supportsMultipleChannels() {
    return false;
  }
  
  public boolean setChannel(String channelName) {
    if (!isChannelSupported(channelName)) {
      return false;
    }
    
    if (super.setChannel(channelName)) {
      MetadataManager metadataManager = rbnbController.getMetadataManager();
      Channel channel = metadataManager.getChannel(channelName);
      
      float sampleRate = Float.parseFloat(channel.getMetadata("samplerate"));
      int sampleSize = Integer.parseInt(channel.getMetadata("samplesize"));
      int channels = Integer.parseInt(channel.getMetadata("channels"));
      boolean signed = channel.getMetadata("signed").equals("1");
      boolean endian = channel.getMetadata("endian").equals("1");
      
      AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSize, channels, signed, endian);
      try {
        audioOut = AudioSystem.getSourceDataLine(audioFormat);
        audioOut.open(audioFormat, (int)(sampleRate*sampleSize*channels));        
        audioOut.start();
      } catch (LineUnavailableException e) {
        e.printStackTrace();
        super.removeChannel(channelName);
        return false;
      }
      
      JPanel controlPanel = new JPanel();
      controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
      controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      
      gainControl = null;
      if (audioOut.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
        gainControl = (FloatControl)audioOut.getControl(FloatControl.Type.MASTER_GAIN);
        
        JLabel volumeLabel = new JLabel("Volume");
        volumeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(volumeLabel);
        
        gainSlider = new FloatSlider(gainControl);
        gainSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(gainSlider);
      }
      
      muteControl = null;
      if (audioOut.isControlSupported(BooleanControl.Type.MUTE)) {
        muteControl = (BooleanControl)audioOut.getControl(BooleanControl.Type.MUTE);
        muteCheckBox = new BooleanCheckBox(muteControl, "Mute");
        muteCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(muteCheckBox);
      }
      
      panel.add(controlPanel, BorderLayout.WEST);
      
      JPanel levelPanel = new JPanel();
      levelPanel.setLayout(new BoxLayout(levelPanel, BoxLayout.PAGE_AXIS));
      levelPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
      
      JLabel levelLabel = new JLabel("Level");
      levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      levelPanel.add(levelLabel);      
      
      meter = new LevelMeter();
      meter.setAlignmentX(Component.CENTER_ALIGNMENT);
      levelPanel.add(meter);
      
      panel.add(levelPanel, BorderLayout.EAST);
            
      return true;
    } else {
      return false;
    }   
  }
  
  private boolean isChannelSupported(String channelName) {
    Channel channel = rbnbController.getChannel(channelName);
    String mimeType = channel.getMetadata("mime");
    
    if (mimeType == null) {
      return false;
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
  
  void clearData() {
    lastTime = -1;
    level = 0;
  }
  
  public void postTime(double time) {
    super.postTime(time);
      
    if (channelMap == null) {
      //no data to display yet
      return;
    }
    
    int state = rbnbController.getState();
    if (!(state == RBNBController.STATE_PLAYING ||
        state == RBNBController.STATE_MONITORING ||
        state == RBNBController.STATE_REALTIME)) {
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
        log.error("Expected byte array for audio data in channel " + channelName + ".");
        log.info("" + channelMap.GetType(channelIndex));
        return;
      }

      double[] times = channelMap.GetTimes(channelIndex);
      
      int startIndex = times.length;
      if (lastTime == -1) {
        startIndex = 0;
      } else {       
        for (int i=0; i<times.length; i++) {
          if (times[i] > lastTime) {
            startIndex = i;
            break;
          }
        }
      }
      
      if (startIndex == times.length) {
        return;
      }
      
      int endIndex = times.length;
      
      for (int i=startIndex; i<times.length; i++) {
        if (times[i] > time) {
          endIndex = i;
        }
      }
      
      lastTime = times[endIndex-1];
      
      // we can only play back audio in real-time
      if (playbackRate != 1) {
        return;
      }
      
      byte[][] audioData = channelMap.GetDataAsByteArray(channelIndex);
      for (int i=startIndex; i<endIndex; i++) {
        audioOut.write(audioData[i], 0, audioData[i].length);
        updateLevel(audioData[i]);
      }
      
    } catch (Exception e) {
      log.error("Failed to receive data for channel " + channelName + ".");
      e.printStackTrace();
    }
  }
  
  private void updateLevel(byte[] data) {
    for (int i=0; i<data.length; i++) {
      level = (byte)(0.9*level + 0.1*data[i]);
      meter.setLevel(level);
    }
  }
  
  public void closePanel() {
    super.closePanel();
    
    audioOut.stop();
    audioOut.flush();
    audioOut.close();
  }
  
  public void playbackRateChanged(double playbackRate) {
    this.playbackRate = playbackRate;
  }
  
  class FloatSlider extends JSlider implements ChangeListener {
    FloatControl control;
    
    public FloatSlider(FloatControl volumeControl) {
      this(volumeControl, JSlider.VERTICAL);
    }
      
    public FloatSlider(FloatControl control, int orientation) {
      super(orientation);
      
      this.control = control;
      
      setMinimum((int)control.getMinimum());
      setMaximum((int)control.getMaximum());
      
      setValue((int)control.getValue());
      
      addChangeListener(this);
    }
    
    public void stateChanged(ChangeEvent arg0) {
      control.setValue(getValue());         
    }        
  }
  
  class BooleanCheckBox extends JCheckBox implements ChangeListener {
    BooleanControl control;
    
    public BooleanCheckBox(BooleanControl control) {
      this(control, null);
    }
    
    public BooleanCheckBox(BooleanControl control, String text) {
      super(text);
      
      this.control = control;
      
      setSelected(control.getValue());
      
      addChangeListener(this);
    }

    public void stateChanged(ChangeEvent arg0) {
      control.setValue(isSelected());
    }
  }
  
  class LevelMeter extends JComponent {
    byte level;
    
    Color darkGreen = new Color(0, 150, 0);
    Color green = new Color(0, 254, 0);
    Color darkYellow = new Color(150, 150, 0);
    Color yellow = new Color(254, 254, 0);
    Color darkRed = new Color(150, 0, 0);
    Color red = new Color(254, 0, 0);
    
    int barGap = 2;
    
    public LevelMeter() {
      level = 127;
    }
    
    public void setLevel(byte level) {
      this.level = level;
      repaint();
    }
    
    protected void paintComponent(Graphics g) {
      Insets insets = getInsets();
      
      int componentWidth = getWidth() - insets.left - insets.right;
      int componentHeight = getHeight() - insets.top - insets.bottom;
      
      int barWidth = Math.min(10, componentWidth);
      int barHeight = Math.round(componentHeight/20f);
      
      int levelHeight = Math.round((127-level)*(20f/255f));
      
      if (isOpaque()) {
        g.setColor(getBackground());
        g.fillRect(0, 0, componentWidth-1, componentHeight-1);
      }
      
      for (int i=1; i<=20; i++) {
        Color color;
        if (i <= 10) {
          if (i > levelHeight) {
            color = darkGreen;
          } else {
            color = green;
          }
        } else if (i <= 16) {
          if (i > levelHeight) {
            color = darkYellow;
          } else {
            color = yellow;
          }          
        } else {
          if (i > levelHeight) {
            color = darkRed;
          } else {
            color = red;
          }
        }
        g.setColor(color);
        g.fillRect(insets.left, insets.top+componentHeight-(i*barHeight)+barGap, barWidth-1, barHeight-barGap-1);
        
        g.setColor(getForeground());
      }
    }
    
    public Dimension getPreferredSize() {
      Insets insets = getInsets();
      return new Dimension(insets.left + 10 + insets.right, 100);
    }
  }
  
}
