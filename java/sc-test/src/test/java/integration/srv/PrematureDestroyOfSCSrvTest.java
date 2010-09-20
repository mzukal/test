package integration.srv;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.ctrl.util.TestConstants;
import org.serviceconnector.ctrl.util.TestEnvironmentController;
import org.serviceconnector.sc.service.SCServiceException;
import org.serviceconnector.srv.ISCSessionServer;
import org.serviceconnector.srv.ISCServerCallback;
import org.serviceconnector.srv.SCSessionServer;


public class PrematureDestroyOfSCSrvTest {

	/** The Constant logger. */
	protected final static Logger logger = Logger
			.getLogger(PrematureDestroyOfSCSrvTest.class);

	private ISCSessionServer server;
	private Process scProcess;

	private static TestEnvironmentController ctrl;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		ctrl = new TestEnvironmentController();
	}
	
	@Before
	public void setUp() throws Exception {
		ctrl = new TestEnvironmentController();
		try {
			scProcess = ctrl.startSC(TestConstants.log4jSC0Properties, TestConstants.scProperties0);
		} catch (Exception e) {
			logger.error("oneTimeSetUp", e);
		}

		server = new SCSessionServer();
		server.startListener(TestConstants.HOST, 30000, 0);
	}

	@After
	public void tearDown() throws Exception {
		server.destroyServer();
		server = null;
		ctrl.stopProcess(scProcess, TestConstants.log4jSC0Properties);
		scProcess = null;
	}
	
	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		ctrl = null;
	}

	@Test(expected = SCServiceException.class)
	public void registerService_afterSCDestroyValidValues_throwsException() throws Exception {
		scProcess.destroy();
		server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceName, 10, 10, new CallBack());
	}

	@Test(expected = SCMPValidatorException.class)
	public void registerService_afterSCDestroyInvalidMaxSessions_throwsException() throws Exception {
		scProcess.destroy();
		server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceName, -1, 10, new CallBack());
	}

	@Test(expected = SCServiceException.class)
	public void registerService_afterSCDestroyInvalidHost_throwsException() throws Exception {
		scProcess.destroy();
		server.registerService("something", TestConstants.PORT9000, TestConstants.serviceName, 10, 10, new CallBack());
	}

	@Test(expected = SCServiceException.class)
	public void registerService_withImmediateConnectFalseAfterSCDestroyInvalidHost_throwsException()
			throws Exception {
		server.setImmediateConnect(false);
		scProcess.destroy();
		server.registerService("something", TestConstants.PORT9000, TestConstants.serviceName, 10, 10, new CallBack());
	}

	@Test
	public void deregisterService_afterSCDestroyWithoutPreviousRegister_passes() throws Exception {
		scProcess.destroy();
		server.deregisterService(TestConstants.serviceName);
	}

	@Test
	public void deregisterService_afterRegisterAfterSCDestroy_notRegistered() throws Exception {
		server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceName, 10, 10, new CallBack());
		scProcess.destroy();
		server.deregisterService(TestConstants.serviceName);
		assertEquals(false, server.isRegistered(TestConstants.serviceName));
	}
	
	@Test(expected = SCServiceException.class)
	public void registerService_afterRegisterAfterSCDestroy_throwsException() throws Exception {
		server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceName, 10, 10, new CallBack());
		scProcess.destroy();
		server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceNameAlt, 10, 10, new CallBack());
	}
	
	@Test
	public void isRegistered_afterRegisterAfterSCDestroy_thinksThatItIsRegistered() throws Exception {
		server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceName, 10, 10, new CallBack());
		scProcess.destroy();
		assertEquals(true, server.isRegistered(TestConstants.serviceName));
	}
	
	@Test
	public void setImmediateConnect_afterRegisterAfterSCDestroy_passes() throws Exception {
		server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceName, 10, 10, new CallBack());
		scProcess.destroy();
		server.setImmediateConnect(false);
	}

	private class CallBack implements ISCServerCallback {
	}
}