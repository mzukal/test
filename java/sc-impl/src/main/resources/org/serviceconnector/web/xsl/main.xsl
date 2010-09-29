<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="template.xsl"/>
    <xsl:template name="sc_content">
      <div class="sc_table" style="width:800px;">
        <div class="sc_table_title">
           System Info
        </div>             
        <table border="0" class="sc_table" cellspacing="0" cellpadding="0">
          <xsl:apply-templates select="$body/system/info/*"/>
        </table>
      </div>
    </xsl:template>
    <xsl:template match="system/info/*">
      <xsl:if test="position() mod 2 = 0">
	     <tr class="sc_table_even" onmouseover="javascript:setStyleOver(this)" onmouseout="javascript:setStyleOut(this)">
	        <xsl:call-template name="service_row"/>
	     </tr>	    
	  </xsl:if>
	  <xsl:if test="position() mod 2 != 0">
	     <tr class="sc_table_odd" onmouseover="javascript:setStyleOver(this)" onmouseout="javascript:setStyleOut(this)">	    
	        <xsl:call-template name="service_row"/>
	     </tr>	    
	  </xsl:if>
	</xsl:template>
	<xsl:template name="service_row">
	    <td class="sc_table"><xsl:value-of select="local-name()"/></td>
	    <td class="sc_table">
	      <xsl:if test="local-name() = 'configFileName'">
	         <a href="./resource?name={.}"><xsl:value-of select="."/></a>
	      </xsl:if>
	      <xsl:if test="local-name() != 'configFileName'">
	         <xsl:value-of select="."/>
	      </xsl:if>
	    </td>	
	</xsl:template>
</xsl:stylesheet>
