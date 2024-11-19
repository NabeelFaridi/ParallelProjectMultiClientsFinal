import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int ThreadCount = 1; // Thread count for the server

        // Create executor services for server and clients
        ExecutorService serverExecutor = Executors.newFixedThreadPool(ThreadCount + 1); // +1 for router
        ExecutorService clientExecutor = Executors.newFixedThreadPool(ThreadCount);

        // Start the Router asynchronously
        TCPServerRouter router = new TCPServerRouter();
        CompletableFuture.runAsync(() -> {
            router.startRouter();
            System.out.println("TCPServerRouter started...");
        }, serverExecutor);

        // Start the Server asynchronously with the specified thread count
        CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
            TCPServer server = new TCPServer(ThreadCount);
            server.startServer();
            System.out.println("TCPServer started with " + ThreadCount + " threads...");
        }, serverExecutor);

        // List to hold metrics from each client
        List<CompletableFuture<Metrics>> clientMetricsFutures = new ArrayList<>();

        // Start multiple clients asynchronously and collect metrics
        for (int i = 0; i < ThreadCount; i++) {
            final int clientId = i + 1;
            CompletableFuture<Metrics> clientFuture = CompletableFuture.supplyAsync(() -> {
                TCPClient client = new TCPClient();
                System.out.println("Starting TCPClient #" + clientId);
                return client.startClient(clientId); // Now returns a Metrics object
            }, clientExecutor);
            clientMetricsFutures.add(clientFuture);
        }

        // Aggregate all metrics once all clients are done
        CompletableFuture<Void> allClientsFuture = CompletableFuture.allOf(clientMetricsFutures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    long totalExecutionTime = 0;
                    double totalSpeedUp = 0;
                    double totalEfficiency = 0;

                    int count = 0;
                    for (CompletableFuture<Metrics> future : clientMetricsFutures) {
                        try {
                            Metrics metrics = future.get(); // Retrieve each client's metrics
                            if (metrics != null) {
                                totalExecutionTime += metrics.executionTime;
                                totalSpeedUp += metrics.speedUp;
                                totalEfficiency += metrics.efficiency;
                                count++;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Print aggregated metrics
                    System.out.println(String.format("Average Execution Time: %d ns", totalExecutionTime / count));
                    System.out.println(String.format("Average Speed Up: %.4f", totalSpeedUp / count));
                    System.out.printf("Average Efficiency: %.3f%%%n", (totalEfficiency / count) * 100);
                });

        // Shutdown all services after clients and server are done
        allClientsFuture.thenCompose(v -> serverFuture) // Wait for server to complete
                .thenRunAsync(() -> {
                    System.out.println("All clients and server tasks are complete. Shutting down router...");
                    router.shutdownRouter(); // Graceful router shutdown
                    System.out.println("TCPServerRouter shutdown complete.");
                }, serverExecutor)
                .thenRun(() -> {
                    System.out.println("Shutting down executors...");
                    serverExecutor.shutdown();
                    clientExecutor.shutdown();
                    System.out.println("All executors have been shut down.");
                })
                .join(); // Wait until all tasks are done
    }
}
