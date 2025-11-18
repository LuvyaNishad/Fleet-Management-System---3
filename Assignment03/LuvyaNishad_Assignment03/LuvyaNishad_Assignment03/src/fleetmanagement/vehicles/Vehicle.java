package fleetmanagement.vehicles;

import fleetmanagement.exceptions.InvalidOperationException;
import fleetmanagement.gui.HighwaySimulatorGUI;

public abstract class Vehicle implements Comparable<Vehicle>, Runnable {
    private String id;
    private String model;
    private double maxSpeed;
    private double currentMileage;

    // Threading flags
    private volatile boolean isRunning = true;
    protected volatile boolean isPaused = false; // changed to protected so subclasses can access if needed
    private volatile String status = "Idle";

    // Reference to the shared simulator
    private HighwaySimulatorGUI simulator;

    public Vehicle(String id, String model, double maxSpeed) throws InvalidOperationException {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidOperationException("Vehicle ID cannot be empty");
        }
        this.id = id;
        this.model = model;
        this.maxSpeed = maxSpeed;
        this.currentMileage = 0.0;
    }

    public void setSimulator(HighwaySimulatorGUI simulator) {
        this.simulator = simulator;
    }

    @Override
    public void run() {
        this.status = "Running";
        while (isRunning) {
            try {
                // --- LOGIC FIX 1: Handle Pause Correctly ---
                while (isPaused) {
                    // Only overwrite status if we are NOT Out of Fuel
                    // This ensures the GUI sees "Out of Fuel" and enables the Refuel button
                    if (!this.status.equals("Out of Fuel")) {
                        this.status = "Paused";
                    }
                    Thread.sleep(100);
                }

                // If we just woke up from a pause (and have fuel), set to Running
                if (!this.status.equals("Out of Fuel")) {
                    this.status = "Running";
                }

                // 1. Simulate Travel (consumes fuel)
                boolean stillHasFuel = simulateTravel(1.0);

                if (stillHasFuel) {
                    // 2. Update Shared Counter
                    if (simulator != null) {
                        simulator.incrementHighwayCounter();
                    }
                } else {
                    // 3. Out of Fuel Logic
                    this.status = "Out of Fuel";
                    this.isPaused = true; // Pause the thread
                }

                // Simulate 1 second of travel time
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                this.isRunning = false;
                this.status = "Stopped";
            }
        }
        this.status = "Stopped";
    }

    public void stopSimulation() {
        this.isRunning = false;
    }

    public void pauseSimulation() {
        this.isPaused = true;
    }

    public void resumeSimulation() {
        // --- LOGIC FIX 2: Always allow resume ---
        // When we refuel, we call this. We MUST allow isPaused to become false
        // regardless of the previous "Out of Fuel" status.
        this.isPaused = false;
        // Reset status to Running immediately so the loop picks it up
        this.status = "Running";
    }

    public String getStatus() {
        return status;
    }

    // --- Abstract Methods ---
    public abstract boolean simulateTravel(double distance);
    public abstract void move(double distance) throws InvalidOperationException;
    public abstract double calculateFuelEfficiency();
    public abstract double estimateJourneyTime(double distance);
    public abstract String toCSVString();

    // --- Getters & Helpers ---
    public String getId() { return id; }
    public String getModel() { return model; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getCurrentMileage() { return currentMileage; }

    protected void addMileage(double distance) {
        if (distance > 0) currentMileage += distance;
    }

    public void resetMileage() { this.currentMileage = 0.0; }

    public void displayInfo() {
        System.out.println("ID: " + id + ", Model: " + model);
    }

    @Override
    public int compareTo(Vehicle other) {
        return Double.compare(other.calculateFuelEfficiency(), this.calculateFuelEfficiency());
    }

    public String getDetails() {
        return String.format("%s: %s (ID: %s)", getClass().getSimpleName(), model, id);
    }
}