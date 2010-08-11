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
package com.stabilit.scm.common.service;

import com.stabilit.scm.cln.service.IService;

/**
 * The Interface IPublishService.
 * 
 * @author JTraber
 */
public interface IPublishService extends IService {

	/**
	 * Change subscription.
	 * 
	 * @param mask
	 *            the mask
	 * @throws Exception
	 *             the exception
	 */
	public abstract void changeSubscription(String mask) throws Exception;

	/**
	 * Unsubscribe.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public abstract void unsubscribe() throws Exception;

	/**
	 * Subscribe.
	 * 
	 * @param mask
	 *            the mask
	 * @param sessionInfo
	 *            the session info
	 * @param noDataInterval
	 *            the no data interval
	 * @param callback
	 *            the callback
	 * @throws Exception
	 *             the exception
	 */
	public abstract void subscribe(String mask, String sessionInfo, int noDataInterval, ISCMessageCallback callback)
			throws Exception;

	/**
	 * Subscribe.
	 * 
	 * @param mask
	 *            the mask
	 * @param sessionInfo
	 *            the session info
	 * @param noDataInterval
	 *            the no data interval
	 * @param authenticationId
	 *            the authentication id
	 * @param callback
	 *            the callback
	 * @throws Exception
	 *             the exception
	 */
	public abstract void subscribe(String mask, String sessionInfo, int noDataInterval, String authenticationId,
			ISCMessageCallback callback) throws Exception;
}
