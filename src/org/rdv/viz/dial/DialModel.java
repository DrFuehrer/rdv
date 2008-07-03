/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
 * Copyright (c) 2008 Palta Software
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

package org.rdv.viz.dial;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.jfree.data.Range;

/**
 * A model for a dial visualization.
 * 
 * @author  Jason P. Hanley
 * @see     DialPanel
 */
public class DialModel {
  
  /** the value of the dial */
  private Number value;
  
  /** the name of the dial */
  private String name;
  
  /** the unit for the value of the dial */
  private String unit;
  
  /** the range for the dial */
  private Range range;
  
  /** the warning threshold */
  private double warningThreshold;
  
  /** the critical threshold */
  private double criticalThreshold;
  
  /** a helper class for property change events */
  private PropertyChangeSupport propertyChangeSupport;
  
  /**
   * Creates a dial model with a null value, name, and unit. The initial range
   * will be from 0 to 1.
   */
  public DialModel() {
    range = new Range(0, 1);
    
    warningThreshold = 0.1;
    criticalThreshold = 0.05;
    
    propertyChangeSupport = new PropertyChangeSupport(this);
  }

  /**
   * Gets the value of the dial.
   * 
   * @return  the value of the dial
   * @see     #setValue(Number)
   */
  public Number getValue() {
    return value;
  }
  
  /**
   * Sets the value of the dial. This will fire a property change event if the
   * new value is different from the old value.
   * 
   * @param value  the new value of the dial
   * @see          #getValue()
   */
  public void setValue(Number value) {
    Number oldValue = this.value;
    this.value = value;
    
    propertyChangeSupport.firePropertyChange("value", oldValue, value);
  }
  
  /**
   * Gets the name of the dial.
   * 
   * @return  the name of the dial
   * @see     #setName(String)
   */
  public String getName() {
    return name;
  }
  
  /**
   * Sets the name of the dial. This will fire a property change event if the
   * new value is different from the old value.
   * 
   * @param name  the new name of the dial
   * @see         #getName()
   */
  public void setName(String name) {
    String oldName = this.name;
    this.name = name;
    
    propertyChangeSupport.firePropertyChange("name", oldName, name);
  }

  /**
   * Gets the unit for the value of the dial.
   * 
   * @return  the unit for the value of the dial
   * @see     #setUnit(String)
   */
  public String getUnit() {
    return unit;
  }

  /**
   * Sets the unit for the value of the dial. This will fire a property change
   * event if the new value is different from the old value.
   * 
   * @param unit  the new unit for the value of the dial
   * @see         #getUnit()
   */
  public void setUnit(String unit) {
    String oldUnit = this.unit;
    this.unit = unit;
    
    propertyChangeSupport.firePropertyChange("unit", oldUnit, unit);
  }
  
  /**
   * Gets the range for the dial.
   * 
   * @return  the range for the dial
   * @see     #setRange(Range)
   */
  public Range getRange() {
    return range;
  }

  /**
   * Sets the range for the dial. This will fire a property change event if the
   * new range is different from the old range.
   * 
   * @param range  the new minimum value for the dial
   * @see          #getRange()
   */
  public void setRange(Range range) {
    Range oldRange = this.range;
    this.range = range;
    
    propertyChangeSupport.firePropertyChange("range", oldRange, range);
  }
  
  /**
   * Gets the warning threshold for the dial.
   * 
   * @return  the warning threshold for the dial
   * @see     #setWarningThreshold(double)
   */
  public double getWarningThreshold() {
    return warningThreshold;
  }

  /**
   * Sets the warning threshold for the dial.
   * 
   * @param warningThreshold  the new warning threshold for the dial
   * @see                     #getWarningThreshold()
   */
  public void setWarningThreshold(double warningThreshold) {
    double oldWarningThreshold = this.warningThreshold;
    this.warningThreshold = warningThreshold;
    
    propertyChangeSupport.firePropertyChange("warningThreshold", oldWarningThreshold, warningThreshold);
  }

  /**
   * Gets the critical threshold for the dial.
   * 
   * @return  the critical threshold for the dial
   * @see     #setCriticalThreshold(double)
   */
  public double getCriticalThreshold() {
    return criticalThreshold;
  }

  /**
   * Sets the critical threshold for the dial
   * @param criticalThreshold  the new critical threshold for the dial
   * @see                      #getCriticalThreshold()
   */
  public void setCriticalThreshold(double criticalThreshold) {
    double oldCriticalThreshold = this.criticalThreshold;
    this.criticalThreshold = criticalThreshold;
    
    propertyChangeSupport.firePropertyChange("criticalThreshold", oldCriticalThreshold, criticalThreshold);
  }

  /**
   * Adds a PropertyChangeListener for the properties of this class.
   * 
   * @param listener  the listener to add
   * @see             #removePropertyChangeListener(PropertyChangeListener)
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
     propertyChangeSupport.addPropertyChangeListener(listener);
   }
  
  /**
   * Removes a PropertyChangeListener for the properties of this class.
   * 
   * @param listener  the listener to remove
   * @see             #addPropertyChangeListener(PropertyChangeListener)
   */
   public void removePropertyChangeListener(PropertyChangeListener listener) {
     propertyChangeSupport.removePropertyChangeListener(listener);
   }
  
}