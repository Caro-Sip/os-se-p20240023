# Class Activity 1 — System Calls in Practice

- **Student Name:** Suon Caro
- **Student ID:** p20240023
- **Date:** [Date of Submission]

---

## Warm-Up: Hello System Call

Screenshot of running `hello_syscall.c` on Linux:

![hello syscall](screenshots/hello_syscall.png)

Screenshot of running `hello_winapi.c` on Windows (CMD/PowerShell/VS Code):

![Hello WinAPI](screenshots/hello_winapi.png)

Screenshot of running `copyfilesyscall.c` on Linux:

![Copy file syscall](screenshots/copyfilesyscall.png)

---

## Task 1: File Creator & Reader

### Part A — File Creator

**Describe your implementation:** I learnt that library use the c standard library `<stdio.h>` while the system call in linux uses `<unistd.h>` & `<fcntl.h>`

**Version A — Library Functions (`file_creator_lib.c`):**

<!-- Screenshot: gcc -o file_creator_lib file_creator_lib.c && ./file_creator_lib && cat output.txt -->

![Task 1A - Library](screenshots/task1_creator_lib.png)

**Version B — POSIX System Calls (`file_creator_sys.c`):**

<!-- Screenshot: gcc -o file_creator_sys file_creator_sys.c && ./file_creator_sys && cat output.txt -->
![Task 1A - Syscall](screenshots/task1_creator_sys.png)

**Questions:**

1. **What flags did you pass to `open()`? What does each flag mean?**

   > `int destination = open("output.txt", O_WRONLY | O_CREAT | O_TRUNC, 0644);`
   > - "output.txt" is the name and type of the file
   > - `O_WRONLY` so the file is write only
   > - `O_CREAT` if file doesn't exist, create it
   > - `O_TRUNC` erase existing contents
   > - `0644` owner read and write permission

2. **What is `0644`? What does each digit represent?**

   > `0644` owner read and write permission

3. **What does `fopen("output.txt", "w")` do internally that you had to do manually?**

   > it is equivalent to `int destination = open("output.txt", O_WRONLY | O_CREAT | O_TRUNC, 0644);`
   > where `w` is write permission, and same file name

### Part B — File Reader & Display

**Describe your implementation:** I use the `open()` function and store it directory as destination and pass it into read with read only permission from the output.txt. And then i create a buffer variable size of 

**Version A — Library Functions (`file_reader_lib.c`):**

![Task 1B - Library](screenshots/task1_reader_lib.png)

**Version B — POSIX System Calls (`file_reader_sys.c`):**

![Task 1B - Syscall](screenshots/task1_reader_sys.png)

**Questions:**

1. **What does `read()` return? How is this different from `fgets()`?**

   > It returns the number of bytes read, it return 0 when it reach end of file but -1 if an error occure

2. **Why do you need a loop when using `read()`? When does it stop?**

   > the byte buffer is reading 256 bytes per loop iteration, so it is reading until the end of file

---

## Task 2: Directory Listing & File Info

**Describe your implementation:** [Your notes]

### Version A — Library Functions (`dir_list_lib.c`)

![Task 2 - Version A](screenshots/task2_lib.png)

### Version B — System Calls (`dir_list_sys.c`)

![Task 2 - Version B](screenshots/task2_sys.png)

### Questions

1. **What struct does `readdir()` return? What fields does it contain?**

   > [Your answer]

2. **What information does `stat()` provide beyond file size?**

   > [Your answer]

3. **Why can't you `write()` a number directly — why do you need `snprintf()` first?**

   > [Your answer]

---

## Optional Bonus: Windows API (`file_creator_win.c`)

Screenshot of running on Windows:

![Task 1 - Windows](screenshots/task1_win.png)

### Bonus Questions

1. **Why does Windows use `HANDLE` instead of integer file descriptors?**

   > [Your answer]

2. **What is the Windows equivalent of POSIX `fork()`? Why is it different?**

   > [Your answer]

3. **Can you use POSIX calls on Windows?**

   > [Your answer]

---

## Task 3: strace Analysis

**Describe what you observed:** [What surprised you about the strace output? How many more system calls did the library version make?]

### strace Output — Library Version (File Creator)

<!-- Screenshot: strace -e trace=openat,read,write,close ./file_creator_lib -->
<!-- IMPORTANT: Highlight/annotate the key system calls in your screenshot -->
![strace - Library version File Creator](screenshots/strace_lib_creator.png)

### strace Output — System Call Version (File Creator)

<!-- Screenshot: strace -e trace=openat,read,write,close ./file_creator_sys -->
<!-- IMPORTANT: Highlight/annotate the key system calls in your screenshot -->
![strace - System call version File Creator](screenshots/strace_sys_creator.png)

### strace Output — Library Version (File Reader or Dir Listing)

![strace - Library version](screenshots/strace_lib_reader.png)

### strace Output — System Call Version (File Reader or Dir Listing)

![strace - System call version](screenshots/strace_sys_reader.png)

### strace -c Summary Comparison

<!-- Screenshot of `strace -c` output for both versions -->
![strace summary - Library](screenshots/strace_summary_lib.png)
![strace summary - Syscall](screenshots/strace_summary_sys.png)

### Questions

1. **How many system calls does the library version make compared to the system call version?**

   > [Your answer — use the `strace -c` counts]

2. **What extra system calls appear in the library version? What do they do?**

   > [Your answer — mention `brk`, `mmap`, `fstat`, etc.]

3. **How many `write()` calls does `fprintf()` actually produce?**

   > [Your answer]

4. **In your own words, what is the real difference between a library function and a system call?**

   > [Your answer]

---

## Task 4: Exploring OS Structure

### System Information

> 📸 Screenshot of `uname -a`, `/proc/cpuinfo`, `/proc/meminfo`, `/proc/version`, `/proc/uptime`:

![System Info](screenshots/task4_system_info.png)

### Process Information

> 📸 Screenshot of `/proc/self/status`, `/proc/self/maps`, `ps aux`:

![Process Info](screenshots/task4_process_info.png)

### Kernel Modules

> 📸 Screenshot of `lsmod` and `modinfo`:

![Kernel Modules](screenshots/task4_modules.png)

### OS Layers Diagram

> 📸 Your diagram of the OS layers, labeled with real data from your system:

![OS Layers Diagram](screenshots/task4_os_layers_diagram.png)

### Questions

1. **What is `/proc`? Is it a real filesystem on disk?**

   > [Your answer]

2. **Monolithic kernel vs. microkernel — which type does Linux use?**

   > [Your answer]

3. **What memory regions do you see in `/proc/self/maps`?**

   > [Your answer]

4. **Break down the kernel version string from `uname -a`.**

   > [Your answer]

5. **How does `/proc` show that the OS is an intermediary between programs and hardware?**

   > [Your answer]

---

## Reflection

What did you learn from this activity? What was the most surprising difference between library functions and system calls?

> [Write your reflection here]