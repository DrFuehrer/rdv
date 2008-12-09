<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" />

  <xsl:variable name="tab"><xsl:text>&#x09;</xsl:text></xsl:variable>

  <xsl:template match="OpenSeesOutput">
    <xsl:value-of select="TimeOutput/ResponseType" />

    <xsl:apply-templates select="NodeOutput"/>
    <xsl:apply-templates select="ElementOutput"/>
    <xsl:apply-templates select="DriftOutput"/>
    <xsl:apply-templates select="ElementOutput/GaussPointOutput/SectionOutput"/>
    <xsl:apply-templates select="ElementOutput/GaussPointOutput/NdMaterialOutput"/>

  </xsl:template>


  <xsl:template match="NodeOutput">
    <xsl:for-each select="./ResponseType">
      <xsl:value-of select="$tab"/>Node<xsl:value-of select="parent::NodeOutput/@nodeTag" />-<xsl:value-of select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="ElementOutput">
    <xsl:for-each select="./ResponseType">
      <xsl:value-of select="$tab"/>
      <xsl:value-of select="parent::ElementOutput/@eleTag" /><xsl:value-of select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="DriftOutput">
    <xsl:for-each select="./ResponseType">
      <xsl:value-of select="$tab"/>
      <xsl:value-of select="parent::DriftOutput/@node1" /><xsl:value-of select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="ElementOutput/GaussPointOutput/SectionOutput">
    <xsl:for-each select="./ResponseType">
      <xsl:value-of select="$tab"/>
      <xsl:value-of select="../../../@eleTag" /><xsl:value-of select="parent::SectionOutput/@secTag" /><xsl:value-of select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="ElementOutput/GaussPointOutput/NdMaterialOutput">
    <xsl:for-each select="./ResponseType">
      <xsl:value-of select="$tab"/>
      <xsl:value-of select="parent::SectionOutput/@matTag" /><xsl:value-of select="."/>
    </xsl:for-each>
  </xsl:template>


</xsl:stylesheet> 
