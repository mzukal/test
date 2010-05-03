/*-----------------------------------------------------------------------------*
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
 *-----------------------------------------------------------------------------*/
package com.stabilit.sc.common.log;

import java.io.IOException;

import com.stabilit.sc.common.factory.IFactoryable;
import com.stabilit.sc.common.listener.IWarningListener;
import com.stabilit.sc.common.listener.WarningEvent;

public class WarningLogger extends SimpleLogger implements IWarningListener {

	public WarningLogger() throws Exception {
		this("log/", "wrn.log");
	}

	public WarningLogger(String dir, String fileName) throws Exception {
		super(dir, fileName);
	}

	public void log(byte[] buffer) throws IOException {
		super.log(buffer);
	}

	public void log(byte[] buffer, int offset, int length) throws IOException {
		super.log(buffer, offset,length);
	}

	@Override
	public IFactoryable newInstance() {
		return this;
	}

	@Override
	public synchronized void warningEvent(WarningEvent warningEvent) {
		try {
			this.log(warningEvent.getText());
			this.log("\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
