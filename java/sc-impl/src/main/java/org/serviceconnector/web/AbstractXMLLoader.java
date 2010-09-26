/*
 *       Copyright � 2010 STABILIT Informatik AG, Switzerland                  *
 *                                                                             *
 *  Licensed under the Apache License, Version 2.0 (the "License");            *
 *  you may not use this file except in compliance with the License.           *
 *  You may obtain a copy of the License at                                    *
 *                                                                             *
 *  http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                             *
 *  Unless required by applicable law or agreed to in writing, software        *
 *  distributed under the License is distributed on an "AS IS" BASIS,          *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 *  See the License for the specific language governing permissions and        *
 *  limitations under the License.                                             *
 */
package org.serviceconnector.web;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.serviceconnector.SCVersion;
import org.serviceconnector.util.DateTimeUtility;


// TODO: Auto-generated Javadoc
/**
 * The Class AbstractXMLLoader.
 */
public abstract class AbstractXMLLoader implements IXMLLoader {

	private Map<String, String> metaMap;
	private List<Map<String, String>> metaMapList;
	
	/**
	 * Instantiates a new abstract xml loader.
	 */
	public AbstractXMLLoader() {
		this.metaMap = new HashMap<String, String>();
		this.metaMapList = new ArrayList<Map<String, String>>();
	}

	
	/** {@inheritDoc} */
	@Override
	public void addMeta(String name, String value) {
		this.metaMap.put(name, value);
	}

	/** {@inheritDoc} */
	@Override
	public void addMeta(Map<String, String> map) {
		this.metaMapList.add(map);
	}

	/**
	 * Load body.
	 * 
	 * @param writer
	 *            the writer
	 */
	public abstract void loadBody(XMLStreamWriter writer) throws Exception;

	/** {@inheritDoc} */
	@Override
	public final void load(IWebRequest request, OutputStream os)
			throws Exception {
		IWebSession webSession = request.getSession(false);
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(os);
		writer.writeStartDocument();
		writer.writeStartElement("sc-web");
		writer.writeStartElement("head");
		writer.writeStartElement("meta");
		writer.writeAttribute("creation",
				DateTimeUtility.getCurrentTimeZoneMillis());
		writer.writeEndElement(); // close meta tag
		writer.writeStartElement("meta");
		writer.writeAttribute("scversion", SCVersion.CURRENT.toString());
		writer.writeEndElement(); // close meta tag
		if (webSession != null) {
			writer.writeStartElement("meta");
			writer.writeAttribute("jsessionid", webSession.getSessionId());
			writer.writeEndElement(); // close meta tag
		}
		for (Entry<String, String> entry : this.metaMap.entrySet()) {
			writer.writeStartElement("meta");
			writer.writeAttribute(entry.getKey(), entry.getValue());
			writer.writeEndElement(); // close meta tag             		
		}
		for (Map<String, String> map : this.metaMapList) {			
			writer.writeStartElement("meta");
			for (Entry<String, String> entry : map.entrySet()) {
			   writer.writeAttribute(entry.getKey(), entry.getValue());
			}
			writer.writeEndElement(); // close meta tag             		
		}
		// write any query params back
		writer.writeStartElement("query");
		Map<String, List<String>> parameterMap = request.getParameterMap();
		for (Entry<String, List<String>> parameter : parameterMap.entrySet()) {
			String name = parameter.getKey();
			if ("password".equals(name)) {
				continue;
			}
			List<String> values = parameter.getValue();
			for (String value : values) {
				writer.writeStartElement("param");
				writer.writeAttribute(name, value);
				writer.writeEndElement(); // close param             					
			}
 		}
		writer.writeEndElement(); // close query             					
		writer.writeStartElement("body");
		this.loadBody(writer);
		writer.writeEndElement(); // close body tag
		writer.writeEndElement(); // close root tag sc-web
		writer.writeEndDocument();
		writer.close();
	}

}