#include <fcntl.h>
#include <unistd.h>
#include <dirent.h>
#include <sys/stat.h>
#include <string.h>
#include <stdio.h>

int main() {
    DIR *dir = opendir(".");
    if (dir == NULL) {
        const char *errorMsg = "Error opening directory";
        write(2,errorMsg,strlen(errorMsg));
    }

    struct dirent *entry;
    struct stat fileStat;
    char buffer[256];
    snprintf(buffer, sizeof(buffer), "%-30s %10ld\n", "Filename", "Size (bytes)");
}