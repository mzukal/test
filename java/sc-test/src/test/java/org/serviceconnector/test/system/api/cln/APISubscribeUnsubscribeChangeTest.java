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
package org.serviceconnector.test.system.api.cln;

import java.security.InvalidParameterException;

import org.junit.Assert;
import org.junit.Test;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.SCSubscribeMessage;
import org.serviceconnector.api.cln.SCMgmtClient;
import org.serviceconnector.api.cln.SCPublishService;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.service.SCServiceException;
import org.serviceconnector.test.system.api.APISystemSuperPublishClientTest;

@SuppressWarnings("unused")
public class APISubscribeUnsubscribeChangeTest extends APISystemSuperPublishClientTest {

	/**
	 * Description: subscribe (regular)<br>
	 * Expectation: passes
	 */
	@Test
	public void t01_subscribe() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());
		Assert.assertTrue("is not subscribed", publishService.isSubscribed());
		publishService.unsubscribe();
		Assert.assertNull("the session ID is not null", publishService.getSessionId());
	}

	/**
	 * Description: create publish service with name = ""<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t03_subscribe() throws Exception {
		publishService = client.newPublishService("");
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage();
		subMsgRequest.setMask(TestConstants.mask);
		SCSubscribeMessage subMsgResponse = null;
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}

	/**
	 * Description: create publish service with name = " "<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t04_subscribe() throws Exception {
		publishService = client.newPublishService(" ");
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage();
		subMsgRequest.setMask(TestConstants.mask);
		SCSubscribeMessage subMsgResponse = null;
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}
	
	/**
	 * Description: create publish service with name = "The quick brown fox jumps over a lazy dog."<br>
	 * Expectation: throws SCServiceException (too long)
	 */
	@Test (expected = SCServiceException.class)
	public void t05_subscribe() throws Exception {
		publishService = client.newPublishService(TestConstants.pangram);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage();
		subMsgRequest.setMask(TestConstants.mask);
		SCSubscribeMessage subMsgResponse = null;
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}
	
	/**
	 * Description: create publish service with name = "service = gaga"<br>
	 * Expectation: throws SCServiceException (contains "=")
	 */
	@Test (expected = SCServiceException.class)
	public void t06_subscribe() throws Exception {
		publishService = client.newPublishService("service = gaga");
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage();
		subMsgRequest.setMask(TestConstants.mask);
		SCSubscribeMessage subMsgResponse = null;
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}

	/**
	 * Description: subscribe with mask = null<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t07_subscribe() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(null);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}

	/**
	 * Description: subscribe with no mask<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t08_subscribe() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}
	/**
	 * Description: subscribe with service name = null<br>
	 * Expectation: throws InvalidParameterException
	 */
	@Test (expected = InvalidParameterException.class)
	public void t09_subscribe() throws Exception {
		publishService = client.newPublishService(null);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(" ");
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}

	/**
	 * Description: subscribe with service name = ""<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t10_subscribe() throws Exception {
		publishService = client.newPublishService("");
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(" ");
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}

	/**
	 * Description: subscribe with service name = " "<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t11_subscribe() throws Exception {
		publishService = client.newPublishService(" ");
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(" ");
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}

	/**
	 * Description: subscribe with non-existing service name<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t12_subscribe() throws Exception {
		publishService = client.newPublishService("gaga");
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(" ");
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}
	
	/**
	 * Description: subscribe with session service name<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t13_subscribe() throws Exception {
		publishService = client.newPublishService(TestConstants.sesServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(" ");
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}
	
	/**
	 * Description: subscribe with file service name<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t14_subscribe() throws Exception {
		publishService = client.newPublishService(TestConstants.filServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(" ");
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}

	/**
	 * Description: subscribe with callback = null<br>
	 * Expectation: throws InvalidParameterException
	 */
	@Test (expected = InvalidParameterException.class)
	public void t15_subscribe() throws Exception {
		publishService = client.newPublishService(TestConstants.filServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(" ");
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		subMsgResponse = publishService.subscribe(subMsgRequest, null);
	}


	/**
	 * Description: subscribe to disabed service<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class )
	public void t20_disabledService() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);

		// disable service
		SCMgmtClient clientMgmt = new SCMgmtClient(TestConstants.HOST, TestConstants.PORT_SC_TCP);
		clientMgmt.attach();
		clientMgmt.disableService(TestConstants.pubServiceName1);
		clientMgmt.detach();

		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}

	/**
	 * Description: subscribe service with noDataInterval = 1 sec<br>
	 * Expectation: passes
	 */
	@Test 
	public void t30_noDataInterval() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(1);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());
		Assert.assertTrue("is not subscribed", publishService.isSubscribed());
		publishService.unsubscribe();
		Assert.assertNull("the session ID is not null", publishService.getSessionId());
	}

	/**
	 * Description: two subscriptions from the same client<br>
	 * Expectation: passes
	 */
	@Test
	public void t40_twoSubscriptions() throws Exception {
		SCPublishService service1 = client.newPublishService(TestConstants.pubServiceName1);
		SCPublishService service2 = client.newPublishService(TestConstants.pubServiceName1);
		
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(10);
		
		MsgCallback cbk1 = new MsgCallback(service1);
		MsgCallback cbk2 = new MsgCallback(service2);
		
		subMsgResponse = service1.subscribe(subMsgRequest, cbk1);
		Assert.assertNotNull("the session ID is null", service1.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());
		Assert.assertTrue("is not subscribed", service1.isSubscribed());
		
		subMsgResponse = service2.subscribe(subMsgRequest, cbk2);
		Assert.assertNotNull("the session ID is null", service2.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());
		Assert.assertTrue("is not subscribed", service2.isSubscribed());
		
		service1.unsubscribe();
		Assert.assertNull("the session ID is not null)", service1.getSessionId());
		service2.unsubscribe();
		Assert.assertNull("the session ID is not null)", service2.getSessionId());
	}

	/**
	 * Description: subscribe twice<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t41_subscribeTwice() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(null);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(10);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}

	/**
	 * Description: reject subscription by server <br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class )
	public void t50_reject() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.rejectSessionCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
	}
	
	/**
	 * Description: reject subscription by server, check error code<br>
	 * Expectation: passes, exception catched 
	 */
	@Test
	public void t51_reject() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.rejectSessionCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		Boolean passed = false;
		try {
			subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		} catch (SCServiceException e) {
			passed = true;
			Assert.assertEquals("is not appErrorCode", TestConstants.appErrorCode, e.getAppErrorCode());
			Assert.assertEquals("is not appErrorText", TestConstants.appErrorText, e.getAppErrorText());
		}
		Assert.assertTrue("did not throw exception", passed);
		Assert.assertFalse("is subscribed", publishService.isSubscribed());
		publishService.unsubscribe();
		Assert.assertNull("the session ID is not null", publishService.getSessionId());
	}

	/**
	 * Description: unsubscribe (regular)<br>
	 * Expectation: passes
	 */
	@Test
	public void t60_unsubscribe() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());

		publishService.unsubscribe();
		Assert.assertNull("the session ID is not null", publishService.getSessionId());
	}

	/**
	 * Description: unsubscribe to disabed service<br>
	 * Expectation: passes
	 */
	@Test
	public void t61_disabledService() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);	
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		
		// disable service
		SCMgmtClient clientMgmt = new SCMgmtClient(TestConstants.HOST, TestConstants.PORT_SC_TCP);
		clientMgmt.attach();
		clientMgmt.disableService(TestConstants.pubServiceName1);
		clientMgmt.detach();
		
		publishService.unsubscribe();
		Assert.assertNull("the session ID is not null", publishService.getSessionId());
	}

	/**
	 * Description: subscribe twice<br>
	 * Expectation: passes
	 */
	@Test
	public void t62_unsubscribeTwice() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());

		publishService.unsubscribe();
		Assert.assertNull("the session ID is not null", publishService.getSessionId());
		
		publishService.unsubscribe();
		Assert.assertNull("the session ID is not null", publishService.getSessionId());
	}
	
	/**
	 * Description: change subscription (regular)<br>
	 * Expectation: passes
	 */
	@Test
	public void t70_changeSubscription() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(10);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());

		subMsgRequest.setMask(TestConstants.mask1);
		subMsgResponse = publishService.changeSubscription(subMsgRequest);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());

		publishService.unsubscribe();
		Assert.assertNull("the session ID is not null", publishService.getSessionId());
	}

	/**
	 * Description: change subscription with mask = null<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t71_changeSubscription() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());

		subMsgRequest.setMask(null);
		subMsgResponse = publishService.changeSubscription(subMsgRequest);
	}

	/**
	 * Description: change subscription without subscribing<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t72_changeSubscription() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgResponse = publishService.changeSubscription(subMsgRequest);
	}
	

	/**
	 * Description: change subscription after unsubscribe<br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class)
	public void t73_changeSubscription() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());

		publishService.unsubscribe();
		Assert.assertNull("the session ID is not null", publishService.getSessionId());
		
		subMsgRequest.setMask(TestConstants.mask1);
		subMsgResponse = publishService.changeSubscription(subMsgRequest);
	}

	/**
	 * Description: change subscription with the same mask<br>
	 * Expectation: passes
	 */
	@Test
	public void t74_changeSubscription() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(10);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());

		subMsgRequest.setMask(TestConstants.mask);
		subMsgResponse = publishService.changeSubscription(subMsgRequest);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());

		publishService.unsubscribe();
		Assert.assertNull("the session ID is not null", publishService.getSessionId());
	}

	
	/**
	 * Description: change subscription to disabed service<br>
	 * Expectation: passes
	 */
	@Test
	public void t80_disabledService() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(10);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());

		// disable service
		SCMgmtClient clientMgmt = new SCMgmtClient(TestConstants.HOST, TestConstants.PORT_SC_TCP);
		clientMgmt.attach();
		clientMgmt.disableService(TestConstants.pubServiceName1);
		clientMgmt.detach();
		
		subMsgRequest.setMask(TestConstants.mask1);
		subMsgResponse = publishService.changeSubscription(subMsgRequest);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());

		publishService.unsubscribe();
		Assert.assertNull("the session ID is not null", publishService.getSessionId());
	}

	/**
	 * Description: reject subscription by server <br>
	 * Expectation: throws SCServiceException
	 */
	@Test (expected = SCServiceException.class )
	public void t90_reject() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());

		subMsgRequest.setMask(TestConstants.mask1);
		subMsgRequest.setSessionInfo(TestConstants.rejectSessionCmd);
		subMsgResponse = publishService.changeSubscription(subMsgRequest);
	}
	
	/**
	 * Description: reject change subscription by server, check error code<br>
	 * Expectation: passes, exception catched 
	 */
	@Test
	public void t91_reject() throws Exception {
		publishService = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage(TestConstants.pangram);
		SCSubscribeMessage subMsgResponse = null;
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo(TestConstants.doNothingCmd);
		subMsgRequest.setNoDataIntervalInSeconds(100);
		msgCallback = new MsgCallback(publishService);
		subMsgResponse = publishService.subscribe(subMsgRequest, msgCallback);
		Assert.assertNotNull("the session ID is null", publishService.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());

		subMsgRequest.setMask(TestConstants.mask1);
		subMsgRequest.setSessionInfo(TestConstants.rejectSessionCmd);

		Boolean passed = false;
		try {
			subMsgResponse = publishService.changeSubscription(subMsgRequest);
		} catch (SCServiceException e) {
			passed = true;
			Assert.assertEquals("is not appErrorCode", TestConstants.appErrorCode, e.getAppErrorCode());
			Assert.assertEquals("is not appErrorText", TestConstants.appErrorText, e.getAppErrorText());
		}
		Assert.assertTrue("did not throw exception", passed);
		Assert.assertTrue("is nor subscribed", publishService.isSubscribed());
		publishService.unsubscribe();
		Assert.assertNull("the session ID is not null)", publishService.getSessionId());
	}

}