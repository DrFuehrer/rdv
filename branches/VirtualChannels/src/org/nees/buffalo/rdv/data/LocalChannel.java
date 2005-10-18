/*
 * Copyright (c) 2005 University at Buffalo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  o The above copyright notice and this permission notice shall be included
 *    in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.nees.buffalo.rdv.data;

import javax.swing.JComponent;

import org.nees.buffalo.rdv.rbnb.RBNBController;

import com.rbnb.sapi.ChannelMap;

/**
 * A <code>Channel</code> that derives it data locally. This data can be based
 * off a remote channel or purely local.
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public interface LocalChannel extends Channel {

  /**
   * Make the channel ready for use. The channel must have a default
   * configuration that is usable (can accept data) but not neccessarily do
   * anything.
   * 
   * @param channelName     the name of this channel
   * @param rbnbController  the interface to remote data
   */
  public void initialize(String channelName, RBNBController rbnbController);
  
  /**
   * Configure this channel with this name. The existing channel metadata is
   * provided. A <code>JComponent</code> may be returned to expose additional
   * configuration UI elements. 
   * 
   * @param channelName    the channel name for this channel
   * @parm rbnbController  the rbnb controller for interacting with the server
   * @return               a component with a configuration options (or null)
   * @since                1.3
   */
  public JComponent getConfigurationComponent();
  
  /**
   * When this method is called, the configuration has concluded. Update the
   * configuration of this channel with changes made in the configuration UI
   * component.
   *
   * @since  1.3
   */
  public void applyConfiguration();
  
  /**
   * Discard any configuration changes made to the configuration UI component.
   *
   * @since  1.3
   */
  public void discardConfiguration();
  
  /**
   * Fill the <code>ChannelMap</code> with the channel metadata.
   * 
   * @param cmap  the channel map to populate with channel metadata
   * @since       1.3
   */
  public void updateMetadata(ChannelMap cmap);
  
  /**
   * Fill the <code>ChannelMap</code> with the channel data between the start
   * and end times.
   * 
   * @param cmap      the channel map to put data into
   * @parm startTime  the start time (inclusive)
   * @parm endTime    the end time (exclusive)
   * @since           1.3
   */
  public void updateData(ChannelMap cmap, double startTime, double EndTime);
  
  /**
   * This method is called when the channel is no longer needed. This signifies
   * the end of the life cycle for the object.
   * 
   * @since  1.3
   */
  public void dispose();
}
