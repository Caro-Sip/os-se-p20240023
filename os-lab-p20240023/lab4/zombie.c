#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>

int main() {
    pid_t pid = fork();

    if (pid > 0) {
        // Parent process
        printf("Parent (PID: %d) sleeping for 10s...\n", getpid());
        sleep(10); 
        
        printf("Parent calling wait() to reap the zombie...\n");
        wait(NULL); // This reaps the child
        
        printf("Child reaped. Parent sleeping another 10s. Check 'ps' now!\n");
        sleep(10);
    } else if (pid == 0) {
        // Child process
        printf("Child (PID: %d) exiting immediately.\n", getpid());
        exit(0);
    }
    return 0;
}
