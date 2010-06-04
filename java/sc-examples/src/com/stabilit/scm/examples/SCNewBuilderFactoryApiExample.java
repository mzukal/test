/*
 *-----------------------------------------------------------------------------*
 *                                                                             *
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
 *-----------------------------------------------------------------------------*
/*
/**
 * 
 */
package com.stabilit.scm.examples;

import com.stabilit.scm.cln.service.ISCBuilderFactory;
import com.stabilit.scm.cln.service.IService;
import com.stabilit.scm.cln.service.IServiceBuilder;
import com.stabilit.scm.cln.service.IServiceContext;
import com.stabilit.scm.cln.service.SCBuilderFactory;


/**
 * @author JTraber
 */
public class SCNewBuilderFactoryApiExample {

	public void runExample() throws Exception {
		try {
			ISCBuilderFactory scFactory = new SCBuilderFactory();
			IServiceBuilder sc1Builder = scFactory.newDataServiceBuilder("localhost", 8080);
			IService dataService1 = sc1Builder.createService("simulation");
			IServiceContext serviceContext1 = dataService1.getServiceContext();		
			dataService1.setMessagInfo("message info");
			byte[] data = new byte[1024];
			dataService1.setData(data);
			Object resp = dataService1.invoke();
			dataService1.destroyService();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
