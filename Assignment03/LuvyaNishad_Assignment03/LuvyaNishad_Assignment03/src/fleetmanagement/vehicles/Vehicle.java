package fleetmanagement.vehicles;

import fleetmanagement.exceptions.InvalidOperationException;
// Import the GUI class
import fleetmanagement.gui.HighwaySimulatorGUI;

// 1. Implement Runnable
public abstract class Vehicle implements Comparable<Vehicle>, Runnable {
    private String id;
    private String model;
    private double maxSpeed;
    private double currentMileage;

    // --- NEW THREADING FIELDS ---

    // 2. Add control flags
    // 'volatile' ensures threads see the most recent value
    private volatile boolean isRunning = true;
    private volatile boolean isPaused = false;
    private volatile String status = "Idle"; // For GUI display

    // 3. Add a reference to the main GUI/Simulator
    // This is needed so the vehicle can call the shared counter
    private HighwaySimulatorGUI simulator;

    // Your existing constructor
    public Vehicle(String id, String model, double maxSpeed) throws InvalidOperationException {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidOperationException("Vehicle ID cannot be empty");
        }
        this.id = id;
        this.model = model;
        this.maxSpeed = maxSpeed;
        this.currentMileage = 0.0;
    }

    // 4. Add a "setter" for the simulator reference
    public void setSimulator(HighwaySimulatorGUI simulator) {
        this.simulator = simulator;
    }

    // --- 5. IMPLEMENT THE RUN() METHOD ---
    @Override
    public void run() {
        this.status = "Running";
        while (isRunning) {
            try {
                // This loop handles the "pause" state
                while (isPaused) {
                    this.status = "Paused";
                    Thread.sleep(100); // Wait 100ms and check again
                }
                this.status = "Running";

                // --- This is the main simulation logic ---

                // A) Simulate 1km of travel
                boolean stillHasFuel = simulateTravel(1.0);

                if (stillHasFuel) {
                    // B) !!! THIS IS THE RACE CONDITION !!!
                    // Call the unsynchronized method on the shared simulator object
                    if (simulator != null) {
                        simulator.incrementHighwayCounter();
                    }
                } else {
                    // C) Out of fuel, so pause this thread
                    this.isPaused = true;
                    this.status = "Out of Fuel";
                }

                // Wait for approx 1 second
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                // Thread was interrupted, stop running
                this.isRunning = false;
                this.status = "Stopped";
            }
        }
        this.status = "Stopped";
    }

    // --- 6. NEW/MODIFIED METHODS FOR THREADING ---

    /**
     * Simulates travel for a given distance.
     * Consumes fuel and adds mileage.
     * @return true if travel was successful (had fuel), false if out of fuel.
     */
    public abstract boolean simulateTravel(double distance);

    // Call this from your GUI buttons
    public void stopSimulation() {
        this.isRunning = false;
    }

    public void pauseSimulation() {
        this.isPaused = true;
    }

    public void resumeSimulation() {
        // Only resume if not out of fuel
        if (!this.status.equals("Out of Fuel")) {
            this.isPaused = false;
        }
    }

    // This is a new helper for the GUI
    public String getStatus() {
        return status;
    }

    // --- All your other existing methods from A2 ---

    public String getId() { return id; }
    public String getModel() { return model; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getCurrentMileage() { return currentMileage; }

    protected void addMileage(double distance) {
        if (distance > 0) {
            currentMileage += distance;
        }
    }

    public void resetMileage() {
        this.currentMileage = 0.0;
    }

    public abstract void move(double distance) throws InvalidOperationException;
    public abstract double calculateFuelEfficiency();
    public abstract double estimateJourneyTime(double distance);

    public void displayInfo() {
        System.out.println("=== VEHICLE INFORMATION ===");
        System.out.println("ID: " + id);
        System.out.println("Model: " + model);
        System.out.println("Max Speed: " + maxSpeed + " km/h");
        System.out.println("Current Mileage: " + currentMileage + " km");
    }

    @Override
    public int compareTo(Vehicle other) {
        return Double.compare(other.calculateFuelEfficiency(), this.calculateFuelEfficiency());
    }

    public abstract String toCSVString();

    public String getDetails() {
        return String.format("%s: %s (ID: %s) - %.1f km/h, %.1f km mileage",
                getClass().getSimpleName(), model, id, maxSpeed, currentMileage);
    }
}