/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright © 2010 STABILIT Informatik AG, Switzerland                  *
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

package org.serviceconnector.web.xml;

import java.io.Writer;

import javax.xml.stream.XMLStreamWriter;

import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.registry.PublishMessageQueue;
import org.serviceconnector.registry.ServerRegistry;
import org.serviceconnector.registry.ServiceRegistry;
import org.serviceconnector.registry.SubscriptionRegistry;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.server.Server;
import org.serviceconnector.service.IPublishService;
import org.serviceconnector.service.Service;
import org.serviceconnector.service.Subscription;
import org.serviceconnector.web.IWebRequest;

/**
 * The Class SubscriptionsXMLLoader.
 */
public class SubscriptionsXMLLoader extends AbstractXMLLoader {

	/** {@inheritDoc} */
	@Override
	public final void loadBody(XMLStreamWriter writer, IWebRequest request) throws Exception {
		String serverParameter = request.getParameter("server");
		if (serverParameter != null) {
			ServerRegistry serverRegistry = AppContext.getServerRegistry();
			Server server = serverRegistry.getServer(serverParameter);
			if (server != null) {
				writer.writeStartElement("server");
				this.writeBean(writer, server);
				writer.writeEndElement();
			}
		}
		String serviceParameter = request.getParameter("service");
		if (serviceParameter != null) {
			ServiceRegistry serviceRegistry = AppContext.getServiceRegistry();
			Service service = serviceRegistry.getService(serviceParameter);
			if (service != null) {
				writer.writeStartElement("service");
				if (service instanceof IPublishService) {
					IPublishService publishService = (IPublishService) service;
					PublishMessageQueue<SCMPMessage> publishMessageQueue = publishService.getMessageQueue();
					writer.writeStartElement("publishMessageQueueSize");
					writer.writeCData(String.valueOf(publishMessageQueue.getTotalSize()));
					writer.writeEndElement(); // end of publishMessageQueueSize
				}
				this.writeBean(writer, service);
				writer.writeEndElement();
			}
		}
		SubscriptionRegistry subscriptionRegistry = AppContext.getSubscriptionRegistry();
		writer.writeStartElement("subscriptions");
		Subscription[] subscriptions = subscriptionRegistry.getSubscriptions();
		int simulation = this.getParameterInt(request, "sim", 0);
		if (simulation > 0) {
			Subscription[] sim = new Subscription[simulation + subscriptions.length];
			System.arraycopy(subscriptions, 0, sim, 0, subscriptions.length);
			for (int i = subscriptions.length; i < simulation; i++) {
				sim[i] = new Subscription(null, "sim " + i, null, 0, 0.0, false);
			}
			subscriptions = sim;
		}
		Paging paging = this.writePagingAttributes(writer, request, subscriptions.length, "");
		int nOfRealSubscriptions = 0;
		for (Subscription subscription : subscriptions) {
			if (subscription.isCascaded() == true) {
				// adding casceded subscriptions of current subscription
				nOfRealSubscriptions += subscription.getCscSubscriptionIds().size();
			} else {
				// adding current subscription because its a real one!
				nOfRealSubscriptions++;
			}
		}
		writer.writeAttribute("nOfRealSubscriptions", nOfRealSubscriptions + "");
		// String showSessionsParameter = request.getParameter("showsessions");
		int startIndex = paging.getStartIndex();
		int endIndex = paging.getEndIndex();
		for (int i = startIndex; i < endIndex; i++) {
			Subscription subscription = subscriptions[i];
			writer.writeStartElement("subscription");
			this.writeBean(writer, subscription);
			writer.writeEndElement();
		}
		writer.writeEndElement(); // close subscriptions tag
	}

	/** {@inheritDoc} */
	@Override
	public final void loadBody(Writer writer, IWebRequest request) throws Exception {
		if (writer instanceof XMLStreamWriter) {
			this.loadBody((XMLStreamWriter) writer, request);
		}
	}
}
