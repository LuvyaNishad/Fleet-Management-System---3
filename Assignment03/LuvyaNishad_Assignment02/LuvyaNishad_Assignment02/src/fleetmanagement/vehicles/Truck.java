package fleetmanagement.vehicles;

import fleetmanagement.exceptions.InsufficientFuelException;
import fleetmanagement.exceptions.InvalidOperationException;
import fleetmanagement.exceptions.OverloadException;
import fleetmanagement.interfaces.CargoCarrier;
import fleetmanagement.interfaces.FuelConsumable;
import fleetmanagement.interfaces.Maintainable;

public class Truck extends LandVehicle implements FuelConsumable, CargoCarrier, Maintainable {

    private double fuelLevel;
    private final double cargoCapacity = 5000.0;
    private double currentCargo;
    private boolean maintenanceNeeded;

    public Truck(String id, String model, double maxSpeed, int numWheels) throws InvalidOperationException {
        super(id, model, maxSpeed, numWheels);
        this.fuelLevel = 0.0;
        this.currentCargo = 0.0;
        this.maintenanceNeeded = false;
    }

    // --- NEW METHOD FOR ASSIGNMENT 3 ---
    @Override
    public boolean simulateTravel(double distance) {
        try {
            consumeFuel(distance);
            addMileage(distance);
            return true; // Still has fuel
        } catch (InsufficientFuelException e) {
            return false; // Out of fuel
        }
    }
    // --- END NEW METHOD ---

    @Override
    public void move(double distance) throws InvalidOperationException {
        if (distance < 0) throw new InvalidOperationException("Distance cannot be negative");
        double fuelConsumed = 0.0;
        try {
            fuelConsumed = consumeFuel(distance);
        } catch (InsufficientFuelException e) {
            throw new InvalidOperationException("Move failed: " + e.getMessage());
        }
        addMileage(distance);
        System.out.println("Truck hauling... " + distance + " km, Fuel consumed: " +
                String.format("%.2f", fuelConsumed) + " liters");
    }

    @Override
    public double calculateFuelEfficiency() {
        double base = 8.0;
        if (currentCargo > cargoCapacity * 0.5) {
            return base * 0.9;
        }
        return base;
    }

    @Override
    public void refuel(double amount) throws InvalidOperationException {
        if (amount <= 0) throw new InvalidOperationException("Refuel amount must be positive");
        fuelLevel += amount;
        // If refueled, it can resume
        if (getStatus().equals("Out of Fuel")) {
            resumeSimulation();
        }
    }

    @Override
    public double getFuelLevel() { return fuelLevel; }

    @Override
    public double consumeFuel(double distance) throws InsufficientFuelException {
        double fuelNeeded = distance / calculateFuelEfficiency();
        if (fuelNeeded > fuelLevel) {
            throw new InsufficientFuelException("Not enough fuel. Needed: " +
                    String.format("%.2f", fuelNeeded) + " L, Available: " + fuelLevel + " L");
        }
        fuelLevel -= fuelNeeded;
        return fuelNeeded;
    }

    // ... (All other methods: loadCargo, unloadCargo, toCSVString, etc. remain unchanged) ...

    @Override
    public void loadCargo(double weight) throws OverloadException {
        if (weight <= 0) throw new OverloadException("Cargo weight must be positive");
        if (currentCargo + weight > cargoCapacity) {
            throw new OverloadException("Cannot load " + weight + " kg. Capacity: " +
                    cargoCapacity + " kg, Current: " + currentCargo + " kg");
        }
        currentCargo += weight;
    }

    @Override
    public void unloadCargo(double weight) throws InvalidOperationException {
        if (weight <= 0) throw new InvalidOperationException("Cargo weight must be positive");
        if (weight > currentCargo) {
            throw new InvalidOperationException("Cannot unload " + weight +
                    " kg. Only " + currentCargo + " kg loaded");
        }
        currentCargo -= weight;
    }

    @Override
    public double getCargoCapacity() { return cargoCapacity; }
    @Override
    public double getCurrentCargo() { return currentCargo; }

    @Override
    public void scheduleMaintenance() { maintenanceNeeded = true; }

    @Override
    public boolean needsMaintenance() {
        return maintenanceNeeded || getCurrentMileage() > 10000;
    }

    @Override
    public void performMaintenance() {
        maintenanceNeeded = false;
        resetMileage();
        System.out.println("Truck maintenance done: Engine service, brakes, cargo inspection.");
    }

    @Override
    public String toCSVString() {
        return String.format("Truck,%s,%s,%.1f,%d,%.1f,%.1f,%.1f,%b",
                getId(), getModel(), getMaxSpeed(), getNumWheels(),
                fuelLevel, currentCargo, getCurrentMileage(), maintenanceNeeded);
    }

    public static Truck fromCSV(String[] data) throws InvalidOperationException {
        try {
            Truck truck = new Truck(data[1], data[2],
                    Double.parseDouble(data[3]), Integer.parseInt(data[4]));
            truck.fuelLevel = Double.parseDouble(data[5]);
            truck.currentCargo = Double.parseDouble(data[6]);
            truck.addMileage(Double.parseDouble(data[7]));
            truck.maintenanceNeeded = Boolean.parseBoolean(data[8]);
            return truck;
        } catch (Exception e) {
            throw new InvalidOperationException("Invalid CSV data for Truck: " + String.join(",", data));
        }
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Type: Truck");
        System.out.println("Cargo: " + currentCargo + "/" + cargoCapacity + " kg");
        System.out.println("Fuel Level: " + fuelLevel + " L");
        System.out.println("Maintenance Needed: " + (needsMaintenance() ? "Yes" : "No"));
        System.out.println("Efficiency: " + calculateFuelEfficiency() + " km/l");
    }

    @Override
    public String getDetails() {
        return String.format("Truck: %s (ID: %s) - %.1f km/h, %d wheels, %.1f km mileage, Cargo: %.1f/%.1f kg",
                getModel(), getId(), getMaxSpeed(), getNumWheels(),
                getCurrentMileage(), currentCargo, cargoCapacity);
    }
}