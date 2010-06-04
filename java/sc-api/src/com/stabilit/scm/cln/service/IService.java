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
package com.stabilit.scm.cln.service;

import com.stabilit.scm.cln.req.IClientSession;
import com.stabilit.scm.cln.req.IRequester;


/**
 * @author JTraber
 *
 */
public interface IService {

	public abstract IServiceContext getServiceContext();

	public abstract void setMessagInfo(String messageInfo);

	public abstract void setData(Object obj);

	public abstract Object invoke() throws Exception;

	public abstract void destroyService() throws Exception;

	public abstract void setRequestor(IRequester client);

	public abstract void setSession(IClientSession session);
	
}
