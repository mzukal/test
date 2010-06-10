/*-----------------------------------------------------------------------------*
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
 *-----------------------------------------------------------------------------*/
package com.stabilit.scm.unit.test.worse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.stabilit.scm.cln.call.SCMPCallException;
import com.stabilit.scm.cln.call.SCMPCallFactory;
import com.stabilit.scm.cln.call.SCMPClnDataCall;
import com.stabilit.scm.cln.call.SCMPClnDeleteSessionCall;
import com.stabilit.scm.cln.call.SCMPClnSystemCall;
import com.stabilit.scm.cln.call.SCMPInspectCall;
import com.stabilit.scm.common.cmd.factory.CommandFactory;
import com.stabilit.scm.common.conf.RequesterConfig;
import com.stabilit.scm.common.msg.impl.InspectMessage;
import com.stabilit.scm.common.net.req.RequesterFactory;
import com.stabilit.scm.common.scmp.SCMPError;
import com.stabilit.scm.common.scmp.SCMPMessage;
import com.stabilit.scm.common.scmp.SCMPMsgType;
import com.stabilit.scm.sc.ServiceConnector;
import com.stabilit.scm.sim.Simulation;
import com.stabilit.scm.unit.UnitCommandFactory;
import com.stabilit.scm.unit.test.SCTest;
import com.stabilit.scm.unit.test.SetupTestCases;
import com.stabilit.scm.unit.test.session.SuperSessionRegisterTestCase;

/**
 * @author JTraber
 */
public class WorseScenarioSimulationServerTestCase extends SuperSessionRegisterTestCase {

	/**
	 * The Constructor.
	 * 
	 * @param fileName
	 *            the file name
	 */
	public WorseScenarioSimulationServerTestCase(String fileName) {
		super(fileName);
	}

	@Before
	@Override
	public void setup() {
		try {
			SetupTestCases.init();
			CommandFactory.setCurrentCommandFactory(new UnitCommandFactory());
			ServiceConnector.main(null);
			Simulation.main(null);
			config = new RequesterConfig();
			config.load(fileName);
			RequesterFactory clientFactory = new RequesterFactory();
			req = clientFactory.newInstance(config.getClientConfig());
			req.connect(); // physical connect
			clnAttachBefore();
			registerServiceBefore();
			clnCreateSessionBefore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void clnDeleteSessionSimulationServerDisconnect() throws Exception {
		// disconnects simulation server from SC after sending response
		SCMPClnSystemCall systemCall = (SCMPClnSystemCall) SCMPCallFactory.CLN_SYSTEM_CALL.newInstance(req, this.scSession);
		systemCall.setMaxNodes(2);
		systemCall.invoke();

		/*
		 * delete session shouldn't fail even service is down, clean up works fine client doesn't notice the failure
		 */
		SCMPClnDeleteSessionCall deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL
				.newInstance(req, this.scSession);
		deleteSessionCall.invoke();

		SCMPInspectCall inspectCall = (SCMPInspectCall) SCMPCallFactory.INSPECT_CALL.newInstance(req);
		SCMPMessage inspect = inspectCall.invoke();
		/*********************************** Verify registry entries in SC ********************************/
		InspectMessage inspectMsg = (InspectMessage) inspect.getBody();
		Assert.assertEquals("", inspectMsg.getAttribute("sessionRegistry"));

		String expectedScEntry = ":compression=false;localDateTime=" + localDateTimeOfConnect
				+ ";scVersion=1.0-000;keepAliveTimeout=30,360;";
		String scEntry = (String) inspectMsg.getAttribute("clientRegistry");
		// truncate /127.0.0.1:3640 because port may vary.
		scEntry = scEntry.substring(scEntry.indexOf(":") + 1);
		scEntry = scEntry.substring(scEntry.indexOf(":"));
		Assert.assertEquals(expectedScEntry, scEntry);

		expectedScEntry = "P01_RTXS_RPRWS1:SCMP [header={messageID=2, portNr=9000, maxSessions=10, msgType=REGISTER_SERVICE, multiThreaded=1, serviceName=P01_RTXS_RPRWS1}]simulation:SCMP [header={messageID=1, portNr=7000, maxSessions=1, msgType=REGISTER_SERVICE, multiThreaded=1, serviceName=simulation}]";
		scEntry = (String) inspectMsg.getAttribute("serviceRegistry");
		Assert.assertEquals(expectedScEntry, scEntry);

		// remove entry in serviceRegistry on Sc
		this.deRegisterServiceAfter("simulation");
	}

	@Test
	public void clnDataSimulationServerDisconnect() throws Exception {

		// disconnects simulation server from SC after sending response
		SCMPClnSystemCall systemCall = (SCMPClnSystemCall) SCMPCallFactory.CLN_SYSTEM_CALL.newInstance(req, this.scSession);
		systemCall.setMaxNodes(2);
		systemCall.invoke();

		// data call should fail because connection lost to simulation server
		SCMPClnDataCall clnDataCall = (SCMPClnDataCall) SCMPCallFactory.CLN_DATA_CALL.newInstance(req, this.scSession);
		clnDataCall.setServiceName("simulation");
		clnDataCall.setMessagInfo("asdasd");
		clnDataCall.setRequestBody("hello");
		try {
			clnDataCall.invoke();
		} catch (SCMPCallException ex) {
			SCTest.verifyError(ex.getFault(), SCMPError.SERVER_ERROR, SCMPMsgType.CLN_DATA);
		}

		SCMPInspectCall inspectCall = (SCMPInspectCall) SCMPCallFactory.INSPECT_CALL.newInstance(req);
		SCMPMessage inspect = inspectCall.invoke();
		/*********************************** Verify registry entries in SC ********************************/
		InspectMessage inspectMsg = (InspectMessage) inspect.getBody();
		Assert.assertEquals("", inspectMsg.getAttribute("sessionRegistry"));

		String expectedScEntry = ":compression=false;localDateTime=" + localDateTimeOfConnect
				+ ";scVersion=1.0-000;keepAliveTimeout=30,360;";
		String scEntry = (String) inspectMsg.getAttribute("clientRegistry");
		// truncate /127.0.0.1:3640 because port may vary.
		scEntry = scEntry.substring(scEntry.indexOf(":") + 1);
		scEntry = scEntry.substring(scEntry.indexOf(":"));
		Assert.assertEquals(expectedScEntry, scEntry);

		expectedScEntry = "P01_RTXS_RPRWS1:SCMP [header={messageID=2, portNr=9000, maxSessions=10, msgType=REGISTER_SERVICE, multiThreaded=1, serviceName=P01_RTXS_RPRWS1}]simulation:SCMP [header={messageID=1, portNr=7000, maxSessions=1, msgType=REGISTER_SERVICE, multiThreaded=1, serviceName=simulation}]";
		scEntry = (String) inspectMsg.getAttribute("serviceRegistry");
		Assert.assertEquals(expectedScEntry, scEntry);

		// remove entry in serviceRegistry on Sc
		this.deRegisterServiceAfter("simulation");
	}

	/**
	 * Tear down. Needs to be overridden because clnDeleteSession() from usual procedure is not possible this time -
	 * backend server already down.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@After
	@Override
	public void tearDown() throws Exception {
		this.deRegisterServiceAfter();
		this.clnDetachAfter();
		req.disconnect();
		req.destroy();
	}
}