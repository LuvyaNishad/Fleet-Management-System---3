Assignment 3: Fleet Highway Simulator (Multithreading & GUI)

Student Name: Luvya Nishad
Course: AP-M2025

1. Overview

This project implements a multithreaded Fleet Highway Simulator using Java Swing. It simulates multiple vehicles (Car, Truck, Airplane) traveling concurrently on a highway. Each vehicle runs in its own thread, updating its mileage, fuel, and a shared "Highway Distance" counter. The application demonstrates a classic race condition arising from unsynchronized access to a shared resource and subsequently resolves it using synchronization.

2. System Architecture

The system follows a concurrent architecture as described in the assignment guidelines:

1. GUI (Main Thread): The Swing-based interface handles user inputs (Start, Stop, Pause) and visualization. It runs on the Event Dispatch Thread (EDT).
2. Vehicle Threads: When the simulation starts, the system spawns a separate execution thread for each vehicle in the fleet (e.g., Vehicle Thread 1, Vehicle Thread 2, Vehicle Thread 3). These threads run concurrently.
3. Shared Resource: All vehicle threads compete to update a single shared variable: the Highway Distance Counter.
4. Interaction: The GUI periodically reads the state of the vehicles and the shared counter to update the display, while the vehicle threads continuously update the shared counter in the background.

3. How to Compile and Run

Prerequisites: Ensure you have the Java Development Kit (JDK) installed on your system.

Compiling:
Open a terminal or command prompt, navigate to the src directory of the project, and run the following command:

    javac fleetmanagement/gui/HighwaySimulatorGUI.java

Running:
After successful compilation, run the application using the following command:

    java fleetmanagement.gui.HighwaySimulatorGUI

4. Design & GUI Layout

The application features a user-friendly Graphical User Interface (GUI) built with Java Swing, utilizing a BorderLayout to organize components into three main sections:

Top (Control Panel):
- Start: Initializes and starts a new thread for each vehicle in the fleet.
- Pause: Pauses the execution of all vehicle threads.
- Resume: Resumes the execution of all paused vehicle threads.
- Stop: Stops all vehicle threads, effectively ending the simulation.

Center (Vehicle Status Panel):
- This area displays a vertical list of panels, one for each vehicle (Car, Truck, Airplane).
- Each panel shows the vehicle's ID, model, current speed, distance traveled, fuel level, and current operational status (Running, Paused, Stopped, or Out of Fuel).
- Refuel Buttons: Each vehicle row includes a "Refuel" button. These buttons become enabled only when the specific vehicle is not in a "Running" state (i.e., when it is Paused, Stopped, or Out of Fuel), allowing users to replenish fuel.

Bottom (Statistics Panel):
- Shared Highway Distance: Displays the total distance calculated by the shared counter, updated by all vehicle threads. This is the variable subject to the race condition.
- Real Total: Displays the accurate sum of the individual mileages of all vehicles. Comparing this value with the "Shared Highway Distance" reveals the presence or absence of the race condition.
- Status Message: A label at the very bottom provides general feedback about the simulation state (e.g., "Simulation RUNNING...", "Simulation PAUSED.").

5. Thread Control Logic

Start: When the "Start" button is clicked, the application iterates through the fleet list. For each vehicle (which implements Runnable), a new Thread object is created and started. This ensures every vehicle operates independently and concurrently.

Pause/Resume: The Vehicle class maintains a volatile boolean isPaused flag. The run() loop of each vehicle checks this flag in every iteration. If isPaused is true, the thread enters a loop where it sleeps for short intervals (100ms) until the flag is set to false by the "Resume" button or a "Refuel" action.

Stop: A volatile boolean isRunning flag in the Vehicle class controls the main execution loop. Setting this flag to false causes the run() method to complete, thereby terminating the thread.

Out of Fuel: If a vehicle runs out of fuel, its status is set to "Out of Fuel", and it automatically pauses itself by setting isPaused to true.

6. Race Condition & Synchronization Fix

The Race Condition (Uncorrected Version)

In the initial uncorrected version of the simulator, the shared resource highwayDistance was updated by multiple threads using a non-atomic read-modify-write operation without any synchronization mechanisms.

The problematic code block was:

    public void incrementHighwayCounter() {
        int currentDistance = highwayDistance; // Read
        try {
            Thread.sleep(5); // Force context switch to exacerbate the issue
        } catch (InterruptedException e) {}
        highwayDistance = currentDistance + 1; // Write
    }

Explanation:
1. Read: A thread reads the current value of highwayDistance.
2. Sleep: The thread sleeps for 5ms. During this pause, the operating system's scheduler may switch execution to another vehicle thread.
3. Interference: The second thread reads the same value for highwayDistance because the first thread hasn't updated it yet.
4. Write: Both threads increment their local copy of the value and write it back.
5. Result: One of the increments is effectively lost (overwritten). Over time, this leads to a significant discrepancy where the "Shared Highway Distance" is much lower than the "Real Total" (the actual sum of miles traveled by all vehicles).

Evidence:
As shown in the "Uncorrected" screenshots (included in submission), the "Shared Highway Distance" lags significantly behind the "Real Total". For example, in one run, the Shared Distance was 103 while the Real Total was 249.

The Fix (Corrected Version)

To resolve the race condition, I applied the synchronized keyword to the method responsible for updating the shared counter.

The corrected code block is:

    public synchronized void incrementHighwayCounter() { // Added 'synchronized'
        int currentDistance = highwayDistance;
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {}
        highwayDistance = currentDistance + 1;
    }

Explanation:
The synchronized keyword ensures mutual exclusion. It forces every thread to acquire an intrinsic lock (monitor) on the HighwaySimulatorGUI instance before executing the method. If one thread is inside this method (even while sleeping), all other threads attempting to enter it are blocked and must wait until the first thread finishes and releases the lock. This guarantees that the read, increment, and write operations happen atomically as a single, indivisible unit.

Evidence:
As shown in the "Corrected" screenshots (included in submission), the "Shared Highway Distance" remains exactly equal to the "Real Total" throughout the simulation, proving that no updates are being lost.

7. Note on GUI Thread Safety

The simulation updates the GUI components (labels, buttons) periodically. To ensure thread safety in Swing, these updates are not performed directly from the vehicle threads. Instead, a javax.swing.Timer is used. This timer fires an event every 100ms, and its action listener creates a task that is executed on the Event Dispatch Thread (EDT). This pattern adheres to Swing's single-threaded rule, ensuring that all UI updates are safe and do not cause concurrency issues or graphical glitches.