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
package org.serviceconnector.console;

import org.serviceconnector.api.cln.SCMgmtClient;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.util.CommandLineUtil;
import org.serviceconnector.util.ValidatorUtility;

public class SCConsole {

	/**
	 * @param args
	 * usage  : java -jar scconsole.jar -h <host> -p <port> <<<enable|disable|state|sessions>=service>|kill><br>
	 * 
	 * samples: java -jar scconsole.jar -h localhost -p 7000 enable=abc<br>
	 *          java -jar scconsole.jar -h localhost -p 7000 disable=abc<br>
	 *          java -jar scconsole.jar -h localhost -p 7000 state=abc<br>
	 *          java -jar scconsole.jar -h localhost -p 7000 sessions=abc<br>
	 *          java -jar scconsole.jar -h localhost -p 7000 kill<br>
	 *          
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// check arguments
		if (args == null || args.length <= 0) {
			showError("no argumments");
			System.exit(1);
		} else if (args.length < 5) {
			showError("not enough argumments");
			System.exit(1);
		} else if (args.length > 5) {
			showError("too many argumments");
			System.exit(1);
		}

		// check host
		String host = CommandLineUtil.getArg(args, Constants.CLI_HOST_ARG);
		if (host == null) {
			showError("Host argument is missing");
			System.exit(1);
		}

		// check port
		String port = CommandLineUtil.getArg(args, Constants.CLI_PORT_ARG);
		if (port == null) {
			showError("Port argument is missing");
			System.exit(1);
		} else {
			ValidatorUtility.validateInt(0, port, 0xFFFF, SCMPError.HV_WRONG_PORTNR);
		}

		ConsoleCommand consoleCommand = ConsoleCommand.UNDEFINED;
		String commandKey = "";
		String serviceName = "";
		for (int i = 0; i < args.length; i++) {
			String[] splitted = args[i].split("=");
			commandKey = splitted[0];
			if (splitted.length >= 2) {
				serviceName = splitted[1];
			}
			consoleCommand = ConsoleCommand.getCommand(commandKey);
			if (consoleCommand != ConsoleCommand.UNDEFINED) {
				break;
			}
		}
		if (consoleCommand == ConsoleCommand.UNDEFINED) {
			showError("invalid or no command (enable|disable|state|sessions|kill)");
			System.exit(3);
		}
		// fileName extracted from vm arguments
		int status = SCConsole.run(host, port, consoleCommand, serviceName);
		System.exit(status);
	}

	/**
	 * Run SCConsole command
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private static int run(String host, String port, ConsoleCommand cmd, String serviceName) throws Exception {
		int status = 0;
		try {
			SCMgmtClient client = new SCMgmtClient();
			client.setConnectionType(org.serviceconnector.Constants.NETTY_TCP);
			client.attach(host, Integer.parseInt(port));
			switch (cmd) {
			case DISABLE:
				client.disableService(serviceName);
				System.out.println("Service [" + serviceName + "] has been disabled");
				client.detach();
				break;
			case ENABLE:
				client.enableService(serviceName);
				System.out.println("Service [" + serviceName + "] has been enabled");
				client.detach();
				break;
			case STATE:
				try {
					boolean enabled = client.isServiceEnabled(serviceName);
					if (enabled) {
						System.out.println("Service [" + serviceName + "] is enabled");
					} else {
						System.out.println("Service [" + serviceName + "] is disabled");
					}
				} catch (Exception e) {
					System.out.println("Service [" + serviceName + "] does not exist!");
					status = 4;
				}
				client.detach();
				break;
			case SESSIONS:
				try {
					String sessions = client.getWorkload(serviceName);
					System.out.println("Service [" + serviceName + "] has " + sessions + " sessions");
				} catch (Exception e) {
					System.out.println("Service [" + serviceName + "] does not exist!");
					status = 4;
				}
				client.detach();
				break;
			case KILL:
				client.killSC();
				System.out.println("SC exit requested");
				break;
			}
			client.detach();
			return status;
		} catch (Exception e) {
			e.printStackTrace();
			status = 5;
		}
		return status;
	}

	private static void showError(String msg) {
		System.err.println("error: " + msg);
		System.out
				.println("\nusage  : java -jar scconsole.jar -h <host> -p <port> <<<enable|disable|state|sessions>=service>|kill>");
		System.out.println("\nsamples: java -jar scconsole.jar -h localhost -p 7000 enable=abc");
		System.out.println("         java -jar scconsole.jar -h localhost -p 7000 disable=abc");
		System.out.println("         java -jar scconsole.jar -h localhost -p 7000 state=abc");
		System.out.println("         java -jar scconsole.jar -h localhost -p 7000 sessions=abc");
		System.out.println("         java -jar scconsole.jar -h localhost -p 7000 kill");
	}
}
