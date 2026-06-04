import java.util.*;
import java.util.concurrent.*;

/**
 * Task 1A: Particle Pair Buffer BEFORE Semaphores
 * 
 * This version intentionally lacks proper synchronization.
 * Multiple producers add particle pairs to a shared buffer.
 * One consumer removes particles without proper ordering or protection.
 * 
 * Expected errors: buffer overflow, underflow, or incorrect pairs.
 */
class particles_before {
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
        
        // NO semaphores, NO mutex - intentionally unsafe!
        
        void addPair(String machineId) throws Exception {
            // Check if buffer is full BEFORE adding (but without protection!)
            if (buffer.size() >= BUFFER_SIZE) {
                throw new Exception("The producing machine is broken");
            }
            
            // Simulate some work
            Thread.sleep(1);
            
            // Add P1 and P2
            int pairId = pairCounter++;
            Particle p1 = new Particle(machineId, pairId, "P1");
            Particle p2 = new Particle(machineId, pairId, "P2");
            
            buffer.add(p1);
            buffer.add(p2);
            producedPairs++;
        }
        
        void removePair() throws Exception {
            // Check if buffer is empty BEFORE removing
            if (buffer.isEmpty() || buffer.size() < 2) {
                throw new Exception("The packaging machine is broken");
            }
            
            // Simulate some work
            Thread.sleep(1);
            
            // Remove two particles (but they might not be from the same pair!)
            Particle p1 = buffer.remove(0);
            Particle p2 = buffer.remove(0);
            
            // Verify they're from the same pair
            if (!p1.machineId.equals(p2.machineId) || p1.pairId != p2.pairId) {
                throw new Exception("Pairs are incorrect");
            }
            
            packagedPairs++;
        }
        
        void printStatus() {
            System.out.printf("Produced pairs: %d | Packaged pairs: %d | Buffer particles: %d%n",
                    producedPairs, packagedPairs, buffer.size());
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        ParticleBuffer buffer = new ParticleBuffer();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_PRODUCERS + 1);
        boolean[] running = {true};
        
        // Producer threads
        for (int i = 1; i <= NUM_PRODUCERS; i++) {
            final int machineId = i;
            executor.submit(() -> {
                try {
                    while (running[0]) {
                        buffer.addPair(String.valueOf(machineId));
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    running[0] = false;
                }
            });
        }
        
        // Consumer thread
        executor.submit(() -> {
            try {
                while (running[0]) {
                    if (!buffer.buffer.isEmpty() && buffer.buffer.size() >= 2) {
                        buffer.removePair();
                    }
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                running[0] = false;
            }
        });
        
        // Status printer thread
        executor.submit(() -> {
            try {
                while (running[0]) {
                    buffer.printStatus();
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                System.err.println("Error printing status");
            }
        });
        
        // Wait for termination or user interrupt
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
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
