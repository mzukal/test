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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.serviceconnector.Constants;
import org.serviceconnector.api.cln.SCMgmtClient;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.util.CommandLineUtil;
import org.serviceconnector.util.ValidatorUtility;

public class SCConsole {

	/**
	 * @param args
	 *            usage : java -jar scconsole.jar -h <host> -p <port> <<<enable|disable|state|sessions>=service>|kill><br>
	 *            samples: java -jar scconsole.jar -h localhost -p 7000 enable=abc<br>
	 *            java -jar scconsole.jar -h localhost -p 7000 disable=abc<br>
	 *            java -jar scconsole.jar -h localhost -p 7000 state=abc<br>
	 *            java -jar scconsole.jar -h localhost -p 7000 sessions=abc<br>
	 *            java -jar scconsole.jar -h localhost -p 7000 kill<br>
	 *            java -jar scconsole.jar -h localhost -p 7000 restartSC<br>
	 *            java -jar scconsole.jar -l log4j-sc.properties -c sc.properties startSC<br>
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

		// get command from args[4]
		String bodyString = args[4];
		if (bodyString == null) {
			showError("Command is missing");
			System.exit(1);
		}

		// check log4j file
		String log4jFile = CommandLineUtil.getArg(args, ConsoleConstants.CLI_LOG4J_ARG);
		// check property file
		String propertyFile = CommandLineUtil.getArg(args, ConsoleConstants.CLI_PROP_ARG);
		// check host
		String host = CommandLineUtil.getArg(args, ConsoleConstants.CLI_HOST_ARG);
		// check port
		String port = CommandLineUtil.getArg(args, ConsoleConstants.CLI_PORT_ARG);

		if (log4jFile == null && propertyFile != null) {
			showError("Log4jFile argument is missing");
			System.exit(1);
		} else if (log4jFile != null && propertyFile == null) {
			showError("Property file argument is missing");
			System.exit(1);
		} else if (log4jFile != null && propertyFile != null) {
			int status = SCConsole.run(log4jFile, propertyFile, bodyString);
			System.exit(status);
		}

		if (host == null) {
			showError("Host argument is missing");
			System.exit(1);
		}
		if (port == null) {
			showError("Port argument is missing");
			System.exit(1);
		} else {
			ValidatorUtility.validateInt(1, port, 0xFFFF, SCMPError.HV_WRONG_PORTNR);
		}
		int status = SCConsole.run(host, port, bodyString);
		System.exit(status);
	}

	/**
	 * Run SCConsole command
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private static int run(String arg0, String arg1, String bodyString) throws Exception {

		/** The Constant COMMAND_REGEX_STRING. */
		String regex = "(" + Constants.KILL + "|" + Constants.STARTSC + "|" + Constants.RESTARTSC + "|(" + Constants.ENABLE + "|"
				+ Constants.DISABLE + "|" + Constants.STATE + "|" + Constants.SESSIONS + ")" + Constants.EQUAL_SIGN + "(.*))";
		int status = 0;

		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(bodyString);
		if (!m.matches()) {
			showError("invalid or no command (enable|disable|state|sessions|startSC|restartSC|kill)");
			return 3;
		}
		String command = m.group(1);
		String function = m.group(2);
		String serviceName = m.group(3);

		try {
			if (command.equalsIgnoreCase(Constants.STARTSC)) {
				System.out.println("SC start requested");
				Thread.sleep(1000);
				String startSCCmd = "java -Dlog4j.configuration=file:..\\config\\" + arg0
						+ " -jar ..\\bin\\sc.jar -sc.configuration ..\\config\\" + arg1;
				Runtime.getRuntime().exec(startSCCmd);
				return status;
			}

			SCMgmtClient client = new SCMgmtClient(arg0, Integer.parseInt(arg1), ConnectionType.NETTY_TCP);
			client.attach(5);

			if (command.equalsIgnoreCase(Constants.RESTARTSC)) {
				client.restartSC();
				System.out.println("SC restart requested");
			} else if (command.equalsIgnoreCase(Constants.KILL)) {
				client.killSC();
				System.out.println("SC exit requested");
			} else if (function.equalsIgnoreCase(Constants.ENABLE)) {
				client.enableService(serviceName);
				System.out.println("Service [" + serviceName + "] has been enabled");
				client.detach();
			} else if (function.equalsIgnoreCase(Constants.DISABLE)) {
				client.disableService(serviceName);
				System.out.println("Service [" + serviceName + "] has been disabled");
				client.detach();
			} else if (function.equalsIgnoreCase(Constants.STATE)) {
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
			} else if (function.equalsIgnoreCase(Constants.SESSIONS)) {
				try {
					String sessions = client.getWorkload(serviceName);
					System.out.println("Service [" + serviceName + "] has " + sessions + " sessions");
				} catch (Exception e) {
					System.out.println("Service [" + serviceName + "] does not exist!");
					status = 4;
				}
				client.detach();
			}
		} catch (Exception e) {
			e.printStackTrace();
			status = 5;
		}
		return status;
	}

	private static void showError(String msg) {
		System.err.println("error: " + msg);
		System.out
				.println("\nusage  : java -jar scconsole.jar -h <host> -p <port> <<<enable|disable|state|sessions>=service>|kill|restartSC>");
		System.out.println("\nsamples: java -jar scconsole.jar -h localhost -p 7000 enable=abc");
		System.out.println("         java -jar scconsole.jar -h localhost -p 7000 disable=abc");
		System.out.println("         java -jar scconsole.jar -h localhost -p 7000 state=abc");
		System.out.println("         java -jar scconsole.jar -h localhost -p 7000 sessions=abc");
		System.out.println("         java -jar scconsole.jar -h localhost -p 7000 kill");
		System.out.println("         java -jar scconsole.jar -h localhost -p 7000 restartSC");
		System.out.println("         java -jar scconsole.jar startSC");
	}
}