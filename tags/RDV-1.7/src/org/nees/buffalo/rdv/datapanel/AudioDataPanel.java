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
import org.nees.buffalo.rdv.rbnb.PlaybackRateListener;
import org.nees.buffalo.rdv.rbnb.RBNBController;
import org.tritonus.share.sampled.FloatSampleBuffer;

import com.rbnb.sapi.ChannelMap;

public class AudioDataPanel extends AbstractDataPanel implements PlaybackRateListener {
  
  static Log log = LogFactory.getLog(AudioDataPanel.class.getName());
  
  SourceDataLine audioOut;
  FloatControl gainControl;
  BooleanControl muteControl;
  
  Channel channel;

  double playbackRate;
  double lastTime;
  float level;
  
  FloatSlider gainSlider;
  BooleanCheckBox muteCheckBox;
  LevelMeter meter;
  
  public AudioDataPanel() {
    super();
    
    lastTime = -1;
    
    level = 0;
    
    initPanel();    
  }
  
  public void openPanel(final DataPanelManager dataPanelManager) {
    super.openPanel(dataPanelManager);
    
    rbnbController.addPlaybackRateListener(this);
  }
  
  private void initPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    
    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
    controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    JLabel volumeLabel = new JLabel("Volume");
    volumeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    controlPanel.add(volumeLabel);
    
    gainSlider = new FloatSlider();
    gainSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
    controlPanel.add(gainSlider);
    
    muteCheckBox = new BooleanCheckBox("Mute");
    muteCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
    controlPanel.add(muteCheckBox);
    
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
    
    setDataComponent(panel);    
  }

  public boolean supportsMultipleChannels() {
    return false;
  }
  
  public boolean setChannel(String channelName) {
    if (!isChannelSupported(channelName)) {
      return false;
    }
    
    if (super.setChannel(channelName)) {
      channel = rbnbController.getMetadataManager().getChannel(channelName);
      
      setupAudioOutput();
            
      return true;
    } else {
      return false;
    }   
  }
  
  public boolean removeChannel(String channelName) {
    if (!super.removeChannel(channelName)) {
      return false;
    }
    
    channel = null;
    
    closeAudioOutput();
        
    clearData();
    
    return true;
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
    meter.setLevel(0);
    
    if (audioOut != null) {
      audioOut.flush();
    }
  }
  
  public void postTime(double time) {
    super.postTime(time);
      
    if (channelMap == null) {
      //no data to display yet
      return;
    }
    
    int state = rbnbController.getState();
    if (!(state == RBNBController.STATE_PLAYING ||
        state == RBNBController.STATE_MONITORING)) {
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
      
      if (endIndex > 0) {
        lastTime = times[endIndex-1];
      }
      
      if (audioOut == null) {
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
    FloatSampleBuffer sampleBuffer = new FloatSampleBuffer(data, 0, data.length, audioOut.getFormat());
    float[] normalizedData = sampleBuffer.getChannel(0);
    for (int i=0; i<normalizedData.length/4; i++) {
      level = (level +
              Math.abs(normalizedData[i]) +
              Math.abs(normalizedData[i+1]) +
              Math.abs(normalizedData[i+2]) +
              Math.abs(normalizedData[i+3]))/5;
      meter.setLevel(level);
    }
  }
  
  public void closePanel() {
    rbnbController.removePlaybackRateListener(this);
    
    super.closePanel();
    
    closeAudioOutput();
  }
  
  public void playbackRateChanged(double playbackRate) {
    if (this.playbackRate == playbackRate) {
      return;
    }
    
    this.playbackRate = playbackRate;

    setupAudioOutput();
  }
  
  public void postState(int newState, int oldState) {
    super.postState(newState, oldState);
    
    if (newState == oldState) {
      return;
    }
    
    setupAudioOutput();
  }

  private boolean setupAudioOutput() {
    if (channel == null) {
      return false;
    }
    
    float desiredPlaybackRate = 1;
    if (state == RBNBController.STATE_PLAYING) {
      desiredPlaybackRate = (float)(playbackRate);
    }
    
    float sampleRate = Float.parseFloat(channel.getMetadata("samplerate"));
    if (audioOut != null &&
        sampleRate*desiredPlaybackRate == audioOut.getFormat().getSampleRate()) {
      return true;
    }
    
    closeAudioOutput();

    sampleRate *= desiredPlaybackRate;
    int sampleSize = Integer.parseInt(channel.getMetadata("samplesize"));
    int channels = Integer.parseInt(channel.getMetadata("channels"));
    boolean signed = channel.getMetadata("signed").equals("1");
    boolean endian = channel.getMetadata("endian").equals("1");
    
    AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSize, channels, signed, endian);
    if (sampleRate != audioFormat.getSampleRate()) {
      closeAudioOutput();
      return false;
    }

    try {
      audioOut = AudioSystem.getSourceDataLine(audioFormat);
      audioOut.open(audioFormat);
    } catch (LineUnavailableException e) {
      closeAudioOutput();
      return false;
    }
    
    if (audioOut.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
      gainControl = (FloatControl)audioOut.getControl(FloatControl.Type.MASTER_GAIN);
      gainSlider.setControl(gainControl);
    }

    
    if (audioOut.isControlSupported(BooleanControl.Type.MUTE)) {
      muteControl = (BooleanControl)audioOut.getControl(BooleanControl.Type.MUTE);
      muteCheckBox.setControl(muteControl);
    }    
    
    audioOut.start();
    
    return true;
  }
  
  private void closeAudioOutput() {
    if (audioOut != null) {
      gainControl = null;
      gainSlider.setControl(null);
      
      muteControl = null;
      muteCheckBox.setControl(null);
      
      audioOut.stop();
      audioOut.flush();
      audioOut.close();
      audioOut = null;
    }
  }
  
  class FloatSlider extends JSlider implements ChangeListener {
    FloatControl control;
    
    public FloatSlider() {
      this(null, VERTICAL);
    }    
    
    public FloatSlider(int orientation) {
      this(null, orientation);
    }
    
    public FloatSlider(FloatControl volumeControl) {
      this(volumeControl, VERTICAL);
    }
      
    public FloatSlider(FloatControl control, int orientation) {
      super(orientation);

      setControl(control);
    }
    
    public void setControl(FloatControl control) {
      this.control = control;
      
      removeChangeListener(this);
      
      if (control != null) {
        setMinimum((int)control.getMinimum());
        setMaximum((int)control.getMaximum());
        setValue((int)control.getValue());
        addChangeListener(this);
        setEnabled(true);
      } else {
        setEnabled(false);
      }     
    }
    
    public void stateChanged(ChangeEvent arg0) {
      control.setValue(getValue());         
    }        
  }
  
  class BooleanCheckBox extends JCheckBox implements ChangeListener {
    BooleanControl control;
        
    public BooleanCheckBox() {
      this(null, null);
    }
    
    public BooleanCheckBox(String text) {
      this(null, text);
    }
    
    public BooleanCheckBox(BooleanControl control) {
      this(control, null);
    }
    
    public BooleanCheckBox(BooleanControl control, String text) {
      super(text);

      setControl(control);
    }
    
    public void setControl(BooleanControl control) {
      this.control = control;
      
      removeChangeListener(this);
      
      if (control != null) {
        setSelected(control.getValue());
        addChangeListener(this);
        setEnabled(true);
      } else {
        setEnabled(false);
      }
    }

    public void stateChanged(ChangeEvent arg0) {
      control.setValue(isSelected());
    }
  }
  
  class LevelMeter extends JComponent {
    float level;
    
    Color darkGreen = new Color(0, 150, 0);
    Color green = new Color(0, 254, 0);
    Color darkYellow = new Color(150, 150, 0);
    Color yellow = new Color(254, 254, 0);
    Color darkRed = new Color(150, 0, 0);
    Color red = new Color(254, 0, 0);
    
    public LevelMeter() {
      level = 0;
    }
    
    public void setLevel(float level) {
      this.level = level;
      repaint();
    }
    
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      
      Insets insets = getInsets();
      
      int componentWidth = getWidth() - insets.left - insets.right;
      int componentHeight = getHeight() - insets.top - insets.bottom;
      
      int barWidth = Math.min(10, componentWidth);
      int barHeight = Math.round(componentHeight/20f);
      
      int levelHeight = Math.round(level*20);
      if (level < 0) {
        levelHeight = 0;
      } else if (level > 20) {
        levelHeight = 20;
      }
      
      int barGap = Math.round(componentHeight/200f);
      if (barGap == 0) {
        barGap = 1;
      } else if (barGap > 10) {
        barGap = 10;
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
