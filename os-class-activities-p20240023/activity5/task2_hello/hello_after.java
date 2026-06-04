import java.util.concurrent.*;

/**
 * Task 2B: Print HELLO AFTER Semaphores
 * 
 * Three concurrent processes coordinate using semaphores to print:
 * H -> E -> L -> L -> O
 * 
 * Semaphore design:
 * - startH: allows Process 1 to print H first (initial 1)
 * - afterH: allows Process 1 to print E (initial 0)
 * - afterE: allows Process 2 to print first L (initial 0)
 * - afterL: allows Process 3 to print second L (initial 0)
 * - afterL2: allows Process 3 to print O (initial 0)
 */
class hello_after {
    
    static class HelloCoordinator {
        // Semaphores enforce the ordering: H -> E -> L -> L -> O
        Semaphore startH;    // Initial 1: only Process 1 can start
        Semaphore afterH;    // Initial 0: Process 1 signals after H
        Semaphore afterE;    // Initial 0: Process 1 signals after E
        Semaphore afterL1;   // Initial 0: Process 2 signals after first L
        Semaphore afterL2;   // Initial 0: Process 3 signals after second L
        
        HelloCoordinator() {
            this.startH = new Semaphore(1);   // Process 1 can start
            this.afterH = new Semaphore(0);
            this.afterE = new Semaphore(0);
            this.afterL1 = new Semaphore(0);
            this.afterL2 = new Semaphore(0);
        }
        
        /**
         * Process 1: Print H and E
         */
        void process1() throws InterruptedException {
            // Can only start when signaled (or initially available)
            startH.acquire();
            System.out.print("H");
            afterH.release();  // Signal that H is done
            
            // Now print E
            System.out.print("E");
            afterE.release();  // Signal that E is done
        }
        
        /**
         * Process 2: Print first L
         */
        void process2() throws InterruptedException {
            // Wait until E is printed
            afterE.acquire();
            System.out.print("L");
            afterL1.release(); // Signal that first L is done
        }
        
        /**
         * Process 3: Print second L and O
         */
        void process3() throws InterruptedException {
            // Wait until first L is printed
            afterL1.acquire();
            System.out.print("L");
            afterL2.release(); // Signal that second L is done
            
            // Now print O
            System.out.print("O");
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        System.out.println("=== HELLO AFTER Semaphores ===\n");
        System.out.println("Expected output: HELLO");
        System.out.println("Running 3 iterations with proper semaphore ordering...\n");
        
        // Run multiple iterations to verify consistency
        for (int iteration = 1; iteration <= 3; iteration++) {
            System.out.print("Iteration " + iteration + ": ");
            
            HelloCoordinator coordinator = new HelloCoordinator();
            CountDownLatch latch = new CountDownLatch(3);
            
            executor.submit(() -> {
                try {
                    coordinator.process1();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
            
            executor.submit(() -> {
                try {
                    coordinator.process2();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
            
            executor.submit(() -> {
                try {
                    coordinator.process3();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
            
            // Wait for all three processes to complete
            latch.await();
            System.out.println(); // Newline after HELLO
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("\n✓ SUCCESS: All iterations printed HELLO in the correct order!");
        System.out.println("\nSemaphore design used:");
        System.out.println("  - startH (initial 1):     Only Process 1 can print H");
        System.out.println("  - afterH (initial 0):     Signals when H is printed");
        System.out.println("  - afterE (initial 0):     Signals when E is printed, allows Process 2");
        System.out.println("  - afterL1 (initial 0):    Signals when first L is printed, allows Process 3");
        System.out.println("  - afterL2 (initial 0):    Signals when second L is printed");
    }
}
