package ch.heigvd.res.exercice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a multi-threaded server of the custom presence protocol. The
 * server binds a socket on the specified port and waits for incoming connection
 * requests. It keeps track of connected clients in a list. When new clients
 * arrive, leave or send messages, the server notifies all connected clients.
 *
 * @author Olivier Liechti
 */
public class CalculatorServer implements Runnable {

	final static Logger LOG = Logger.getLogger(CalculatorServer.class.getName());

	boolean shouldRun;
	ServerSocket serverSocket;
	final List<Worker> connectedWorkers;

	public CalculatorServer() {
		this.shouldRun = true;
		this.connectedWorkers = Collections.synchronizedList(new LinkedList<Worker>());
	}

	private void registerWorker(Worker worker) {
		LOG.log(Level.INFO, ">> Waiting for lock before registring worker {0}", worker.userName);
		connectedWorkers.add(worker);
		LOG.log(Level.INFO, "<< Worker {0} registered.", worker.userName);
	}

	private void unregisterWorker(Worker worker) {
		LOG.log(Level.INFO, ">> Waiting for lock before unregistring worker {0}", worker.userName);
		connectedWorkers.remove(worker);
		LOG.log(Level.INFO, "<< Worker {0} unregistered.", worker.userName);
	}

	private void notifyConnectedWorkers(String message) {
		LOG.info(">> Waiting for lock before notifying workers");
		synchronized (connectedWorkers) {
		LOG.info("Notifying workers");
			for (Worker worker : connectedWorkers) {
				worker.sendNotification(message);
			}
		}
		LOG.info("<< Workers notified");
	}

	private void disconnectConnectedWorkers() {
		LOG.info(">> Waiting for lock before disconnecting workers");
		synchronized (connectedWorkers) {
		LOG.info("Disconnecting workers");
			for (Worker worker : connectedWorkers) {
				worker.disconnect();
			}
		}
		LOG.info("<< Workers disconnected");
	}


	public void run() {
		try {
			LOG.log(Level.INFO, "Starting Presence Server on port {0}", Protocol.DEFAULT_PORT);
			serverSocket = new ServerSocket(Protocol.DEFAULT_PORT);
			while (shouldRun) {
				Socket clientSocket = serverSocket.accept();
				CalculatorServer.this.notifyConnectedWorkers("Someone has arrived...");
				Worker newWorker = new Worker(clientSocket);
				registerWorker(newWorker);
				new Thread(newWorker).start();
			}
			serverSocket.close();
			LOG.info("shouldRun is false... server going down");
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
            System.exit(-1);
		}
	}

	private void shutdown() {
		LOG.info("Shutting down server...");
		shouldRun = false;
		try {
			serverSocket.close();
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
		disconnectConnectedWorkers();
	}

	class Worker implements Runnable {

		Socket clientSocket;
		BufferedReader in;
		PrintWriter out;
		boolean connected;
		String userName = "An anonymous user";

		public Worker(Socket clientSocket) {
			this.clientSocket = clientSocket;
			try {
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream());
				connected = true;
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}


		public void run() {
			String commandLine;
			CalculatorServer.this.notifyConnectedWorkers("Bienvenue au calculateur en ligne");
			CalculatorServer.this.notifyConnectedWorkers("  Voici les commandes à utiliser: ");
			CalculatorServer.this.notifyConnectedWorkers("  Addition: 'ADD nbre1 nbre2'");
			CalculatorServer.this.notifyConnectedWorkers("  Soustraction: 'SUB nbre1 nbre2'");
			CalculatorServer.this.notifyConnectedWorkers("  Multiplication: 'MULT nbre1 nbre2'");
			CalculatorServer.this.notifyConnectedWorkers("  Division: 'DIV nbre1 nbre2'");
			CalculatorServer.this.notifyConnectedWorkers("  Fermer la connexion: 'KILL'");

			try {
				while (connected && ((commandLine = in.readLine()) != null)) {
					String[] tokens = commandLine.split(" ");
					switch (tokens[0].toUpperCase()) {
						case (Protocol.CMD_ADD):
							CalculatorServer.this.notifyConnectedWorkers("Le résultat de votre addition = " + (Double.parseDouble(tokens[1]) + Double.parseDouble(tokens[2])));
							break;
						case (Protocol.CMD_SUB):
							CalculatorServer.this.notifyConnectedWorkers("Le résultat de votre soustraction = " + (Double.parseDouble(tokens[1]) - Double.parseDouble(tokens[2])));
							break;
						case (Protocol.CMD_MULT):
							CalculatorServer.this.notifyConnectedWorkers("Le résultat de votre multiplication = " + (Double.parseDouble(tokens[1]) * Double.parseDouble(tokens[2])));
							break;
						case (Protocol.CMD_DIV):
							CalculatorServer.this.notifyConnectedWorkers("Le résultat de votre division = " +
									(Double.parseDouble(tokens[0]) != 0 ? (Double.parseDouble(tokens[1]) +
											Double.parseDouble(tokens[2])) : "Erreur car division par 0"));
							break;
						case (Protocol.CMD_KILL):
							sendNotification("KILL command received. Bringing server down...");
							shutdown();
							break;
						default:
							sendNotification("Erreur: il ne s'agit pas des commandes ADD, SUB, MULT, DIV ou KILL");
					}
				}
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			} finally {
				unregisterWorker(this);
				CalculatorServer.this.notifyConnectedWorkers("Le client a quitté le serveur");
				cleanup();
			}
		}

		private void cleanup() {
			LOG.log(Level.INFO, "Cleaning up worker used by {0}", userName);

			LOG.log(Level.INFO, "Closing clientSocket used by {0}", userName);
			try {
				if (clientSocket != null) {
					clientSocket.close();
				}
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			}

			LOG.log(Level.INFO, "Closing in used by {0}", userName);
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			}

			LOG.log(Level.INFO, "Closing out used by {0}", userName);
			if (out != null) {
				out.close();
			}

			LOG.log(Level.INFO, "Clean up done for worker used by {0}", userName);
		}

		public void sendNotification(String message) {
			out.println(message);
			out.flush();
		}

		private void disconnect() {
			LOG.log(Level.INFO, "Disconnecting worker used by {0}", userName);
			connected = false;
			cleanup();
		}

	}

}
