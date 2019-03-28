package ch.heigvd.res.exercice;

import java.util.Scanner;

/**
 * The server reacts to the following commands, defined in the protocol:
 * - HELLO name: the user "behind" the client is not anonymous anymore
 * - SAY message: the message is broadcasted to connected clients
 * - WHO: the server returns the list of connected users
 * - BYE: the client is disconnected and the others are notified
 * 
 * @author Olivier Liechti
 */
public class CalculatorApplicationClient {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		CalculatorClient calculator = new CalculatorClient();
		String requete = "";
		calculator.connect(args[0], Protocol.DEFAULT_PORT);

		do{
			requete = sc.nextLine().toUpperCase();
			calculator.sendRequest(requete);
		}while(requete.compareTo(Protocol.CMD_KILL) != 0);
		calculator.disconnect();

	}
}
