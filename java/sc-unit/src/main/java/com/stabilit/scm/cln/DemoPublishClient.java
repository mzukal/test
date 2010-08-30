package com.stabilit.scm.cln;

import org.apache.log4j.Logger;

import com.stabilit.scm.cln.service.IPublishService;
import com.stabilit.scm.cln.service.ISCClient;
import com.stabilit.scm.cln.service.IService;
import com.stabilit.scm.common.log.IExceptionLogger;
import com.stabilit.scm.common.log.impl.ExceptionLogger;
import com.stabilit.scm.common.service.ISCMessage;
import com.stabilit.scm.common.service.SCMessageCallback;

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
			IExceptionLogger exceptionLogger = ExceptionLogger.getInstance();
			exceptionLogger.logErrorException(logger, this.getClass().getName(), "run", e);
		} finally {
			try {
				publishService.unsubscribe();
				sc.detach();
			} catch (Exception e) {
				IExceptionLogger exceptionLogger = ExceptionLogger.getInstance();
				exceptionLogger.logErrorException(logger, this.getClass().getName(), "run", e);
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