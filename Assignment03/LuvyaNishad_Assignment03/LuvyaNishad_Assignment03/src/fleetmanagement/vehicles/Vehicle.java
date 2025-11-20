package fleetmanagement.vehicles;

import fleetmanagement.exceptions.InvalidOperationException;
import fleetmanagement.interfaces.HighwayTracker; // Use the new interface

public abstract class Vehicle implements Comparable<Vehicle>, Runnable {
    private String id;
    private String model;
    private double maxSpeed;
    private double currentMileage;

    private volatile boolean isRunning = true;
    protected volatile boolean isPaused = false; // Protected so subclasses can check if needed, though accessor is better
    private volatile String status = "Idle";

    // Decoupled: Uses Interface instead of GUI class directly
    private HighwayTracker simulator;

    public Vehicle(String id, String model, double maxSpeed) throws InvalidOperationException {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidOperationException("Vehicle ID cannot be empty");
        }
        this.id = id;
        this.model = model;
        this.maxSpeed = maxSpeed;
        this.currentMileage = 0.0;
    }

    // Updated setter to accept the Interface
    public void setSimulator(HighwayTracker simulator) {
        this.simulator = simulator;
    }

    @Override
    public void run() {
        this.status = "Running";
        while (isRunning) {
            try {
                // --- OPTIMIZATION FIX (Feedback Point 1) ---
                // Replaced polling (sleep loop) with efficient wait/notify
                synchronized (this) {
                    while (isPaused) {
                        if (!this.status.equals("Out of Fuel")) {
                            this.status = "Paused";
                        }
                        wait(); // Releases lock and waits efficiently until notified
                    }
                }
                // -------------------------------------------

                if (!this.status.equals("Out of Fuel")) {
                    this.status = "Running";
                }

                // Simulate Travel
                boolean stillHasFuel = simulateTravel(1.0);

                if (stillHasFuel) {
                    // Update Shared Counter via Interface
                    if (simulator != null) {
                        simulator.incrementHighwayCounter();
                    }
                } else {
                    // Out of Fuel
                    this.status = "Out of Fuel";
                    this.isPaused = true; // Will catch on next loop iteration
                }

                Thread.sleep(1000); // Simulate 1 second of travel

            } catch (InterruptedException e) {
                this.isRunning = false;
                this.status = "Stopped";
                Thread.currentThread().interrupt(); // Restore interrupt status
            }
        }
        this.status = "Stopped";
    }

    public void stopSimulation() {
        this.isRunning = false;
    }

    // Synchronized to ensure thread safety with wait/notify logic
    public synchronized void pauseSimulation() {
        this.isPaused = true;
    }

    // Updated to use notifyAll() to wake up the waiting thread
    public synchronized void resumeSimulation() {
        this.isPaused = false;
        this.status = "Running";
        notifyAll(); // Wakes up the thread paused in the run() method
    }

    public String getStatus() { return status; }
    public abstract boolean simulateTravel(double distance);
    public abstract void move(double distance) throws InvalidOperationException;
    public abstract double calculateFuelEfficiency();
    public abstract double estimateJourneyTime(double distance);
    public abstract String toCSVString();
    public String getId() { return id; }
    public String getModel() { return model; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getCurrentMileage() { return currentMileage; }
    protected void addMileage(double distance) { if (distance > 0) currentMileage += distance; }
    public void resetMileage() { this.currentMileage = 0.0; }
    public void displayInfo() { System.out.println("ID: " + id + ", Model: " + model); }
    @Override public int compareTo(Vehicle other) { return Double.compare(other.calculateFuelEfficiency(), this.calculateFuelEfficiency()); }
    public String getDetails() { return String.format("%s: %s (ID: %s)", getClass().getSimpleName(), model, id); }
}