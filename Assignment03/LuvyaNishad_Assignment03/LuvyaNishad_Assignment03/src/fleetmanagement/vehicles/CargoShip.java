package fleetmanagement.vehicles;

import fleetmanagement.exceptions.InsufficientFuelException;
import fleetmanagement.exceptions.InvalidOperationException;
import fleetmanagement.exceptions.OverloadException;
import fleetmanagement.interfaces.CargoCarrier;
import fleetmanagement.interfaces.FuelConsumable;
import fleetmanagement.interfaces.Maintainable;

public class CargoShip extends WaterVehicle implements CargoCarrier, Maintainable, FuelConsumable {

    private final double cargoCapacity = 50000.0;
    private double currentCargo;
    private boolean maintenanceNeeded;
    private double fuelLevel;

    public CargoShip(String id, String model, double maxSpeed, boolean hasSail) throws InvalidOperationException {
        super(id, model, maxSpeed, hasSail);
        this.currentCargo = 0.0;
        this.maintenanceNeeded = false;
        this.fuelLevel = 0.0;
    }

    // --- NEW METHOD FOR ASSIGNMENT 3 ---
    @Override
    public boolean simulateTravel(double distance) {
        if (!hasSail()) { // Only consume fuel if it doesn't have a sail
            try {
                consumeFuel(distance);
                addMileage(distance);
                return true;
            } catch (InsufficientFuelException e) {
                return false; // Out of fuel
            }
        } else {
            // Sail-powered, no fuel needed
            addMileage(distance);
            return true;
        }
    }
    // --- END NEW METHOD ---

    @Override
    public void move(double distance) throws InvalidOperationException {
        if (distance < 0) throw new InvalidOperationException("Distance cannot be negative");
        if (!hasSail()) {
            double fuelNeeded = distance / calculateFuelEfficiency();
            if (fuelNeeded > fuelLevel) {
                throw new InvalidOperationException("Not enough fuel for " + distance + " km journey");
            }
            fuelLevel -= fuelNeeded;
        }
        addMileage(distance);
        System.out.println("Cargo ship sailing... " + distance + " km");
    }

    @Override
    public double calculateFuelEfficiency() {
        return hasSail() ? 0.0 : 4.0;
    }

    @Override
    public void refuel(double amount) throws InvalidOperationException {
        if (hasSail()) {
            // Do nothing, but don't throw an error in simulation
            return;
        }
        if (amount <= 0) throw new InvalidOperationException("Refuel amount must be positive");
        fuelLevel += amount;
        // If refueled, it can resume
        if (getStatus().equals("Out of Fuel")) {
            resumeSimulation();
        }
    }

    @Override
    public double getFuelLevel() {
        return hasSail() ? 0.0 : fuelLevel;
    }

    @Override
    public double consumeFuel(double distance) throws InsufficientFuelException {
        if (hasSail()) return 0.0;
        double fuelNeeded = distance / calculateFuelEfficiency();
        if (fuelNeeded > fuelLevel) {
            throw new InsufficientFuelException("Not enough fuel for " + distance + " km journey");
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
        System.out.println("Cargo ship maintenance done: Hull, engine, cargo inspection.");
    }

    @Override
    public String toCSVString() {
        return String.format("CargoShip,%s,%s,%.1f,%b,%.1f,%.1f,%.1f,%b",
                getId(), getModel(), getMaxSpeed(), hasSail(),
                fuelLevel, currentCargo, getCurrentMileage(), maintenanceNeeded);
    }

    public static CargoShip fromCSV(String[] data) throws InvalidOperationException {
        try {
            CargoShip ship = new CargoShip(data[1], data[2],
                    Double.parseDouble(data[3]), Boolean.parseBoolean(data[4]));
            ship.fuelLevel = Double.parseDouble(data[5]);
            ship.currentCargo = Double.parseDouble(data[6]);
            ship.addMileage(Double.parseDouble(data[7]));
            ship.maintenanceNeeded = Boolean.parseBoolean(data[8]);
            return ship;
        } catch (Exception e) {
            throw new InvalidOperationException("Invalid CSV data for CargoShip: " + String.join(",", data));
        }
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Type: CargoShip");
        System.out.println("Cargo: " + currentCargo + "/" + cargoCapacity + " kg");
        if (!hasSail()) {
            System.out.println("Fuel Level: " + fuelLevel + " L");
        } else {
            System.out.println("Sail-powered: Yes");
        }
        System.out.println("Maintenance Needed: " + (needsMaintenance() ? "Yes" : "No"));
        System.out.println("Efficiency: " + (hasSail() ? "Sail-powered (no fuel use)" : calculateFuelEfficiency() + " km/l"));
    }

    @Override
    public String getDetails() {
        return String.format("CargoShip: %s (ID: %s) - %.1f km/h, %.1f km mileage, Cargo: %.1f/%.1f kg, Sail: %s",
                getModel(), getId(), getMaxSpeed(), getCurrentMileage(),
                currentCargo, cargoCapacity, hasSail() ? "Yes" : "No");
    }
}