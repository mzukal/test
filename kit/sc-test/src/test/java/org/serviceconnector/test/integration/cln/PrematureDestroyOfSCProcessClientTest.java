package org.serviceconnector.test.integration.cln;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.cln.SCMgmtClient;
import org.serviceconnector.ctrl.util.ProcessesController;
import org.serviceconnector.service.SCServiceException;

public class PrematureDestroyOfSCProcessClientTest {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(PrematureDestroyOfSCProcessClientTest.class);

	private SCMgmtClient client;
	private Process scProcess;

	private static ProcessesController ctrl;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		ctrl = new ProcessesController();
	}

	@Before
	public void setUp() throws Exception {
		try {
			scProcess = ctrl.startSC(TestConstants.log4jSCProperties, TestConstants.SCProperties);
		} catch (Exception e) {
			logger.error("oneTimeSetUp", e);
		}
		client = new SCMgmtClient();
	}

	@After
	public void tearDown() throws Exception {
		ctrl.stopSC(scProcess, TestConstants.log4jSCProperties);
		client = null;
		scProcess = null;
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		ctrl = null;
	}

	@Test(expected = SCServiceException.class)
	public void attach_afterSCDestroy_throwsException() throws Exception {
		scProcess.destroy();
		scProcess.waitFor();
		client.attach(TestConstants.HOST, TestConstants.PORT_HTTP);
	}

	@Test
	public void detach_beforeAttachAfterSCDestroy_passes() throws Exception {
		scProcess.destroy();
		scProcess.waitFor();
		client.detach();
	}

	@Test
	public void detach_afterSCDestroy_passes() throws Exception {
		client.attach(TestConstants.HOST, TestConstants.PORT_HTTP);
		scProcess.destroy();
		scProcess.waitFor();
		// TODO very with jan
		try {
			client.detach();
		} catch (SCServiceException ex) {
		}
		assertEquals(false, client.isAttached());
	}

	@Test(expected = SCServiceException.class)
	public void enableService_afterSCDestroy_throwsException() throws Exception {
		client.attach(TestConstants.HOST, TestConstants.PORT_HTTP);
		scProcess.destroy();
		scProcess.waitFor();
		client.enableService(TestConstants.sessionServiceName);
	}

	@Test(expected = SCServiceException.class)
	public void disableService_afterSCDestroy_throwsException() throws Exception {
		client.attach(TestConstants.HOST, TestConstants.PORT_HTTP);
		scProcess.destroy();
		scProcess.waitFor();
		client.enableService(TestConstants.sessionServiceName);
	}

	@Test(expected = SCServiceException.class)
	public void workload_afterSCDestroy_throwsException() throws Exception {
		client.attach(TestConstants.HOST, TestConstants.PORT_HTTP);
		scProcess.destroy();
		scProcess.waitFor();
		client.getWorkload(TestConstants.sessionServiceName);
	}

	@Test
	public void setMaxConnection_afterAttachAfterSCDestroy_passes() throws Exception {
		client.attach(TestConstants.HOST, TestConstants.PORT_HTTP);
		scProcess.destroy();
		scProcess.waitFor();
		client.setMaxConnections(10);
	}
}
