package integration.srv;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.ctrl.util.TestConstants;
import org.serviceconnector.ctrl.util.TestEnvironmentController;
import org.serviceconnector.srv.ISCSessionServer;
import org.serviceconnector.srv.ISCServerCallback;
import org.serviceconnector.srv.SCSessionServer;


public class RegisterServiceToMultipleSCTest {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(RegisterServiceToMultipleSCTest.class);
	
	private int threadCount = 0;
	private ISCSessionServer server;

	private static TestEnvironmentController ctrl;
	private static Process scProcess;
	private static Process r;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		ctrl = new TestEnvironmentController();
		try {
			scProcess = ctrl.startSC(TestConstants.log4jSC0Properties, TestConstants.scProperties0);
			r = ctrl.startSC(TestConstants.log4jSC1Properties, TestConstants.scProperties1);
		} catch (Exception e) {
			logger.error("oneTimeSetUp", e);
		}
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		ctrl.stopProcess(scProcess, TestConstants.log4jSC0Properties);
		ctrl.stopProcess(r, TestConstants.log4jSC1Properties);
		ctrl = null;
		scProcess = null;
		r = null;
	}

	@Before
	public void setUp() throws Exception {
		threadCount = Thread.activeCount();
		server = new SCSessionServer();
	}

	@After
	public void tearDown() throws Exception {
		server.destroyServer();
		server = null;
		assertEquals(threadCount, Thread.activeCount());
	}

	@Test
	public void registerService_onMultipleSCs_registeredOnBoth() throws Exception {
		server.startListener(TestConstants.HOST, 9001, 0);
		server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceName, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.serviceName));
		assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
		server.registerService(TestConstants.HOST, TestConstants.PORT65535, TestConstants.serviceNameAlt, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.serviceName));
		assertEquals(true, server.isRegistered(TestConstants.serviceNameAlt));
		server.deregisterService(TestConstants.serviceName);
		assertEquals(false, server.isRegistered(TestConstants.serviceName));
		assertEquals(true, server.isRegistered(TestConstants.serviceNameAlt));
		server.deregisterService(TestConstants.serviceNameAlt);
		assertEquals(false, server.isRegistered(TestConstants.serviceName));
		assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
	}

	@Test
	public void registerService_withDifferentConnectionTypesHttpFirst_registeredThenNot()
			throws Exception {
		server.startListener(TestConstants.HOST, 9001, 0);
		((SCSessionServer) server).setConnectionType("netty.http");
		server.registerService(TestConstants.HOST, TestConstants.PORT8080, TestConstants.serviceName, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.serviceName));
		assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
		((SCSessionServer) server).setConnectionType("netty.tcp");
		server.registerService(TestConstants.HOST, TestConstants.PORT65535, TestConstants.serviceNameAlt, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.serviceName));
		assertEquals(true, server.isRegistered(TestConstants.serviceNameAlt));
		server.deregisterService(TestConstants.serviceName);
		server.deregisterService(TestConstants.serviceNameAlt);
		assertEquals(false, server.isRegistered(TestConstants.serviceName));
		assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
	}

	@Test
	public void registerService_withDifferentConnectionTypesTcpFirst_registeredThenNot()
			throws Exception {
		server.startListener(TestConstants.HOST, 9001, 0);
		server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceName, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.serviceName));
		assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
		((SCSessionServer) server).setConnectionType("netty.http");
		server.registerService(TestConstants.HOST, TestConstants.PORT1, TestConstants.serviceNameAlt, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.serviceName));
		assertEquals(true, server.isRegistered(TestConstants.serviceNameAlt));
		server.deregisterService(TestConstants.serviceName);
		server.deregisterService(TestConstants.serviceNameAlt);
		assertEquals(false, server.isRegistered(TestConstants.serviceName));
		assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
	}

	@Test
	public void registerService_httpConnectionType_registeredThenNot() throws Exception {
		server.startListener(TestConstants.HOST, 9001, 0);
		((SCSessionServer) server).setConnectionType("netty.http");
		server.registerService(TestConstants.HOST, TestConstants.PORT8080, TestConstants.serviceName, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.serviceName));
		assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
		server.registerService(TestConstants.HOST, TestConstants.PORT1, TestConstants.serviceNameAlt, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.serviceName));
		assertEquals(true, server.isRegistered(TestConstants.serviceNameAlt));
		server.deregisterService(TestConstants.serviceName);
		server.deregisterService(TestConstants.serviceNameAlt);
		assertEquals(false, server.isRegistered(TestConstants.serviceName));
		assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
	}

	@Test
	public void registerServiceDeregisterService_onTwoSCsHttp_periodicallyRegistered() throws Exception {
		server.startListener(TestConstants.HOST, 9001, 0);
		((SCSessionServer) server).setConnectionType("netty.http");
		for (int i = 0; i < 100; i++) {
			server.registerService(TestConstants.HOST, TestConstants.PORT8080, TestConstants.serviceName, 1, 1, new CallBack());
			server.registerService(TestConstants.HOST, TestConstants.PORT1, TestConstants.serviceNameAlt, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.serviceName));
			assertEquals(true, server.isRegistered(TestConstants.serviceNameAlt));
			server.deregisterService(TestConstants.serviceName);
			server.deregisterService(TestConstants.serviceNameAlt);
			assertEquals(false, server.isRegistered(TestConstants.serviceName));
			assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
		}
	}

	@Test
	public void registerServiceDeregisterService_onTwoSCsTcp_periodicallyRegistered() throws Exception {
		server.startListener(TestConstants.HOST, 9001, 0);
		for (int i = 0; i < 100; i++) {
			server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceName, 1, 1, new CallBack());
			server.registerService(TestConstants.HOST, TestConstants.PORT65535, TestConstants.serviceNameAlt, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.serviceName));
			assertEquals(true, server.isRegistered(TestConstants.serviceNameAlt));
			server.deregisterService(TestConstants.serviceName);
			server.deregisterService(TestConstants.serviceNameAlt);
			assertEquals(false, server.isRegistered(TestConstants.serviceName));
			assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
		}
	}
	
	@Test
	public void registerServiceDeregisterService_onTwoSCsBoth_periodicallyRegistered() throws Exception {
		server.startListener(TestConstants.HOST, 9001, 0);
		for (int i = 0; i < 100; i++) {
			((SCSessionServer) server).setConnectionType("netty.tcp");
			server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceName, 1, 1, new CallBack());
			((SCSessionServer) server).setConnectionType("netty.http");
			server.registerService(TestConstants.HOST, TestConstants.PORT1, TestConstants.serviceName, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.serviceName));
			assertEquals(true, server.isRegistered(TestConstants.serviceName));
			server.deregisterService(TestConstants.serviceName);
			server.deregisterService(TestConstants.serviceName);
			assertEquals(false, server.isRegistered(TestConstants.serviceName));
			assertEquals(false, server.isRegistered(TestConstants.serviceName));
		}
	}

	@Test
	public void registerServiceDeregisterService_onTwoSCsChangingConnectionTypes_periodicallyRegistered() throws Exception {
		server.startListener(TestConstants.HOST, 9001, 0);
		for (int i = 0; i < 50; i++) {
			server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceName, 1, 1, new CallBack());
			((SCSessionServer) server).setConnectionType("netty.http");
			server.registerService(TestConstants.HOST, TestConstants.PORT1, TestConstants.serviceNameAlt, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.serviceName));
			assertEquals(true, server.isRegistered(TestConstants.serviceNameAlt));
			server.deregisterService(TestConstants.serviceName);
			server.deregisterService(TestConstants.serviceNameAlt);
			assertEquals(false, server.isRegistered(TestConstants.serviceName));
			assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
			server.registerService(TestConstants.HOST, TestConstants.PORT8080, TestConstants.serviceName, 1, 1, new CallBack());
			((SCSessionServer) server).setConnectionType("netty.tcp");
			server.registerService(TestConstants.HOST, TestConstants.PORT65535, TestConstants.serviceNameAlt, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.serviceName));
			assertEquals(true, server.isRegistered(TestConstants.serviceNameAlt));
			server.deregisterService(TestConstants.serviceName);
			server.deregisterService(TestConstants.serviceNameAlt);
			assertEquals(false, server.isRegistered(TestConstants.serviceName));
			assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
			((SCSessionServer) server).setConnectionType("netty.tcp");
		}
	}

	@Test
	public void registerServiceDeregisterService_onTwoSCsChangingServices_periodicallyRegistered() throws Exception {
		server.startListener(TestConstants.HOST, 9001, 0);
		for (int i = 0; i < 50; i++) {
			server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceName, 1, 1, new CallBack());
			server.registerService(TestConstants.HOST, TestConstants.PORT65535, TestConstants.serviceNameAlt, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.serviceName));
			assertEquals(true, server.isRegistered(TestConstants.serviceNameAlt));
			server.deregisterService(TestConstants.serviceName);
			server.deregisterService(TestConstants.serviceNameAlt);
			assertEquals(false, server.isRegistered(TestConstants.serviceName));
			assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
			server.registerService(TestConstants.HOST, TestConstants.PORT65535, TestConstants.serviceName, 1, 1, new CallBack());
			server.registerService(TestConstants.HOST, TestConstants.PORT9000, TestConstants.serviceNameAlt, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.serviceName));
			assertEquals(true, server.isRegistered(TestConstants.serviceNameAlt));
			server.deregisterService(TestConstants.serviceName);
			server.deregisterService(TestConstants.serviceNameAlt);
			assertEquals(false, server.isRegistered(TestConstants.serviceName));
			assertEquals(false, server.isRegistered(TestConstants.serviceNameAlt));
		}
	}
	
	private class CallBack implements ISCServerCallback {
	}
}