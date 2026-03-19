#include <unistd.h>
#include <fcntl.h>

int main() {
    int destination = open("output.txt", O_RDONLY);
    if(destination < 0) {
        const char *err = "Error opening file";
        return 1;
    }

    char buffer[256];
    ssize_t bytesRead;
    while((bytesRead = read(destination, buffer, sizeof(buffer))) > 0){
        write(1, buffer, bytesRead);
    }

    close(destination);
    return 0;
}