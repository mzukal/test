/*
 *-----------------------------------------------------------------------------*
 *                            Copyright � 2010 by                              *
 *                    STABILIT Informatik AG, Switzerland                      *
 *                            ALL RIGHTS RESERVED                              *
 *                                                                             *
 * Valid license from STABILIT is required for possession, use or copying.     *
 * This software or any other copies thereof may not be provided or otherwise  *
 * made available to any other person. No title to and ownership of the        *
 * software is hereby transferred. The information in this software is subject *
 * to change without notice and should not be construed as a commitment by     *
 * STABILIT Informatik AG.                                                     *
 *                                                                             *
 * All referenced products are trademarks of their respective owners.          *
 *-----------------------------------------------------------------------------*
 */
package com.stabilit.sc.handler;

import com.stabilit.sc.exception.ScConnectionException;
import com.stabilit.sc.msg.IMessage;
import com.stabilit.sc.service.IService;

/**
 * The Interface ClientResponseHandler handles responses on client side.
 * 
 * @author JTraber
 */
public interface ClientResponseHandler {

	/**
	 * Invoked when a message object was received from a remote peer.
	 * 
	 * @param service
	 *            the service
	 * @param response
	 *            the response
	 */
	void messageReceived(IService service, IMessage response);

	/**
	 * Invoked when a exception occurs in process of receiving a message.
	 * 
	 * @param service
	 *            the service
	 * @param exception
	 *            the exception
	 */
	void exceptionCaught(IService service, ScConnectionException exception);
}
