package ch.heigvd.res.exercice;

import ch.heigvd.res.exercice.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a single-threaded TCP server. It is able to interact
 * with only one client at the time. If a client tries to connect when
 * the server is busy with another one, it will have to wait.
 *
 * @author Olivier Liechti
 */
public class CalculatorServer {

	final static Logger LOG = Logger.getLogger(CalculatorServer.class.getName());

	int port;

	/**
	 * Constructor
	 * @param port the port to listen on
	 */
	public CalculatorServer(int port) {
		this.port = port;
	}

	public boolean isNumeric(String s) {
		return s != null && s.matches("[-+]?\\d*\\.?\\d+");
	}

	/**
	 * This method initiates the process. The server creates a socket and binds
	 * it to the previously specified port. It then waits for clients in a infinite
	 * loop. When a client arrives, the server will read its input line by line
	 * and send back the data converted to uppercase. This will continue until
	 * the client sends the "BYE" command.
	 */
	public void serveClients() {
		ServerSocket serverSocket;
		Socket clientSocket = null;
		BufferedReader in = null;
		PrintWriter out = null;

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, null, ex);
			return;
		}

		while (true) {
			try {

				LOG.log(Level.INFO, "En attente d'un client sur le port " + port, port);
				clientSocket = serverSocket.accept();
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream());
				String line;
				boolean shouldRun = true;

				out.println("Bienvenue au calculateur en ligne");
				out.println("  - Voici les commandes à utiliser: ");
				out.println("  - Addition: 'ADD nbre1 nbre2'");
				out.println("  - Soustraction: 'SUB nbre1 nbre2'");
				out.println("  - Multiplication: 'MULT nbre1 nbre2'");
				out.println("  - Division: 'DIV nbre1 nbre2'");
				out.println("  - Fermer la connexion: 'KILL'\n");
				out.flush();
				LOG.info("Lecture et traitement des requêtes du client connecté");
				while ( (shouldRun) && (line = in.readLine()) != null ) {
					String[] tokens = line.split(" ");
					String op = tokens[0].toUpperCase();
					if(tokens.length != 3 && op.compareTo(Protocol.CMD_KILL) != 0 || op.compareTo(Protocol.CMD_KILL) == 0 && tokens.length != 1){
						out.println("Erreur: vous n'avez pas entré le bon nombre d'arguments");
						out.flush();
						continue;
					}

					if((op.compareTo(Protocol.CMD_KILL) != 0) && (!isNumeric(tokens[1]) || !isNumeric(tokens[2]))){
						out.println("Erreur: entrez des nombres !");
						out.flush();
						continue;
					}

					switch (op) {
						case (Protocol.CMD_ADD):
							out.println("Le résultat de votre addition = " + (Double.parseDouble(tokens[1]) + Double.parseDouble(tokens[2])));
							break;
						case (Protocol.CMD_SUB):
							out.println("Le résultat de votre soustraction = " + (Double.parseDouble(tokens[1]) - Double.parseDouble(tokens[2])));
							break;
						case (Protocol.CMD_MULT):
							out.println("Le résultat de votre multiplication = " + (Double.parseDouble(tokens[1]) * Double.parseDouble(tokens[2])));
							break;
						case (Protocol.CMD_DIV):
							out.println(Double.parseDouble(tokens[2]) != 0 ? "Le résultat de votre division = " + (Double.parseDouble(tokens[1]) /
											Double.parseDouble(tokens[2])) : "Erreur car division par 0");
							break;
						case (Protocol.CMD_KILL):
							out.println("La commande KILL a été reçue. Fermeture de la connexion en cours...");
							shouldRun = false;
							break;
						default:
							out.println("Erreur: il ne s'agit pas des commandes ADD, SUB, MULT, DIV ou KILL");
							break;
					}
					out.flush();
				}

				LOG.info("Nettoyage des ressources...");
				clientSocket.close();
				in.close();
				out.close();

			} catch (IOException ex) {
				if (in != null) {
					try {
						in.close();
					} catch (IOException ex1) {
						LOG.log(Level.SEVERE, ex1.getMessage(), ex1);
					}
				}
				if (out != null) {
					out.close();
				}
				if (clientSocket != null) {
					try {
						clientSocket.close();
					} catch (IOException ex1) {
						LOG.log(Level.SEVERE, ex1.getMessage(), ex1);
					}
				}
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
	}
}
