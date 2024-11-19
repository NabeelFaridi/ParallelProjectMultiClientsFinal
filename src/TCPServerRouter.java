import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class TCPServerRouter {
    private static final int ROUTER_PORT = 12345;
    private ConcurrentHashMap<Integer, Socket> connectionMap = new ConcurrentHashMap<>();
    private volatile boolean isRunning = true; // Flag to control server loop
    private ServerSocket serverSocket;

    public void startRouter() {
        try {
            serverSocket = new ServerSocket(ROUTER_PORT);
            System.out.println("Server Router is running on port " + ROUTER_PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Route the client to a server using SThread
                SThread thread = new SThread(clientSocket, this);
                new Thread(thread).start();
            }
        } catch (IOException e) {
            if (isRunning) { // Only print stack trace if not in shutdown mode
                e.printStackTrace();
            }
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Server Router has stopped.");
        }
    }

    public void shutdownRouter() {
        isRunning = false; // Stop accepting new connections
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Release the server socket
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Adds a client connection to the connectionMap
    public void addConnection(int clientId, Socket clientSocket) {
        connectionMap.put(clientId, clientSocket);
        System.out.println("Client " + clientId + " added to connection map.");
    }

    // Removes a client connection from the connectionMap
    public void removeConnection(int clientId) {
        Socket removedSocket = connectionMap.remove(clientId);
        if (removedSocket != null) {
            System.out.println("Client " + clientId + " removed from connection map.");
            try {
                removedSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket for client " + clientId);
                e.printStackTrace();
            }
        }
    }
}
