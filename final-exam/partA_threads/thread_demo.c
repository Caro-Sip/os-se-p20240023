#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/syscall.h>

#define NUM_THREADS 5

// Structure to pass data to threads
typedef struct {
    int thread_idx;
} thread_data_t;

// Thread function
void* worker_thread(void* arg) {
    thread_data_t* data = (thread_data_t*)arg;
    int idx = data->thread_idx;
    
    // Get Linux TID (Thread ID)
    pid_t tid = syscall(SYS_gettid);
    
    // Compute a value
    long computed_value = (idx + 1) * 111;
    
    printf("Worker %d: System Thread ID (TID) = %d, Computed Value = %ld\n", 
           idx, tid, computed_value);
           
    // Let's sleep a little bit so we can observe the thread mapping if needed
    sleep(10);
    
    // Return computed value
    return (void*)computed_value;
}

int main() {
    pthread_t threads[NUM_THREADS];
    thread_data_t thread_data[NUM_THREADS];
    int rc;
    
    printf("Main thread starting. PID = %d. Spawning %d worker threads...\n", getpid(), NUM_THREADS);
    
    // Spawn threads
    for (int i = 0; i < NUM_THREADS; i++) {
        thread_data[i].thread_idx = i;
        rc = pthread_create(&threads[i], NULL, worker_thread, &thread_data[i]);
        if (rc) {
            fprintf(stderr, "Error: pthread_create failed with code %d\n", rc);
            exit(EXIT_FAILURE);
        }
    }
    
    long total_sum = 0;
    
    // Join threads and collect results
    for (int i = 0; i < NUM_THREADS; i++) {
        void* status;
        rc = pthread_join(threads[i], &status);
        if (rc) {
            fprintf(stderr, "Error: pthread_join failed with code %d\n", rc);
            exit(EXIT_FAILURE);
        }
        long val = (long)status;
        printf("Main thread: Joined Worker %d, collected result = %ld\n", i, val);
        total_sum += val;
    }
    
    printf("Summary: Original %d threads joined successfully. Total Sum = %ld\n", NUM_THREADS, total_sum);
    printf("\n=== CURVEBALL A: Spawning 2 EXTRA worker threads (ONLY after originals joined) ===\n\n");
    
    // === CURVEBALL A: Spawn 2 extra workers AFTER originals have joined ===
    pthread_t extra_threads[2];
    thread_data_t extra_data[2];
    
    for (int i = 0; i < 2; i++) {
        extra_data[i].thread_idx = NUM_THREADS + i;  // IDs 5, 6
        rc = pthread_create(&extra_threads[i], NULL, worker_thread, &extra_data[i]);
        if (rc) {
            fprintf(stderr, "Error: pthread_create failed for extra worker %d\n", i);
            exit(EXIT_FAILURE);
        }
    }
    
    printf("Main thread: Extra workers spawned. PID = %d\n", getpid());
    printf(">>> NOW: In another terminal, run: ps -eLo pid,lwp,comm | grep thread_demo\n");
    printf(">>> You should see 3 LWPs (threads 5, 6, and main) for the next ~10 seconds\n");
    printf("Main thread: Waiting 10 seconds before joining extra workers...\n\n");
    
    sleep(10);  // Give time to observe LWPs in other terminal
    
    printf("\n>>> Now joining the 2 extra threads...\n\n");
    
    // Join the 2 extra threads
    long extra_sum = 0;
    for (int i = 0; i < 2; i++) {
        void* status;
        rc = pthread_join(extra_threads[i], &status);
        if (rc) {
            fprintf(stderr, "Error: pthread_join failed for extra worker\n");
            exit(EXIT_FAILURE);
        }
        long val = (long)status;
        printf("Main thread: Joined Extra Worker %d, collected result = %ld\n", NUM_THREADS + i, val);
        extra_sum += val;
    }
    
    printf("\n=== FINAL SUMMARY ===\n");
    printf("Original 5 workers sum: %ld\n", total_sum);
    printf("Extra 2 workers sum: %ld\n", extra_sum);
    printf("Total workers: 7 (5 original + 2 extra)\n");
    printf("All threads joined successfully.\n");
    printf(">>> NOW: Verify extra threads are GONE: ps -eLo pid,lwp,comm | grep thread_demo\n");
    printf(">>> You should only see 1 LWP (main thread) or no results\n");
    return 0;
}