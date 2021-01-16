/**
 * Author : Brian Klein
 * Date : 2/14/19
 * Description : User guided console to run CPU Scheduling Simulator.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

public class MainScheduler {

    static Scanner console = new Scanner(System.in);

    private static ArrayList<PCB> jobQ = new ArrayList();
    private static ArrayList<PCB> ioQ = new ArrayList();
    private static ArrayList<PCB> disk = new ArrayList();
    private static ArrayList<PCB> cpu = new ArrayList();
    private static ArrayList<PCB> readyQ = new ArrayList();
    private static ArrayList<PCB> doneQ = new ArrayList();
    private static int timer = 0;

    private static Comparator<PCB> compareBycpuBurst = new Comparator<PCB>() {
        @Override
        public int compare(PCB object1, PCB object2) {
            return object1.cpuBurstList.get(0).compareTo(object2.cpuBurstList.get(0));
        }
    };

    public static void main(String args[]) throws IndexOutOfBoundsException {

        //variables
        String command;
        String choice;
        int timeQuantum;

        //Introduction
        System.out.println("Brian Klein \nCS 405 \nProject 1\n\n\n"
                + "Welcome to Brian Klein's CPU Scheduling Simulator!\n");

        do {
            //Get user input to select the scheduling algorithm to use here
            System.out.println("\nSelect the scheduling algorithm by entering a number:"
                    + "\n  1  ---  Non-preemtive Shortest Job First"
                    + "\n  2  ---  Round Robin\n");

            command = console.next();

            //algorithm switch
            switch (command) {

                //choose Non-preemtive SJF
                case "1":

                    boolean flag = true;

                    while (flag) {
                        System.out.println("\nYou have seleted the Non-preemptive Shortest Job First scheduling algorithm.");
                        System.out.println("\nSelect Forward or Reverse by typing the corresponding number:"
                                + "\n1 -- Forward"
                                + "\n2 -- Reverse");
                        choice = console.next();
                        if (choice.equals("1")) {
                            SJF(choice);
                            flag = false;
                        } else if (choice.equals("2")) {
                            SJF(choice);
                            flag = false;
                        } else {
                            System.out.println("Invalid selection, please try again.");
                        }
                    }
                    break;

                //choose RR
                case "2":

                    System.out.println("\nYou have seleted the Round Robin scheduling algorithm.");
                    System.out.println("\nEnter the time quantum:\n");
                    timeQuantum = console.nextInt();

                    RR(timeQuantum);

                    break;

                default:

                    //do while loop to repeat options while invalid entry.
                    System.out.println("\nInvalid entry, please select the "
                            + "scheduling algorithm by entering a number:");

                    break;

            }//end algorithm switch

            //loops while the user input is anything but 1 or 2
        } while (!command.equals("1") && !command.equals("2"));
        //end do-while loop

    }//end main method   

    public static void SJF(String choice) {

        //create and load jobs into the Job Queue
        jobQ = jobPool(choice);
        int doneCheck = jobQ.size();

        //print the initial queue info
        System.out.println(initialQueue(jobQ));

        //load the ready queue from the job queue
        readyLoadSJF(jobQ);

        //load a job into the cpu
        cpuLoadSJF();

        //fill the ready queue
        readyLoadSJF(jobQ);

        //print status at timer
        System.out.println(printStatus());

        int check = timer;

        do {
            //increment system clock
            timer = timerTick();
            //check++;

            try {
                for (int i = 0; i < disk.size(); i++) {
                    if (disk.get(i).ioBurstList.get(0) == 0) {
                        disk.get(i).ioBurstList.remove(0);
                        ioQ.add(disk.remove(i));
                        i--;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Timer: " + timer + "\nIndex out of bounds in RR.");
            }
            //if the cpu burst is done move the job to the disk
            try {
                if (cpu.get(0).getCpuBurst() == 0) {

                    //check = 0;
                    shuffleSJF();
                    System.out.println(printStatus());

                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Timer: " + timer + "\nIndex out of bounds in RR shuffle.");
            }

            //testing
            //System.out.println(printStatus(timer, cpu, jobQ, readyQ, disk, ioQ));
        } while (doneQ.size() < doneCheck);

    }//end SJF method

    public static String shuffleSJF() {

        //local variables
        ArrayList<PCB> loadQ;
        String status;

        try {
            //if the PCB in the CPU is done with CPU and IO, remove it
            if (cpu.get(0).cpuBurstList.isEmpty()) {
                doneQ.add(cpu.remove(0));
                cpuLoadSJF();

                //if the IO burst is done, move the job to the waiting queue
                for (int i = 0; i < disk.size(); i++) {
                    if (disk.get(i).getIoBurst() == 0) {
                        ioQ.add(disk.remove(i));
                    }//end if
                }//end for

                //if IO Queue has a job load the Ready Queue from there, otherwise load
                //Ready Queue from the Job Queue
                if (!ioQ.isEmpty()) {
                    loadQ = ioQ;
                } else {
                    loadQ = jobQ;
                }//end if else

                readyLoadSJF(loadQ);
                

                //if the PCB in the CPU is done with CPU but not IO, move it to the disk
            } else if (cpu.get(0).getCpuBurst() == 0) {

                cpu.get(0).cpuBurstList.remove(0);
                if (cpu.get(0).cpuBurstList.isEmpty()) {
                    doneQ.add(cpu.remove(0));
                } else {
                    disk.add(cpu.remove(0));
                }
                cpuLoadSJF();

                //if IO Queue has a job load the Ready Queue from there, otherwise load
                //Ready Queue from the Job Queue
                if (!ioQ.isEmpty()) {
                    loadQ = ioQ;
                } else {
                    loadQ = jobQ;
                }//end if else

                readyLoadSJF(loadQ);
                

                //if the PCB is not done with CPU this is triggered by the time quantum
                //PCB is moved to the Ready Queue
            } else {

                readyQ.add(cpu.remove(0));
                cpuLoadSJF();

                //if IO Queue has a job load the Ready Queue from there, otherwise load
                //Ready Queue from the Job Queue
                if (!ioQ.isEmpty()) {
                    loadQ = ioQ;
                } else {
                    loadQ = jobQ;
                }//end if else

                int diskCheck = disk.size();
                //if the IO burst is done, move the job to the waiting queue
                for (int i = 0; i < diskCheck; i++) {
                    if (disk.get(i).getIoBurst() == 0) {
                        disk.get(i).ioBurstList.remove(0);
                        ioQ.add(disk.get(i));
                        System.out.println("check here");
                    }//end if
                }//end for

                readyLoadSJF(loadQ);
                

            }//end if else

        } catch (IndexOutOfBoundsException e) {
            //System.out.println("Error Detected.");
        }//end try catch

        status = printStatus();

        return status;

    }//end shuffle method

    public static void cpuLoadSJF() {
        if (cpu.isEmpty()) {
            //load job into cpu from ready queue or io queue, whichever is smallest
            if (!readyQ.isEmpty() && !ioQ.isEmpty()) {
                Collections.sort(readyQ, compareBycpuBurst);
                Collections.sort(ioQ, compareBycpuBurst);
                if (readyQ.get(0).cpuBurstList.get(0) <= ioQ.get(0).cpuBurstList.get(0)) {
                    cpu.add(readyQ.remove(0));
                } else {
                    cpu.add(ioQ.remove(0));
                }
            } else if (!readyQ.isEmpty() && ioQ.isEmpty()) {
                Collections.sort(readyQ, compareBycpuBurst);
                cpu.add(readyQ.remove(0));
            } else {
                cpu.add(ioQ.remove(0));
            }
        }
    }//end cpuLoad method

    public static void readyLoadSJF(ArrayList<PCB> loadQ) {

        //take the first two PCBs in the job queue and add them to the ready queue
        while (readyQ.size() < 2) {
            //load another job from job queue to ready queue
            readyQ.add(loadQ.remove(0));
            //Collections.sort(readyQ, compareBycpuBurst);
        }
    }//end readyLoad method

    public static void RR(int timeQuantum) {

        String choice = "1";
        //create and load jobs into the Job Queue
        jobQ = jobPool(choice);
        int doneCheck = jobQ.size();

        //print the initial queue info
        System.out.println(initialQueue(jobQ));

        //load the ready queue from the job queue
        readyLoad(jobQ);

        //load a job into the cpu
        cpuLoad();

        //fill the ready queue
        readyLoad(jobQ);

        //print status at timer
        System.out.println(printStatus());

        int check = timer;

        do {
            //increment system clock
            timer = timerTick();
            check++;

            try {
                for (int i = 0; i < disk.size(); i++) {
                    if (disk.get(i).ioBurstList.get(0) == 0) {
                        disk.get(i).ioBurstList.remove(0);
                        ioQ.add(disk.remove(i));
                        i--;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Timer: " + timer + "\nIndex out of bounds in RR.");
            }
            //shuffle jobs at time quantum
            //else if the cpu burst is done move the job to the disk
            try {
                if (check % timeQuantum == 0) {
                    shuffle();
                    System.out.println(printStatus());
                } else if (cpu.get(0).getCpuBurst() == 0) {

                    check = 0;
                    shuffle();
                    System.out.println(printStatus());

                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Timer: " + timer + "\nIndex out of bounds in RR shuffle.");
            }

            //testing
            //System.out.println(printStatus(timer, cpu, jobQ, readyQ, disk, ioQ));
        } while (doneQ.size() < doneCheck);

    }//end RR method

    public static String shuffle() {

        //local variables
        ArrayList<PCB> loadQ;
        String status;

        try {
            //if the PCB in the CPU is done with CPU and IO, remove it
            if (cpu.get(0).cpuBurstList.isEmpty()) {
                doneQ.add(cpu.remove(0));
                cpuLoad();

                //if the IO burst is done, move the job to the waiting queue
                for (int i = 0; i < disk.size(); i++) {
                    if (disk.get(i).getIoBurst() == 0) {
                        ioQ.add(disk.remove(i));
                    }//end if
                }//end for

                //if IO Queue has a job load the Ready Queue from there, otherwise load
                //Ready Queue from the Job Queue
                if (!ioQ.isEmpty()) {
                    loadQ = ioQ;
                } else {
                    loadQ = jobQ;
                }//end if else

                readyLoad(loadQ);

                //if the PCB in the CPU is done with CPU but not IO, move it to the disk
            } else if (cpu.get(0).getCpuBurst() == 0) {

                cpu.get(0).cpuBurstList.remove(0);
                if (cpu.get(0).cpuBurstList.isEmpty()) {
                    doneQ.add(cpu.remove(0));
                } else {
                    disk.add(cpu.remove(0));
                }
                cpuLoad();

                //if IO Queue has a job load the Ready Queue from there, otherwise load
                //Ready Queue from the Job Queue
                if (!ioQ.isEmpty()) {
                    loadQ = ioQ;
                } else {
                    loadQ = jobQ;
                }//end if else

                readyLoad(loadQ);

                //if the PCB is not done with CPU this is triggered by the time quantum
                //PCB is moved to the Ready Queue
            } else {

                readyQ.add(cpu.remove(0));
                cpuLoad();

                //if IO Queue has a job load the Ready Queue from there, otherwise load
                //Ready Queue from the Job Queue
                if (!ioQ.isEmpty()) {
                    loadQ = ioQ;
                } else {
                    loadQ = jobQ;
                }//end if else

                int diskCheck = disk.size();
                //if the IO burst is done, move the job to the waiting queue
                for (int i = 0; i < diskCheck; i++) {
                    if (disk.get(i).getIoBurst() == 0) {
                        disk.get(i).ioBurstList.remove(0);
                        ioQ.add(disk.get(i));
                        System.out.println("check here");
                    }//end if
                }//end for

                readyLoad(loadQ);

            }//end if else

        } catch (IndexOutOfBoundsException e) {
            //System.out.println("Error Detected.");
        }//end try catch

        status = printStatus();

        return status;

    }//end shuffle method

    public static ArrayList<PCB> jobPool(String choice) {

        //create ArrayLists
        ArrayList<Integer> cpu1 = new ArrayList();
        ArrayList<Integer> io1 = new ArrayList();
        ArrayList<Integer> cpu2 = new ArrayList();
        ArrayList<Integer> io2 = new ArrayList();
        ArrayList<Integer> cpu3 = new ArrayList();
        ArrayList<Integer> io3 = new ArrayList();
        ArrayList<Integer> cpu4 = new ArrayList();
        ArrayList<Integer> io4 = new ArrayList();
        ArrayList<Integer> cpu5 = new ArrayList();
        ArrayList<Integer> io5 = new ArrayList();

        //hardcode ArrayLists
//        cpu1.addAll(Arrays.asList(5, 6, 3));
//        io1.addAll(Arrays.asList(2, 3));
//        cpu2.addAll(Arrays.asList(3, 2, 4, 3));
//        io2.addAll(Arrays.asList(1, 2, 1));
//        cpu3.addAll(Arrays.asList(2, 2, 2));
//        io3.addAll(Arrays.asList(6, 6));
//        cpu4.addAll(Arrays.asList(3, 2));
//        io4.addAll(Arrays.asList(4));
//        cpu5.addAll(Arrays.asList(6, 6));
//        io5.addAll(Arrays.asList(5));
        
        cpu1.addAll(Arrays.asList(4, 5, 3));
        io1.addAll(Arrays.asList(3, 2));
        cpu2.addAll(Arrays.asList(2, 4));
        io2.addAll(Arrays.asList(1));
        cpu3.addAll(Arrays.asList(3, 2, 2));
        io3.addAll(Arrays.asList(2, 3));
        cpu4.addAll(Arrays.asList(5, 2));
        io4.addAll(Arrays.asList(3));
        cpu5.addAll(Arrays.asList(4, 2));
        io5.addAll(Arrays.asList(2));

        ArrayList<PCB> jobQue = new ArrayList();

        //create jobs
        if (choice.equals("1")) {
            PCB job1 = new PCB("1", cpu1, io1);
            PCB job2 = new PCB("2", cpu2, io2);
            PCB job3 = new PCB("3", cpu3, io3);
            PCB job4 = new PCB("4", cpu4, io4);
            PCB job5 = new PCB("5", cpu5, io5);

            jobQue.add(job1);
            jobQue.add(job2);
            jobQue.add(job3);
            jobQue.add(job4);
            jobQue.add(job5);
        } else {
            PCB job5 = new PCB("5", cpu1, io1);
            PCB job4 = new PCB("4", cpu2, io2);
            PCB job3 = new PCB("3", cpu3, io3);
            PCB job2 = new PCB("2", cpu4, io4);
            PCB job1 = new PCB("1", cpu5, io5);

            jobQue.add(job1);
            jobQue.add(job2);
            jobQue.add(job3);
            jobQue.add(job4);
            jobQue.add(job5);
        }

        return jobQue;
    }//end jobPool method

    public static void cpuLoad() {
        if (cpu.isEmpty()) {
            //load first job into cpu from ready queue
            if (!readyQ.isEmpty()) {
                cpu.add(readyQ.remove(0));
            } else {
                cpu.add(ioQ.remove(0));
            }
        }
    }//end cpuLoad method

    public static void readyLoad(ArrayList<PCB> loadQ) {
        //take the first two PCBs in the job queue and add them to the ready queue
        while (readyQ.size() < 2) {
            //load another job from job queue to ready queue
            readyQ.add(loadQ.remove(0));
        }
    }//end readyLoad method

    public static ArrayList<PCB> cpuRun(ArrayList<PCB> cpu) {

        Integer temp = cpu.get(0).getCpuBurst();
        temp--;
        cpu.get(0).setCpuBurst(temp);

        return cpu;
    }//end cpuRun method

    public static ArrayList<PCB> diskRun(ArrayList<PCB> disk, int i) {

        Integer temp = disk.get(i).getIoBurst();
        temp--;
        disk.get(i).setIoBurst(temp);

        return disk;
    }//end diskRun method

    public static int timerTick() {
        int check = timer;

        //everything that happens when the timer ticks
        try {
            if (cpu.get(0).getCpuBurst() == 0) {
                cpu.get(0).cpuBurstList.remove(0);

            }
            if (cpu.get(0).cpuBurstList.get(0) > 0) {
                cpu = cpuRun(cpu);
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Timer: " + timer + "\nIndex out of bounds in TimerTick cpu.");
        }
        try {
            for (int i = 0; i < disk.size(); i++) {
                if (!disk.get(i).ioBurstList.isEmpty()) {
                    disk = diskRun(disk, i);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Timer: " + timer + "\nIndex out of bounds in TimerTick disk.");
        }
        check++;

        return check;
    }//end timerTick method

    public static String initialQueue(ArrayList<PCB> jobQ) {
        String initialQ = "";
        for (int i = 0; i < jobQ.size(); i++) {
            PCB item = jobQ.get(i);
            initialQ += "\nPCB: " + item.processID
                    + "\n   CPU Bursts:     " + item.cpuBurstList
                    + "\n   IO Bursts:      " + item.ioBurstList
                    + "\n";
        }
        return initialQ;
    }//end initialQueue method

    public static String printStatus() {

        String printOut;

        printOut = "Timer = " + timer
                + "\n   CPU:            " + cpu.toString()
                + "\n   Job Queue:      " + jobQ.toString()
                + "\n   Ready Queue:    " + readyQ.toString()
                + "\n   Disk Queue:     " + disk.toString()
                + "\n   IO Wait Queue:  " + ioQ.toString();

        return printOut;
    }//end printStatus method

}//end MainScheduler
