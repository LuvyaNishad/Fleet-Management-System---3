package fleetmanagement.vehicles;

import fleetmanagement.exceptions.InvalidOperationException;
import fleetmanagement.gui.HighwaySimulatorGUI;

public abstract class Vehicle implements Comparable<Vehicle>, Runnable {
    private String id;
    private String model;
    private double maxSpeed;
    private double currentMileage;

    private volatile boolean isRunning = true;
    protected volatile boolean isPaused = false;
    private volatile String status = "Idle";

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
                // Handle Pause
                while (isPaused) {
                    if (!this.status.equals("Out of Fuel")) {
                        this.status = "Paused";
                    }
                    Thread.sleep(100);
                }

                if (!this.status.equals("Out of Fuel")) {
                    this.status = "Running";
                }

                // Simulate Travel
                boolean stillHasFuel = simulateTravel(1.0);

                if (stillHasFuel) {
                    // Update Shared Counter
                    if (simulator != null) {
                        simulator.incrementHighwayCounter();
                    }
                } else {
                    // Out of Fuel
                    this.status = "Out of Fuel";
                    this.isPaused = true;
                }

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
        this.isPaused = false;
        this.status = "Running";
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