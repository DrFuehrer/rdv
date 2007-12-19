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

package org.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;

import org.rdv.DataViewer;
import org.rdv.util.AudioErrorListener;
import org.rdv.util.AudioPlayer;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;

/**
 * A panel to interface with the audio player.
 * 
 * @author Jason P. Hanley
 */
public class AudioPlayerPanel extends JPanel {

  /** serialization version identifier */
  private static final long serialVersionUID = -6314108138102440777L;

  /** the label for the panel title */
  private JLabel titleLabel;
  
  /** the play/pause button */
  private JButton playButton;
  
  /** the text field for the stream URL */
  private JTextField streamURLField;
  
  /**
   * Creates the audio player panel.
   */
  public AudioPlayerPanel() {
    initComponents();
    initEvents();
  }
  
  /**
   * Initializes the UI components.
   */
  private void initComponents() {
    setBorder(null);
    setLayout(new BorderLayout());
        
    JPanel p = new JPanel();
    p.setBorder(new EmptyBorder(5,5,5,5));
    p.setLayout(new BorderLayout(5, 5));
    p.setBackground(Color.white);

    playButton = new JButton(DataViewer.getIcon("icons/audio_play.gif"));
    playButton.setOpaque(false);
    playButton.setBorder(null);
    playButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        togglePlay();
      }
    });
    p.add(playButton, BorderLayout.WEST);
   
    // popup menu for URL text field
    JPopupMenu popupMenu = new JPopupMenu();

    final JMenuItem pasteMenuItem = new JMenuItem("Paste");
    pasteMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        TransferHandler.getPasteAction().actionPerformed(
            new ActionEvent(streamURLField, ae.getID(), ae
                .getActionCommand(), ae.getWhen(), ae
                .getModifiers()));
      }
    });
    popupMenu.add(pasteMenuItem);
    
    streamURLField = new JTextField();
    streamURLField.setBorder(null);
    streamURLField.setComponentPopupMenu(popupMenu);
    streamURLField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        updateURL();
      }      
    });
    p.add(streamURLField, BorderLayout.CENTER);
    
    titleLabel = new JLabel(
        "Audio Player",
        DataViewer.getIcon("icons/audio.gif"),
        SwingConstants.LEADING);
    
    SimpleInternalFrame sif = new SimpleInternalFrame(
        titleLabel,
        null,
        p);

    add(sif, BorderLayout.CENTER);
  }
  
  /**
   * Initializes the events.
   */
  private void initEvents() {
    AudioPlayer audioPlayer = AudioPlayer.getInstance();
    
    audioPlayer.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(final PropertyChangeEvent pce) {
        if (SwingUtilities.isEventDispatchThread()) {
          handlePropertyChange(pce);
        } else {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              handlePropertyChange(pce);
            }
          });
        }
      }      
    });
    
    audioPlayer.addAudioErrorListener(new AudioErrorListener() {
      public void audioError(Exception e) {
        e.printStackTrace();
        
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            handleError();
          }
        });
      }
    });
  }
  
  /**
   * Called when the play button is pressed. This will toggle the playing state
   * of the current stream. If the stream URL in the text field has changed, it
   * will update the audio play URL and try and play the stream.
   */
  private void togglePlay() {
    URL streamURL;
    try { streamURL = new URL(streamURLField.getText()); } catch (MalformedURLException e) {
      titleLabel.setText("<html><b>Error in stream URL</b> - Audio Player</html>");
      return;          
    }

    AudioPlayer audioPlayer = AudioPlayer.getInstance();
    
    if (streamURL.equals(audioPlayer.getURL())) {
      audioPlayer.setPlaying(!audioPlayer.isPlaying());
    } else {
      audioPlayer.setURL(streamURL);
    }    
  }
  
  /**
   * Called when the text field control has an action to update the audio player
   * URL.
   */
  private void updateURL() {
    URL streamURL;
    try { streamURL = new URL(streamURLField.getText()); } catch (MalformedURLException e) {
      titleLabel.setText("Error in stream URL - Audio Player");
      return;
    }
    
    AudioPlayer audioPlayer = AudioPlayer.getInstance();
    audioPlayer.setURL(streamURL);
  }
  
  /**
   * Called when a property of the audio player changes. This will update the UI
   * to reflect the current state of the audio player.
   * 
   * @param pce  the property change event
   */
  private void handlePropertyChange(PropertyChangeEvent pce) {
    if (pce.getPropertyName().equals("playing")) {
      boolean playing = (Boolean)pce.getNewValue();
      if (playing) {
        playButton.setIcon(DataViewer.getIcon("icons/pause.gif"));
        
        if (!isVisible()) {
          setVisible(true);
        }
      } else {
        playButton.setIcon(DataViewer.getIcon("icons/audio_play.gif"));
      }
    } else if (pce.getPropertyName().equals("url")) {
      URL url = (URL)pce.getNewValue();
      if (url != null) {
        streamURLField.setText(url.toString());
      } else {
        streamURLField.setText(null);
      }
    } else if (pce.getPropertyName().equals("title")) {
      String title = (String)pce.getNewValue();
      if (title != null && title.length() > 0) {
        titleLabel.setText("<html><b>" + title + "</b> - Audio Player</html>");
      } else {
        titleLabel.setText("Audio Player");
      }
    }
  }
  
  /**
   * Called when an error event is received from the audio player. This will
   * display the error in the UI.
   */
  private void handleError() {
    if (!isVisible()) {
      setVisible(true);
    }

    titleLabel.setText("<html><b>Error playing stream</b> - Audio Player</html>");    
  }
}