#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>

int main() {
    uid_t ruid = getuid();   // Real UID
    uid_t euid = geteuid();  // Effective UID
    
    printf("Real UID: %d\n", ruid);
    printf("Effective UID: %d\n", euid);
    
    if (ruid != euid) {
        printf("Setuid bit is active! Running with escalated/different effective privileges.\n");
    } else {
        printf("Real and Effective UIDs are the same. (No privilege elevation).\n");
    }
    
    return 0;
}
