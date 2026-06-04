import java.util.*;
import java.util.concurrent.*;

/**
 * Task 1B: Particle Pair Buffer AFTER Semaphores
 * 
 * Uses three semaphores to safely manage:
 * - empty_pairs: counts available pair slots (initial 50)
 * - full_pairs: counts complete pairs ready to package (initial 0)
 * - mutex: protects the shared buffer (initial 1)
 * 
 * Producers wait for empty space, add pairs, then signal availability.
 * Consumer waits for complete pairs, removes them safely.
 */
class particles_after {
    static final int BUFFER_SIZE = 100;  // 50 particle pairs
    static final int NUM_PRODUCERS = 3;
    
    static class Particle {
        String machineId;
        int pairId;
        String type; // P1 or P2
        
        Particle(String machineId, int pairId, String type) {
            this.machineId = machineId;
            this.pairId = pairId;
            this.type = type;
        }
        
        @Override
        public String toString() {
            return "M" + machineId + "-" + pairId + "-" + type;
        }
    }
    
    static class ParticleBuffer {
        List<Particle> buffer = new ArrayList<>();
        int producedPairs = 0;
        int packagedPairs = 0;
        int pairCounter = 0;
        
        // Semaphores for synchronization
        Semaphore emptyPairs;    // Counts available pair spaces (initial 50)
        Semaphore fullPairs;     // Counts complete pairs to package (initial 0)
        Semaphore mutex;         // Protects shared buffer access (initial 1)
        
        ParticleBuffer() {
            this.emptyPairs = new Semaphore(BUFFER_SIZE / 2);  // 50 pairs initially
            this.fullPairs = new Semaphore(0);
            this.mutex = new Semaphore(1);
        }
        
        /**
         * Producer: Create and add a particle pair to the buffer
         */
        void addPair(String machineId) throws InterruptedException {
            // Wait until there's space for a pair (2 particles)
            emptyPairs.acquire();
            
            // Enter critical section to modify buffer
            mutex.acquire();
            try {
                // Verify we still have space (sanity check)
                if (buffer.size() >= BUFFER_SIZE) {
                    throw new RuntimeException("The producing machine is broken");
                }
                
                // Create the pair
                int pairId = pairCounter++;
                Particle p1 = new Particle(machineId, pairId, "P1");
                Particle p2 = new Particle(machineId, pairId, "P2");
                
                // Add both particles consecutively
                buffer.add(p1);
                buffer.add(p2);
                
                producedPairs++;
            } finally {
                mutex.release();
            }
            
            // Signal that a complete pair is now available for packaging
            fullPairs.release();
        }
        
        /**
         * Consumer: Remove and package a particle pair from the buffer
         */
        void removePair() throws InterruptedException {
            // Wait until there's a complete pair available
            fullPairs.acquire();
            
            // Enter critical section to modify buffer
            mutex.acquire();
            try {
                // Verify we have at least 2 particles (sanity check)
                if (buffer.size() < 2) {
                    throw new RuntimeException("The packaging machine is broken");
                }
                
                // Remove the two particles
                Particle p1 = buffer.remove(0);
                Particle p2 = buffer.remove(0);
                
                // Verify they belong to the same pair
                if (!p1.machineId.equals(p2.machineId) || p1.pairId != p2.pairId) {
                    throw new RuntimeException("Pairs are incorrect");
                }
                
                packagedPairs++;
            } finally {
                mutex.release();
            }
            
            // Signal that a pair slot is now empty
            emptyPairs.release();
        }
        
        void printStatus() {
            System.out.printf("Produced pairs: %d | Packaged pairs: %d | Buffer particles: %d%n",
                    producedPairs, packagedPairs, buffer.size());
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        ParticleBuffer buffer = new ParticleBuffer();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_PRODUCERS + 2);
        boolean[] running = {true};
        
        System.out.println("=== Particle Pair Buffer WITH Semaphores ===");
        System.out.println("Producers: " + NUM_PRODUCERS);
        System.out.println("Buffer capacity: " + BUFFER_SIZE + " particles (50 pairs)");
        System.out.println("Semaphores: empty_pairs=" + buffer.emptyPairs.availablePermits() + 
                         ", full_pairs=" + buffer.fullPairs.availablePermits() + 
                         ", mutex=" + buffer.mutex.availablePermits());
        System.out.println();
        
        // Producer threads
        for (int i = 1; i <= NUM_PRODUCERS; i++) {
            final int machineId = i;
            executor.submit(() -> {
                try {
                    while (running[0]) {
                        buffer.addPair(String.valueOf(machineId));
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // Consumer thread
        executor.submit(() -> {
            try {
                while (running[0]) {
                    buffer.removePair();
                    Thread.sleep(15);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Status printer thread
        executor.submit(() -> {
            try {
                while (running[0]) {
                    buffer.printStatus();
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Wait for termination or user interrupt
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n=== Shutdown requested ===");
            running[0] = false;
            executor.shutdown();
            try {
                executor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }));
        
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }
}
