import java.net.*;
import java.io.*;
import java.util.Random;

public class TCPClient {
    private static final int ROUTER_PORT = 12345;
    private static final String SERVER_IP = "localhost";
    private static final int MATRIX_SIZE = 20;

    public Metrics startClient(int clientId) {
        Metrics metrics = null; // Initialize as null to catch potential failures
        try (Socket socket = new Socket(SERVER_IP, ROUTER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Send client ID to the server
            out.writeInt(clientId);
            out.flush();

            // Generate random matrices
            int[][] matrixA = generateRandomMatrix(MATRIX_SIZE);
            int[][] matrixB = generateRandomMatrix(MATRIX_SIZE);

            // Send matrices to the server
            out.writeObject(matrixA);
            out.writeObject(matrixB);

            // Receive result and metrics from the server
            int[][] result = (int[][]) in.readObject();
            long parallelExecutionTime = in.readLong();
            double speedUp = in.readDouble();
            double efficiency = in.readDouble();

            // Create a Metrics object with the received values
            metrics = new Metrics(parallelExecutionTime, speedUp, efficiency);

            // Output results and metrics
            System.out.println("Client ID: " + clientId);
            System.out.println("Parallel Execution Time: " + parallelExecutionTime + " ns");
            System.out.println("Speed Up: " + speedUp);
            System.out.println("Efficiency: " + efficiency);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return metrics;
    }




    private static int[][] generateRandomMatrix(int size) {
        Random random = new Random();
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextInt(10); // Random integers between 0-9
            }
        }
        return matrix;
    }
}
