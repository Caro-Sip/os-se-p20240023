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
    
    printf("Summary: All threads joined successfully. Total Sum of Computed Values = %ld\n", total_sum);
    return 0;
}
