package org.serviceconnector.cln;

import org.apache.log4j.Logger;
import org.serviceconnector.cln.SCClient;
import org.serviceconnector.cln.service.IPublishService;
import org.serviceconnector.cln.service.ISCClient;
import org.serviceconnector.cln.service.IService;
import org.serviceconnector.service.ISCMessage;
import org.serviceconnector.service.SCMessageCallback;


public class DemoPublishClient extends Thread {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(DemoPublishClient.class);
	
	public static void main(String[] args) {
		DemoPublishClient demoPublishClient = new DemoPublishClient();
		demoPublishClient.start();
	}

	@Override
	public void run() {
		ISCClient sc = new SCClient();
		IPublishService publishService = null;
		try {
			((SCClient) sc).setConnectionType("netty.tcp");
			sc.attach("localhost", 9000);
			publishService = sc.newPublishService("publish-simulation");
			publishService.subscribe("0000121ABCDEFGHIJKLMNO-----------X-----------", "sessionInfo", 300,
					new DemoSessionClientCallback(publishService));

			while (true)
				;
		} catch (Exception e) {
			logger.error("run", e);
		} finally {
			try {
				publishService.unsubscribe();
				sc.detach();
			} catch (Exception e) {
				logger.info("run "+e.getMessage());
			}
		}
	}

	private class DemoSessionClientCallback extends SCMessageCallback {

		public DemoSessionClientCallback(IService service) {
			super(service);
		}

		@Override
		public void callback(ISCMessage reply) {
			System.out.println("Publish client received: " + reply.getData());
		}

		@Override
		public void callback(Exception e) {
		}
	}
}