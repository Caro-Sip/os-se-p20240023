import java.util.concurrent.*;

/**
 * Task 2A: Print HELLO BEFORE Semaphores
 * 
 * Three concurrent processes attempt to print letters:
 * - Process 1: H, E
 * - Process 2: L (first)
 * - Process 3: O
 * 
 * Without semaphore ordering, output is unpredictable.
 */
class hello_before {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        System.out.println("=== HELLO BEFORE Semaphores ===");
        System.out.println("Running 5 iterations to show unpredictable ordering...\n");
        
        // Run multiple iterations to show randomness
        for (int iteration = 1; iteration <= 5; iteration++) {
            System.out.print("Iteration " + iteration + ": ");
            
            // Barrier to synchronize the start of all threads for this iteration
            CyclicBarrier barrier = new CyclicBarrier(3);
            
            executor.submit(() -> {
                try {
                    barrier.await(); // Synchronize start
                    // Process 1: print H and E
                    System.out.print("H");
                    Thread.sleep(Math.random() > 0.5 ? 5 : 0);
                    System.out.print("E");
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            executor.submit(() -> {
                try {
                    barrier.await(); // Synchronize start
                    // Process 2: print first L
                    Thread.sleep(Math.random() > 0.5 ? 5 : 0);
                    System.out.print("L");
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            executor.submit(() -> {
                try {
                    barrier.await(); // Synchronize start
                    // Process 3: print second L and O
                    Thread.sleep(Math.random() > 0.5 ? 5 : 0);
                    System.out.print("L");
                    System.out.print("O");
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // Wait for all threads to complete
            Thread.sleep(100);
            System.out.println();
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("\nNotice: The order is unpredictable! Sometimes you might see:");
        System.out.println("  HELLO (correct by chance)");
        System.out.println("  LEHLO (L printed first due to race condition)");
        System.out.println("  ELLHO (E before L due to timing)");
        System.out.println("  ...and other permutations");
        System.out.println("\nThis is why we need semaphores for ordering!");
    }
}
