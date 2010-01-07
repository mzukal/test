package com.stabilit.sc.app.server.socket.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.stabilit.sc.app.server.ServerApplication;
import com.stabilit.sc.cmd.CommandException;
import com.stabilit.sc.cmd.ICommand;
import com.stabilit.sc.cmd.factory.ICommandFactory;
import com.stabilit.sc.cmd.factory.CommandFactory;
import com.stabilit.sc.io.IRequest;
import com.stabilit.sc.io.IResponse;
import com.stabilit.sc.io.ISession;

public class SocketHttpServer extends ServerApplication {

	private static final int THREAD_COUNT = 10;

	private ServerSocket serverSocket;
	private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
			THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.MICROSECONDS,
			new LinkedBlockingQueue());

	@Override
	public void create() throws Exception {
		int port = this.getPort();
		serverSocket = new ServerSocket(port);
	}

	@Override
	public void run() throws Exception {
		while (true) {
			Socket requestSocket = serverSocket.accept();
			pool.execute(new RequestThread(requestSocket));
		}
	}

	@Override
	public void destroy() throws Exception {
		pool.shutdown();
		serverSocket.close();
	}

	public static class RequestThread implements Runnable {
		private Socket requestSocket = null;
		ICommandFactory commandFactory = CommandFactory.getInstance();

		public RequestThread(Socket requestSocket) {
			this.requestSocket = requestSocket;
		}

		public void run() {
			try {
				while (true) {
					IRequest request = new SocketHttpRequest(requestSocket);
					IResponse response = new SocketHttpResponse(requestSocket);
					ICommand command = commandFactory.newCommand(request);
					if (command == null) {
						throw new CommandException("invalid command");
					}
					command.run(request, response);
				}
			} catch (Exception e) {
				try {
					requestSocket.close();
				} catch (IOException ex) {
				}
			}
		}
	}

}
