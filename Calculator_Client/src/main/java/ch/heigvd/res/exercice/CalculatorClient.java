package ch.heigvd.res.exercice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a simple client for our custom presence protocol.
 * When the client connects to a server, a thread is started to listen for
 * notifications sent by the server.
 * 
 * @author Olivier Liechti
 */
public class CalculatorClient {

	final static Logger LOG = Logger.getLogger(CalculatorClient.class.getName());

	Socket clientSocket;
	BufferedReader in;
	PrintWriter out;
	boolean connected = false;
	String entree;


	/**
	 * This inner class implements the Runnable interface, so that the run()
	 * method can execute on its own thread. This method reads data sent from the
	 * server, line by line, until the connection is closed or lost.
	 */
	class NotificationListener implements Runnable {

		public void run() {
			String notification;
			try {
				while ((connected && (notification = in.readLine()) != null)) {
					System.out.println(notification);
				}
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Connection problem in client used by {0}", e.getMessage());
				connected = false;
			} finally {
				cleanup();
			}
		}
	}

	/**
	 * This method is used to connect to the server and to inform the server that
	 * the user "behind" the client has a name (in other words, the HELLO command
	 * is issued after successful connection).
	 * 
	 * @param serverAddress the IP address used by the Presence Server
	 * @param serverPort the port used by the Presence Server
	 */
	public void connect(String serverAddress, int serverPort) {
		try {
			clientSocket = new Socket(serverAddress, serverPort);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream());
			connected = true;
			this.entree = entree;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Unable to connect to server: {0}", e.getMessage());
			cleanup();
			return;
		}
		// Let us start a thread, so that we can listen for server notifications
		new Thread(new NotificationListener()).start();

	}

	public void sendRequest(String entree){
		out.println(entree);
		out.flush();
	}

	public void disconnect() {
		LOG.log(Level.INFO, "{0} has requested to be disconnected.", entree);
		connected = false;
		//out.println("BYE");
		cleanup();
	}

	private void cleanup() {

		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}

		if (out != null) {
			out.close();
		}

		try {
			if (clientSocket != null) {
				clientSocket.close();
			}
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}


}
