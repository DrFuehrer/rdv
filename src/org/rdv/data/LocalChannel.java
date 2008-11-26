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

package org.rdv.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.matheclipse.parser.client.Parser;
import org.matheclipse.parser.client.SyntaxError;
import org.matheclipse.parser.client.ast.ASTNode;
import org.matheclipse.parser.client.ast.FunctionNode;
import org.matheclipse.parser.client.eval.DoubleEvaluator;
import org.matheclipse.parser.client.eval.DoubleVariable;
import org.rdv.rbnb.DataListener;
import org.rdv.rbnb.RBNBController;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.ChannelTree.Node;

/**
 * A class for a <code>LocalChannel</code> that is calculated from server
 * channels. This channel is defined by a formula with variables referencing
 * server channels.
 * 
 * @author Jason P. Hanley
 */
public class LocalChannel extends Channel {

  /** an array of variable channels */
  private String[] variableChannels;
  
  /** an array of variable names */
  private String[] variableNames;
  
  /** the formula for this channel */
  private String formula;
  
  /** a dummy data listener */
  private DataListener dataListener;
  
  /** the expression parser */
  private final Parser p;
  
  /** the parsed expression node */
  private ASTNode node;
  
  /** the engine to evaluate the expression */
  private final DoubleEvaluator engine;
  
  /** the variable objects for the evaluation engine */
  private DoubleVariable[] variableObjects;
  
  /** the channel map indexes for the server channels */
  private int[] serverIndexes;
  
  /** the data types for the server channels */
  private int[] serverDataTypes;
  
  /** the metadata XML for the metadata channel map */
  private final static String metadataXml = 
    "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + 
    "<!DOCTYPE rbnb>\n" +
    "<rbnb>\n" +
    "  <size>8</size>\n" +
    "  <mime>application/octet-stream</mime>\n" +
    "</rbnb>\n";
  
  /** the mime type for the metadata XML */
  private final static String metadataXMLType = "text/xml";
  
  /**
   * Creates a local channel with a <code>name</code>, <code>unit</code>
   * (optional), map of <code>variables</code> representing server channels, and
   * a <code>formula</code>.
   * 
   * @param name       the name of the channel
   * @param unit       the unit for the channel
   * @param variables  a map of variables names to server channels
   * @param formula    the expression to be evaluated
   * @see #setFormula(String, Map)
   */
  public LocalChannel(String name, String unit, Map<String,String> variables, String formula) {
    super(name, unit);
    
    // create a dummy data listener
    dataListener = new DataListener() {
      public void postData(ChannelMap channelMap) {}
    };
    
    p = new Parser();
    
    engine = new DoubleEvaluator();
    
    setFormula(formula, variables);
    
    setMetadata();
  }
  
  /**
   * Gets the formula for this channel.
   * 
   * @return  the formula for the channel
   */
  public String getFormula() {
    return formula;
  }

  /**
   * Sets the <code>formula</code> and <code>variables</code> map for this
   * channel.
   * 
   * @param formula               the formula for this channel
   * @param variables             a map of variables names to server channels
   * @throws ArithmeticException  if the formula can't be evaluated
   * @throws SyntaxError          if the formula can't be parsed
   */
  private void setFormula(String formula, Map<String,String> variables) throws ArithmeticException, SyntaxError {
    // try and parse the formula
    node = p.parse(formula);
    if (node instanceof FunctionNode) {
      node = engine.optimizeFunction((FunctionNode) node);
    }
    
    // try and evaluate the formula
    DoubleVariable vd = new DoubleVariable(0);
    for (String variable : variables.keySet()) {
      engine.defineVariable(variable, vd);
    }
    engine.evaluateNode(node);
    
    // set the formula
    this.formula = formula;
    
    // create the various arrays for the variables
    variableNames = new String[variables.size()];
    variableChannels = new String[variables.size()];
    variableObjects = new DoubleVariable[variables.size()];
    serverIndexes = new int[variables.size()];
    serverDataTypes = new int[variables.size()];
    
    // populate the variables arrays for the variables
    int i=0;
    Iterator<String> it = variables.keySet().iterator();
    while (it.hasNext()) {
      variableNames[i] = it.next();
      variableChannels[i] = variables.get(variableNames[i]);
      
      variableObjects[i] = new DoubleVariable(0);
      engine.defineVariable(variableNames[i], variableObjects[i]);

      i++;
    }
    
    // subscribe to the server channels so the server gets their data
    List<String> serverChannels = Arrays.asList(variableChannels);
    RBNBController.getInstance().subscribe(serverChannels, dataListener);
  }
  
  /**
   * Sets the local, formula, and variables metadata entries.
   */
  private void setMetadata() {
    // set the mime type
    setMetadata("mime", "application/octet-stream");
    
    // set the size of the data
    setMetadata("size", "8");
    
    // set start and duration metadata to default values
    setMetadata("start", "0");
    setMetadata("duration", "0");
    
    // a key to signify that this is a local channel
    setMetadata("local");

    // set the formula
    setMetadata("formula", formula);

    // set the variable names and channels in the form
    //   name1:channel1|name2:channel2
    String variablesString = "";
    for (int i=0; i<variableNames.length; i++) {
      variablesString += variableNames[i] + ":" + variableChannels[i];
      if (i != variableNames.length-1) {
        variablesString += "|";
      }
    }
    setMetadata("variables", variablesString);
  }

  /**
   * Update the <code>channelMap</code> with the metadata for this channel.
   * 
   * @param channelMap         the channel map to put this channels metadata in
   * @param serverChannelTree  the channel tree of server channels
   * @throws SAPIException     if there is an error adding the metadata
   */
  public void updateMetadata(ChannelMap channelMap, ChannelTree serverChannelTree) throws SAPIException {
    // see if the first server channel exists
    Node serverNode = serverChannelTree.findNode(variableChannels[0]);
    if (node == null) {
      return;
    }
    
    // add the channel metadata to the channel map
    int index = channelMap.Add(getName());
    channelMap.PutTime(serverNode.getStart(), serverNode.getDuration());
    channelMap.PutDataAsString(index, metadataXml);
    channelMap.PutMime(index, metadataXMLType);
    
    // update start and duration metadata
    setMetadata("start", Double.toString(serverNode.getStart()));
    setMetadata("duration", Double.toString(serverNode.getDuration()));
  }
  
  /**
   * Update the <code>channelMap</code> with the data for this channel. If there
   * is no data to post for this channel, the <code>channelMap</code> will not
   * be updated. For data to be posted, the data points for every variable
   * channel must have the same timestamp. This will always put the data with a
   * <code>double</code> data type, regardless of the input data type.
   * 
   * @param channelMap              the channel map to put this channels data in
   * @throws SAPIException          if there is an error getting data from or
   *                                adding data to the <code>channelMap</code>
   * @throws LocalChannelException  if a variable channel doesn't have a numeric
   *                                data type
   */
  public void updateData(ChannelMap channelMap) throws SAPIException, LocalChannelException {
    // go through the channel map to make sure we have data for all the variable
    // channels and they have the same number of points
    int i=0;
    int points = 0;
    for (String channel : variableChannels) {
      // get the index for the channel
      serverIndexes[i] = channelMap.GetIndex(channel);
      if (serverIndexes[i] < 0) {
        return;
      }
      
      // get the data type for the channel
      serverDataTypes[i] = channelMap.GetType(serverIndexes[i]);
      
      // check the number of data points for the channel
      if (points == 0) {
        points = channelMap.GetTimes(serverIndexes[i]).length;
      } else if (channelMap.GetTimes(serverIndexes[i]).length != points) {
        return;
      }
      
      i++;
    }
    
    // add the channel to the channel map
    int index = channelMap.Add(getName());
    channelMap.PutMime(index, "application/octet-stream");
    channelMap.PutTimeRef(channelMap, serverIndexes[0]);
    
    // evaluate the function at each data point and add the data to the channel
    // map
    double[] localData = new double[points];
    double value;
    for (i=0; i<points; i++) {
      for (int j=0; j<variableNames.length; j++) {
        // get the data as a double
        switch (serverDataTypes[j]) {
        case ChannelMap.TYPE_INT8:
          value = channelMap.GetDataAsInt8(serverIndexes[j])[i];
          break;
        case ChannelMap.TYPE_INT16:
          value = channelMap.GetDataAsInt16(serverIndexes[j])[i];
          break;
        case ChannelMap.TYPE_INT32:
          value = channelMap.GetDataAsInt32(serverIndexes[j])[i];
          break;
        case ChannelMap.TYPE_INT64:
          value = channelMap.GetDataAsInt64(serverIndexes[j])[i];
          break;
        case ChannelMap.TYPE_FLOAT32:
          value = channelMap.GetDataAsFloat32(serverIndexes[j])[i];
          break;
        case ChannelMap.TYPE_FLOAT64:
          value = channelMap.GetDataAsFloat64(serverIndexes[j])[i];
          break;
        default:
          throw new LocalChannelException("Invalid data type for channel " +
              variableChannels[j] + ": " +
              channelMap.TypeName(serverDataTypes[j]));
        }
        
        // set the value in the variable object for the expression evaluator
        variableObjects[j].setValue(value);
      }
      
      // evaluate the expression
      localData[i] = engine.evaluateNode(node);
    }
    
    // put the calculated channel data
    channelMap.PutDataAsFloat64(index, localData);
  }
  
  /**
   * Called when this local channel will no longer be used. This unsubscribes it
   * from the server channels it was subscribed to.
   */
  public void dispose() {
    List<String> serverChannels = Arrays.asList(variableChannels);
    RBNBController.getInstance().unsubscribe(serverChannels, dataListener);
  }

}