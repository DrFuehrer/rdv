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

package org.nees.video;

import java.io.IOException;

import org.nees.windows.Registry;

/**
 * A class to launch an instance of VLC.
 * 
 * @author Jason P. Hanley
 */
public class VLCLauncher {
  /** the default command to execute vlc */
  private static final String DEFAULT_VLC_COMMAND = "vlc";
  
  /** the Windows registry key where the VLC path is stored */
  private static final String VLC_REGISTRY_KEY = "HKEY_LOCAL_MACHINE\\SOFTWARE\\VideoLAN\\VLC";
  
  /** the default path for VLC on Windows */
  private static final String DEFAULT_VLC_PATH_WINDOWS = "c:\\Program Files\\VideoLAN\\VLC\\vlc.exe";

  /** the default path for VLC on Mac */
  private static final String DEFAULT_VLC_PATH_MAC = "open /Applications/VLC.app";

  /**
   * Launch VLC.
   * 
   * @throws IOException  if there is an error starting VLC
   */
  public static Process launchVLC() throws IOException {
    return launchVLC(null);
  }
  
  /**
   * Launch VLC with the mrl.
   * 
   * @param mrl           the mrl to load
   * @throws IOException  if there is an error starting VLC
   */
  public static Process launchVLC(String mrl) throws IOException {
    String vlcPath = null;
    
    String osName = System.getProperty("os.name");
    if (osName.startsWith("Windows")) {
      try {
        vlcPath = Registry.readString(VLC_REGISTRY_KEY);
      } catch (IOException e) {
        e.printStackTrace();
      }
      
      if (vlcPath == null) {
        vlcPath = DEFAULT_VLC_PATH_WINDOWS;
      }
    } else if (osName.startsWith("Mac")) {
      vlcPath = DEFAULT_VLC_PATH_MAC;
    } else {
      vlcPath = DEFAULT_VLC_COMMAND;      
    }
    
    String vlcCommand = vlcPath;
    if (mrl != null && mrl.trim().length() > 0) {
      vlcCommand += " \"" + mrl + "\"";
    }
    
    return Runtime.getRuntime().exec(vlcCommand);
  }
}