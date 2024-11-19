import java.net.*;
import java.io.*;
import java.util.concurrent.*;


public class TCPServer {
    private static final int SERVER_PORT = 12346;
    private int threadCount;
    private ExecutorService threadPool;

    public TCPServer(int threadCount) {
        this.threadCount = threadCount > 0 ? threadCount : 1;
        this.threadPool = Executors.newFixedThreadPool(threadCount);
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server running on port " + SERVER_PORT + " with " + threadCount + " threads...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new StrassenMatrixMultiplication(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        threadPool.shutdown();
        try {
            for (int i = 0; i < 60; i++) { // Wait up to 60 seconds
                if (threadPool.isTerminated()) {
                    System.out.println("All tasks have terminated.");
                    break;
                }
                Thread.sleep(1000); // Sleep for 1 second before checking again
            }
            if (!threadPool.isTerminated()) {
                System.out.println("Forcing shutdown...");
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Server shutdown complete.");
    }

}
