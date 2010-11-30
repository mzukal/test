<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="template.xsl"/>
    <xsl:template name="sc_content">
      <div class="sc_table max_width">
        <div class="sc_table_title">
          SC Maintenance
        </div>
        <div class="sc_separator">&#160;</div>
        <div>
           <div id="sc_terminate"><input class="sc_form_button" style="margin:10px;" name="Terminate SC" type="button" value="Terminate SC" onclick="javascript:terminateSC()"></input></div> 
           <div id="sc_cache_reset"><input class="sc_form_button" style="margin:10px;" name="Terminate SC" type="button" value="Reset Cache" onclick="javascript:resetCache()"></input></div> 
        </div>
      </div>
    </xsl:template>
	<xsl:template name="sc_menu_left"><xsl:call-template name="menu_separator"/><div class="sc_menu_item" onmouseover="javascript:setStyleOver(this)" onmouseout="javascript:setStyleOut(this)"><a class="sc_menu_item" href="./maintenance">Maintenance</a></div></xsl:template>
</xsl:stylesheet>
