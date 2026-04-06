---
created: 2026-03-12
course: "[[OS]]"
---
# OS Lab 1 Submission

- **Student Name:** SUON Caro
- **Student ID:** p20240023

---

## Task 1: Operating System Identification

I use WSL to virtualize linux jammy jellyfish 22.04.5 LTS

<!-- Insert your screenshot for Task 1 below: -->
<!-- SCREENSHOT REQUIREMENT: Show the terminal after running uname -a and lsb_release -a, or the contents of your task1_os_info.txt file. -->
![task1](os-lab-p20240023/lab1/images/task1.png)

---

## Task 2: Essential Linux File and Directory Commands

I create, read, update, and delete linux shell commands on file a and b and output them into text files.

<!-- Insert your screenshot for Task 2 below: -->
<!-- SCREENSHOT REQUIREMENT: Show the terminal running the file manipulation commands (mkdir, touch, cp, mv, rm) or the final cat of your task2_file_commands.txt file. -->
![task2](os-lab-p20240023/lab1/images/task2.png)

---

## Task 3: Package Management Using APT

remove saves the configuration while purge remove all instances including the configuration of the package

<!-- Insert your screenshot for Task 3 below: -->
<!-- SCREENSHOT REQUIREMENT: Show the output of ls -ld /etc/mc after running apt-get remove (folder still exists) versus after running apt-get purge (folder is gone). -->
![task3](task3.png)

---

## Task 4: Programs vs Processes (Single Process)

I ran the process, in this case sleep with 120 seconds as arg, in the background using the ampersand symbol `&` and I checked the processes running on my system with ps and output them into the `task4_process_list.txt` (forgot to cat)

<!-- Insert your screenshot for Task 4 below: -->
<!-- SCREENSHOT REQUIREMENT: Show the terminal where you ran sleep 120 & and the subsequent ps output showing the sleep process running. -->
![task4](os-lab-p20240023/lab1/images/task4.png)

---

## Task 5: Installing Real Applications & Observing Multitasking

I ran 3 background processes simulating multitasking and read the processes with `ps` again into `task5_multitasking.txt`

<!-- Insert your screenshot for Task 5 below: -->
<!-- SCREENSHOT REQUIREMENT: Show the terminal ps output capturing the multiple background tasks (sleep and python3 server) running at the same time. -->
![task5](task5.png)

---

## Task 6: Virtualization and Hypervisor Detection

My system is running on a virtualization of the Linux through WSL with the hypervisor vendor from Microsoft software as my host machine name is mago

<!-- Insert your screenshot for Task 6 below: -->
<!-- SCREENSHOT REQUIREMENT: Show the terminal output of the systemd-detect-virt and lscpu commands. -->
![task6](task6.png)