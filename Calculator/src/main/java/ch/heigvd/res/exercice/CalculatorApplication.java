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
public class CalculatorApplication {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");

		Thread listenThread = new Thread(new CalculatorServer());
		listenThread.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		CalculatorClient c1 = new CalculatorClient();
		c1.connect("localhost", Protocol.DEFAULT_PORT, "Sacha");
		c1.disconnect();

	}

}
