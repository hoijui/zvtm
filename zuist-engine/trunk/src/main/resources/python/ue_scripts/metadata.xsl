<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- $Id$-->
<xsl:output method="xml" indent="yes" encoding="UTF-8"
            cdata-section-elements=""/>

<xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable><xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>

<!--<xsl:variable name="year"><xsl:value-of select="substring(//conference_date/start_date/text(), 7)"/></xsl:variable>-->
<xsl:variable name="year"><xsl:value-of select="//copyright_year/text()"/></xsl:variable>

<xsl:template match="/">
  <proceedings>
    <xsl:attribute name="year">
      <xsl:value-of select="$year"/>
    </xsl:attribute>
    <xsl:attribute name="startDate">
      <xsl:value-of select="//conference_date/start_date/text()"/>
    </xsl:attribute>
    <xsl:attribute name="endDate">
      <xsl:value-of select="//conference_date/start_date/text()"/>
    </xsl:attribute>
    <location>
      <xsl:copy-of select="//conference_loc/city"/>
      <xsl:copy-of select="//conference_loc/state"/>
      <xsl:copy-of select="//conference_loc/country"/>
    </location>
    <chairs>
      <xsl:for-each select="//chair_editor/ch_ed[role/text() = 'General Chair' or role/text() = 'Chairman']">
        <general_chair>
          <xsl:copy-of select="first_name"/>
          <xsl:copy-of select="middle_name"/>
          <xsl:copy-of select="last_name"/>
          <xsl:copy-of select="suffix"/>
        </general_chair>
      </xsl:for-each>
      <xsl:for-each select="//chair_editor/ch_ed[role/text() = 'Program Chair']">
        <program_chair>
          <xsl:copy-of select="first_name"/>
          <xsl:copy-of select="middle_name"/>
          <xsl:copy-of select="last_name"/>
          <xsl:copy-of select="suffix"/>
        </program_chair>
      </xsl:for-each>
    </chairs>
    <xsl:for-each select="//article_rec">
      <article>
        <xsl:attribute name="doi">
          <xsl:value-of select="doi_number"/>
        </xsl:attribute>
        <xsl:attribute name="session">
          <xsl:value-of select="ancestor::section[section_type/text() = 'SESSION']/section_title/text()" />
        </xsl:attribute>
        <xsl:apply-templates select="."/>
      </article>
    </xsl:for-each>
  </proceedings>
</xsl:template>

<xsl:template match="article_rec">
  <firstPage><xsl:value-of select="page_from/text()"/></firstPage>
  <lastPage><xsl:value-of select="page_to/text()"/></lastPage>
  <title><xsl:value-of select="title/text()"/></title>
  <subtitle><xsl:value-of select="subtitle/text()"/></subtitle>
  <xsl:copy-of select="abstract"/>
  <xsl:copy-of select="keywords"/>
  <authors>
    <xsl:for-each select="authors/au">
      <xsl:apply-templates select="."/>
    </xsl:for-each>
  </authors>
</xsl:template>

<xsl:template match="au">
  <author>
    <xsl:copy-of select="first_name"/>
    <xsl:copy-of select="middle_name"/>
    <xsl:copy-of select="last_name"/>
    <xsl:copy-of select="suffix"/>
  </author>
</xsl:template>

<xsl:template match="text()|@*">
  <xsl:value-of select="."/>
</xsl:template>


</xsl:stylesheet>
