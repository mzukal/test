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
package com.stabilit.scm.sc.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Properties;

import com.stabilit.scm.common.cmd.SCMPValidatorException;
import com.stabilit.scm.common.conf.Constants;
import com.stabilit.scm.common.scmp.SCMPError;
import com.stabilit.scm.sc.registry.DisabledServiceRegistry;
import com.stabilit.scm.sc.registry.ServiceRegistry;

/**
 * @author JTraber
 */
public class ServiceLoader {

	/**
	 * Loads services from a file.
	 * 
	 * @param fileName
	 *            the file name
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void load(String fileName) throws Exception {
		InputStream is = null;
		try {
			// try to find file outside of jar archive
			is = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			// try to find file inside jar archive
			is = ClassLoader.getSystemResourceAsStream(fileName);
		}

		if (is == null) {
			throw new InvalidParameterException("could not find property file : " + fileName);
		}
		Properties props = new Properties();
		props.load(is);

		String serviceNamesString = props.getProperty(Constants.SERVICE_NAMES);
		String[] serviceNames = serviceNamesString.split(Constants.COMMA_OR_SEMICOLON);

		ServiceRegistry serviceRegistry = ServiceRegistry.getCurrentInstance();
		DisabledServiceRegistry disabledServiceRegistry = DisabledServiceRegistry.getCurrentInstance();

		for (String serviceName : serviceNames) {
			// remove blanks in serviceName
			serviceName = serviceName.trim();
			String serviceTypeString = (String) props.get(serviceName + Constants.TYPE_QUALIFIER);
			ServiceType serviceType = ServiceType.getServiceType(serviceTypeString);

			// instantiate right type of service
			Service service = null;
			switch (serviceType) {
			case SESSION_SERVICE:
				service = new SessionService(serviceName);
				break;
			case PUBLISH_SERVICE:
				service = new PublishService(serviceName);
				break;
			case FILE_SERVICE:
				// TODO implement file services -wrong at this time
				service = new PublishService(serviceName);
				break;
			case UNDEFINED:
			default:
				throw new SCMPValidatorException(SCMPError.V_WRONG_CONFIGURATION_FILE,
						"wrong serviceType, serviceName/serviceType: " + serviceName + "/" + serviceTypeString);
			}

			String enable = props.getProperty(serviceName + Constants.ENABLE_QUALIFIER);
			if (enable == null || enable.equals("true")) {
				// enable is not set - means true or true itself
				serviceRegistry.addService(service.getServiceName(), service);
			} else {
				// enable is false - so add to disabledServiceRegistry
				disabledServiceRegistry.addService(service.getServiceName(), service);
			}
		}
	}
}
