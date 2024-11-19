import java.io.*;
import java.net.Socket;

public class SThread implements Runnable {
	private Socket clientSocket;
	private TCPServerRouter router;
	private static int clientIdCounter = 0;
	private int clientId;

	public SThread(Socket clientSocket, TCPServerRouter router) {
		this.clientSocket = clientSocket;
		this.router = router;
		this.clientId = ++clientIdCounter;
	}

	@Override
	public void run() {
		try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

			// Read clientId first
			int receivedClientId = in.readInt();  // Now matches client sending
			int[][] matrixA = (int[][]) in.readObject();
			int[][] matrixB = (int[][]) in.readObject();

			Socket serverSocket = new Socket("localhost", 12346);
			router.addConnection(receivedClientId, serverSocket);

			ObjectOutputStream serverOut = new ObjectOutputStream(serverSocket.getOutputStream());
			serverOut.writeObject(matrixA);
			serverOut.writeObject(matrixB);

			ObjectInputStream serverIn = new ObjectInputStream(serverSocket.getInputStream());
			int[][] result = (int[][]) serverIn.readObject();
			long parallelExecutionTime = serverIn.readLong();
			double speedUp = serverIn.readDouble();
			double efficiency = serverIn.readDouble();

			// Send results back to the client
			out.writeObject(result);
			out.writeLong(parallelExecutionTime);
			out.writeDouble(speedUp);
			out.writeDouble(efficiency);

			router.removeConnection(receivedClientId);
			serverSocket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
