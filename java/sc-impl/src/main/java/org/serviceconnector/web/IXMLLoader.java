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
import java.util.Map;

import org.serviceconnector.factory.IFactoryable;

// TODO: Auto-generated Javadoc
/**
 * The Interface IXMLLoader.
 */
public interface IXMLLoader extends IFactoryable{

	/**
	 * Load.
	 *
	 * @param request the request
	 * @param os the os
	 * @throws Exception 
	 */
	public abstract void load(IWebRequest request, OutputStream os) throws Exception;

	/**
	 * Adds the meta.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public abstract void addMeta(String name, String value);
	
	/**
	 * Adds the meta.
	 *
	 * @param map the map
	 */
	public abstract void addMeta(Map<String, String> map);
	
}