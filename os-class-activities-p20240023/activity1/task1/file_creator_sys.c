#include <unistd.h>
#include <fcntl.h>

int main() {
    const char *message = "Hello from Operating Systems class!\n";
    
    int destination = open("output.txt", O_WRONLY | O_CREAT | O_TRUNC, 0644);
    if(destination < 0) {
        const char *err = "Error: cannot create output.txt\n";
        write(2, err, 32);
        return 1;
    }

    write(destination, message, 36);
    close(destination);

    const char *outputMsg = "File created successfully!\n";
    write(1, outputMsg, 27);
    return 0;
}