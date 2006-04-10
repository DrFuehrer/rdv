package org.nees.rbnb.marker;

/**
 * Provides a static string version of the XSLT stylesheets for the transformations between
 * the XML representation of Events and Properties
 * 
 * @author Terry E. Weymouth <weymouth@umich.edu>
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * @since 050916
 * Perforce RCS info:
 * $Id: $
 * $Header: $
 * $Date: $
 * $Change: $
 * $File: $
 * $Revision: $
 * $Author: $
 *
 * Copyright (c) 2005, NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * All rights reserved. See full notice in the source, at the end of the file.
 * 
 */
public class Stylesheets {
  
  public static final String PROPERTIES_TO_EVENTS_XSL =
  "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \n" +
  "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"> \n" +
  "<xsl:output method='xml' version='1.0' encoding='UTF-8' indent='yes'/> \n" +
  " \n" +
  "<xsl:template match=\"/properties\"> \n" +
  "  <event> \n" +
  "  <xsl:for-each select=\"entry\"> \n" +
  "    <xsl:choose> \n" +
  "      <xsl:when test=\"@key = 'label'\"> \n" +
  "        <label><xsl:value-of select=\".\" /></label> \n" +
  "      </xsl:when> \n" +
  "      <xsl:when test=\"@key = 'timestamp'\"> \n" +
  "        <timestamp><xsl:value-of select=\".\" /></timestamp> \n" +
  "      </xsl:when> \n" +
  "      <xsl:when test=\"@key = 'type'\"> \n" +
  "        <type><xsl:value-of select=\".\" /></type> \n" +
  "      </xsl:when> \n" +
  "      <xsl:when test=\"@key = 'content'\"> \n" +
  "        <content><xsl:value-of select=\".\" /></content> \n" +
  "      </xsl:when> \n" +
  "      <xsl:when test=\"@key = 'source'\"> \n" +
  "        <source><xsl:value-of select=\".\" /></source> \n" +
  "      </xsl:when> \n" +
  "    </xsl:choose> \n" +
  "  </xsl:for-each> \n" +
  "  </event> \n" +
  "</xsl:template> \n" +
  "</xsl:stylesheet> \n";
  
  public static final String EVENTS_TO_PROPERTIES_XSL =
    "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \n" +
    "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"> \n" +
    "<xsl:output method='xml' version='1.0' encoding='UTF-8' indent='yes'" +
    "     doctype-system=\"http://java.sun.com/dtd/properties.dtd\" /> \n" +
    " \n" +
    "<xsl:template match=\"/event\">\n" +
    "<properties>\n" +
    "    <xsl:apply-templates />\n" +
    "</properties>\n" +
    "</xsl:template>\n" +
    "\n" +
    "<xsl:template match=\"label\">\n" +
    "  <entry key=\"label\"><xsl:value-of select=\".\"/></entry>\n" +
    "</xsl:template>\n" +
    "\n" +
    "<xsl:template match=\"timestamp\">\n" +
    "  <entry key=\"timestamp\"><xsl:value-of select=\".\"/></entry>\n" +
    "</xsl:template>\n" +
    "\n" +
    "<xsl:template match=\"type\">\n" +
    "  <entry key=\"type\"><xsl:value-of select=\".\"/></entry>\n" +
    "</xsl:template>\n" +
    "\n" +
    "<xsl:template match=\"content\">\n" +
    "  <entry key=\"content\"><xsl:value-of select=\".\"/></entry>\n" +
    "</xsl:template>\n" +
    "\n" +
    "<xsl:template match=\"source\">\n" +
    "  <entry key=\"source\"><xsl:value-of select=\".\"/></entry>\n" +
    "</xsl:template>\n" +
    "\n" +
    "</xsl:stylesheet> \n";
}

/* Copyright Notice:
 *
 * Copyright (c) 2005, NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the 
 * documentation and/or other materials provided with the distribution.
 *    * Neither the name of the San Diego Supercomputer Center nor the names of
 * its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
