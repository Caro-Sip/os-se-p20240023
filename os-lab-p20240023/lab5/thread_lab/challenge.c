#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <signal.h>
#include <unistd.h>
#include <string.h>

/* Global flag to control thread execution */
volatile int keep_running = 1;

/* Signal handler for SIGINT (Ctrl+C) */
void signal_handler(int signum) {
    if (signum == SIGINT) {
        printf("\n[SIGINT received] Shutting down threads...\n");
        keep_running = 0;
    }
}

/* Worker thread function */
void* worker_thread(void* arg) {
    int thread_num = *(int*)arg;
    free(arg);  /* Free allocated memory for thread number */
    
    pthread_t thread_id = pthread_self();
    
    printf("Thread %d started with ID: %lu\n", thread_num, (unsigned long)thread_id);
    
    while (keep_running) {
        printf("Thread %d (ID: %lu) is running...\n", thread_num, (unsigned long)thread_id);
        sleep(1);
    }
    
    printf("Thread %d exiting...\n", thread_num);
    pthread_exit(NULL);
}

int main(void) {
    pthread_t thread1, thread2;
    struct sigaction sa;
    
    /* Set up signal handler for SIGINT */
    memset(&sa, 0, sizeof(struct sigaction));
    sa.sa_handler = signal_handler;
    sigaction(SIGINT, &sa, NULL);
    
    printf("Main thread: Starting worker threads...\n");
    printf("Press Ctrl+C to gracefully shut down.\n\n");
    
    /* Create first worker thread */
    int* t1_num = malloc(sizeof(int));
    *t1_num = 1;
    if (pthread_create(&thread1, NULL, worker_thread, t1_num) != 0) {
        perror("pthread_create (thread1)");
        free(t1_num);
        return EXIT_FAILURE;
    }
    
    /* Create second worker thread */
    int* t2_num = malloc(sizeof(int));
    *t2_num = 2;
    if (pthread_create(&thread2, NULL, worker_thread, t2_num) != 0) {
        perror("pthread_create (thread2)");
        free(t2_num);
        return EXIT_FAILURE;
    }
    
    /* Wait for both threads to complete */
    if (pthread_join(thread1, NULL) != 0) {
        perror("pthread_join (thread1)");
        return EXIT_FAILURE;
    }
    
    if (pthread_join(thread2, NULL) != 0) {
        perror("pthread_join (thread2)");
        return EXIT_FAILURE;
    }
    
    /* Print exit message */
    printf("All threads cleanly exited. Goodbye.\n");
    
    return EXIT_SUCCESS;
}