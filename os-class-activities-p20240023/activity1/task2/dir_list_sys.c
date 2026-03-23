#include <fcntl.h>
#include <unistd.h>
#include <dirent.h>
#include <sys/stat.h>
#include <string.h>
#include <stdio.h>

int main() {
    char buffer[512];
    DIR *dir = opendir(".");
    if (dir == NULL) {
        const char *errorMsg = "Error opening directory";
        write(2,errorMsg,strlen(errorMsg));
    }

    struct dirent *entry;
    struct stat fileStat;
    int len = snprintf(buffer, sizeof(buffer), "%-30s %10s\n", "Filename", "Size (bytes)");
    write(1, buffer,len);

    len = snprintf(buffer, sizeof(buffer), "%-30s %10s\n", "------------------------------", "----------");
    write(1, buffer, len);
    // or strlen of the buffer varia

    while ((entry = readdir(dir)) != NULL) {
        if (stat(entry->d_name, &fileStat) == 0) {
            int len = snprintf(buffer, sizeof(buffer), "%-30s %10ld\n", entry->d_name, fileStat.st_size);
            write(1, buffer, len);
        }
    }
}