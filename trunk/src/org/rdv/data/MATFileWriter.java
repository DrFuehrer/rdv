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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.util.Date;
import java.util.List;

/**
 * A class to write an array of numeric data to a MATLAB MAT file. This will
 * create a 2 dimensional array with the name 'data' containing all the data.
 * 
 * @author  Jason P. Hanley
 * @see     DataFileWriter
 * @see     <a href="http://www.mathworks.com/access/helpdesk/help/pdf_doc/matlab/matfile_format.pdf">MAT-File Format</a>
 */
public class MATFileWriter implements DataFileWriter {
  
  /** the header for the MAT file */
  private static final String HEADER_TEXT_FIELD = "MATLAB 5.0 MAT-file, Platform: Java, Created on: ";
  
  /** the file to write to */
  private File file;

  /** the writer for the file */
  private RandomAccessFile fileWriter;

  /** a buffer for temporary data and byte-swapping */
  private ByteBuffer bb;
  
  /** the number of columns in the array */
  private int cols;
  
  /** the position in the file where the data starts */
  private long dataStartPosition;
  
  /**
   * Creates a MAT file writer.
   */
  public MATFileWriter() {
    bb = ByteBuffer.allocate(1024);
    bb.order(ByteOrder.LITTLE_ENDIAN);
  }
  
  public void init(List<DataChannel> channels, double startTime, double endTime, File file) throws IOException {
    this.file = file;
    
    fileWriter = new RandomAccessFile(file, "rw");
    fileWriter.setLength(0);

    // header text field (117 bytes)
    fileWriter.writeBytes(HEADER_TEXT_FIELD);
    fileWriter.writeBytes(new Date().toString());
    while (fileWriter.getFilePointer() < 124) {
      fileWriter.write(' ');
    }
    
    bb.clear();
    
    // version
    bb.putShort((short) 0x0100);
    
    // endian indicator
    bb.put("IM".getBytes());
    
    bb.flip();
    fileWriter.getChannel().write(bb);
    
    writeDoubleArrayHeader("data", channels.size());
  }
  
  public void writeSample(NumericDataSample sample) throws IOException {
    writeDoubleData(sample.getValues());
  }
  
  public void close() throws IOException {
    finishDoubleArray();
  }
  
  public void abort() {
    try {
      fileWriter.close();
      
      file.delete();
    } catch (Exception e) {}
  }
  
  /**
   * Writes the header for a data element.
   * 
   * @param dataType       the type of element
   * @param numberOfBytes  the number of bytes in the element
   * @throws IOException   if there is an error writing the header
   */
  private void writeDataElementHeader(DataTypes dataType, int numberOfBytes) throws IOException {
    bb.clear();
    
    // data type
    bb.putInt(dataType.getId());
    
    // number of bytes
    bb.putInt(numberOfBytes);
    
    bb.flip();
    fileWriter.getChannel().write(bb);
  }
  
  /**
   * Writes a data element with a byte array as data.
   * 
   * @param dataType      the type of data for the element
   * @param data          the data for the element
   * @throws IOException  if there is an error writing the element
   */
  private void writeDataElement(DataTypes dataType, byte[] data) throws IOException {
    writeDataElementHeader(dataType, data.length);
    
    bb.clear();
      
    // data
    bb.put(data);
    
    // pad
    if (data.length % 8 != 0) {
      int numberOfPadBytes = 8 - (data.length % 8);
      for (int i=0; i<numberOfPadBytes; i++) {
        bb.put((byte) 0);
      }
    }
    
    bb.flip();
    fileWriter.getChannel().write(bb);
  }
  
  /**
   * Writes a data element with an int array as data.
   * 
   * @param dataType      the type of data for the element
   * @param data          the data for the element
   * @throws IOException  if there is an error writing the element
   */
  private void writeDataElement(DataTypes dataType, int[] data) throws IOException {
    int numberOfBytes = dataType.getSize() * data.length;
    writeDataElementHeader(dataType, numberOfBytes);
    
    bb.clear();
    
    // data
    bb.asIntBuffer().put(data);
    bb.position(bb.position() + numberOfBytes);
    
    // pad
    if (data.length % 2 != 0) {
      bb.putInt(0);
    }
    
    bb.flip();
    fileWriter.getChannel().write(bb);
  }
  
  /**
   * Writes a data element with a string as the data.
   * 
   * @param data          the data for the element
   * @throws IOException  if there is an error writing the element
   */
  private void writeDataElement(String data) throws IOException {
    writeDataElement(DataTypes.miINT8, data.getBytes());
  }
  
  /**
   * Writes the header for a double array. This leaves a number of fields not
   * filled in correctly since they can only be computed if the size of the data
   * is known.
   * 
   * @param name          the name of the array
   * @param rows          the number of rows in the array
   * @throws IOException  if there is an error writing the array header
   * @see                 #finishDoubleArray()
   * @see                 #writeDoubleData(Number[])
   */
  private void writeDoubleArrayHeader(String name, int rows) throws IOException {
    writeDataElementHeader(DataTypes.miMATRIX, -1);
    
    // array flags
    int[] arrayFlags = new int[2];
    arrayFlags[0] = ArrayTypes.mxDOUBLE_CLASS.getID();
    writeDataElement(DataTypes.miUINT32, arrayFlags);
    
    // dimensions array
    int[] dimensionsArray = new int[2];
    dimensionsArray[0] = rows;
    dimensionsArray[1] = -1;
    writeDataElement(DataTypes.miINT32, dimensionsArray);
    
    // array name
    writeDataElement(name);
    
    // data element header
    writeDataElementHeader(DataTypes.miDOUBLE, -1);
    
    dataStartPosition = fileWriter.getFilePointer();
  }
  
  /**
   * Writes the data to the file as doubles.
   * 
   * @param data          the data to write.
   * @throws IOException  if there is an error writing the data
   */
  private void writeDoubleData(Number[] data) throws IOException {
    bb.clear();
    DoubleBuffer db = bb.asDoubleBuffer();
    for (Number n : data) {
      db.put(n.doubleValue());
    }
    bb.position(bb.position() + (8 * data.length));
    
    bb.flip();
    fileWriter.getChannel().write(bb);
    
    cols++;
  }
  
  /**
   * Finishes a double array. This goes back to the header and writes various
   * fields such as the length of the matrix, the number of columns and the size
   * of the data element.
   * 
   * @throws IOException  if there is an error writing to the file
   * @see                 #writeDoubleArrayHeader(String, int)
   */
  private void finishDoubleArray() throws IOException {
    int pos = (int) fileWriter.getFilePointer();
    
    // matrix size
    fileWriter.seek(128 + 4);
    bb.clear();
    bb.putInt(pos - 136);
    bb.flip();
    fileWriter.getChannel().write(bb);
    
    // number of array columns
    fileWriter.seek(128 + 8 + 16 + 12);
    bb.clear();
    bb.putInt(cols);
    bb.flip();
    fileWriter.getChannel().write(bb);    

    // number of bytes of data
    int numberOfDataBytes = (int) (pos - dataStartPosition);
    fileWriter.seek(dataStartPosition - 4);
    bb.clear();
    bb.putInt(numberOfDataBytes);
    bb.flip();
    fileWriter.getChannel().write(bb);    
    fileWriter.close();    
  }
  
  /**
   * An enum for the data types defined for a data element.
   */
  private enum DataTypes {
    miINT8        (1,  1),
    miUINT8       (2,  1),
    miINT16       (3,  2),
    miUINT16      (4,  2),
    miINT32       (5,  4),
    miUINT32      (6,  4),
    miSINGLE      (7,  4),
    miDOUBLE      (9,  8),
    miINT64      (12,  8),
    miUINT64     (13,  8),
    miMATRIX     (14, -1),
    miCOMPRESSED (15, -1),
    miUTF8       (16,  1),
    miUTF16      (17,  2),
    miUTF32      (18,  4);
    
    /** the id of the data type */
    private final int id;
    
    /** the size (in bytes) of the data type */
    private final int size;
    
    /**
     * Creates the data type.
     * 
     * @param id    the id of the data type
     * @param size  the size of the data type
     */
    private DataTypes(int id, int size) {
      this.id = id;
      this.size = size;
    }
    
    /**
     * Gets the id of the data type.
     * 
     * @return  the id of the data type
     */
    public int getId() {
      return id;
    }
    
    /**
     * Gets the size (in bytes) of the data type.
     * 
     * @return  the size of the data type
     */
    public int getSize() {
      return size;
    }
  }
  
  /**
   * An enum for the array types
   */
  private enum ArrayTypes {
    mxCELL_CLASS    (1),
    mxSTRUCT_CLASS  (2),
    mxOBJECT_CLASS  (3),
    mxCHAR_CLASS    (4),
    mxSPARSE_CLASS  (5),
    mxDOUBLE_CLASS  (6),
    mxSINGLE_CLASS  (7),
    mxINT8_CLASS    (8),
    mxUINT8_CLASS   (9),
    mxINT16_CLASS  (10),
    mxUINT16_CLASS (11),
    mxINT32_CLASS  (12),
    mxUINT32_CLASS (13);
    
    /** the id of the array type */
    private final byte id;
    
    /**
     * Creates the array type
     * 
     * @param id  the id of the array type
     */
    private ArrayTypes(int id) {
      this.id = (byte) id;
    }
    
    /**
     * Gets the id of the array type.
     * 
     * @return  the id of the array type
     */
    public byte getID() {
      return id;
    }
  }
  
}