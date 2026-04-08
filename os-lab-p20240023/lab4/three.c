#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>

int main() {
    for (int i = 0; i < 3; i++) {
        pid_t pid = fork();

        if (pid == 0) {
            // Child process
            printf("Child %d (PID: %d) started. Sleeping for 20s...\n", i+1, getpid());
            sleep(20);
            exit(0); // CRITICAL: Child must exit so it doesn't loop and fork!
        }
    }

    // Parent waits for all 3 children
    printf("Parent (PID: %d) waiting for 3 children...\n", getpid());
    for (int i = 0; i < 3; i++) {
        wait(NULL);
    }

    printf("All children finished. Parent exiting.\n");
    return 0;
}
