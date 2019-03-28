package ch.heigvd.res.exercice;

/**
 * The server reacts to the following commands, defined in the protocol:
 * - HELLO name: the user "behind" the client is not anonymous anymore
 * - SAY message: the message is broadcasted to connected clients
 * - WHO: the server returns the list of connected users
 * - BYE: the client is disconnected and the others are notified
 * 
 * @author Olivier Liechti
 */
public class CalculatorApplicationServer {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		CalculatorServer calculator = new CalculatorServer(Protocol.DEFAULT_PORT);
		calculator.serveClients();


	}

}
