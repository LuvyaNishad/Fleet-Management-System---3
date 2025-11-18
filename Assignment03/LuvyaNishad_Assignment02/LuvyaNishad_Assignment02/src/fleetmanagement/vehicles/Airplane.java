package fleetmanagement.vehicles;

import fleetmanagement.exceptions.InsufficientFuelException;
import fleetmanagement.exceptions.InvalidOperationException;
import fleetmanagement.exceptions.OverloadException;
import fleetmanagement.interfaces.CargoCarrier;
import fleetmanagement.interfaces.FuelConsumable;
import fleetmanagement.interfaces.Maintainable;
import fleetmanagement.interfaces.PassengerCarrier;

public class Airplane extends AirVehicle implements FuelConsumable, PassengerCarrier, CargoCarrier, Maintainable {

    private double fuelLevel;
    private final double cargoCapacity = 10000.0;
    private final int passengerCapacity = 200;
    private int currentPassengers;
    private double currentCargo;
    private boolean maintenanceNeeded;

    public Airplane(String id, String model, double maxSpeed, double maxAltitude) throws InvalidOperationException {
        super(id, model, maxSpeed, maxAltitude);
        this.fuelLevel = 0.0;
        this.currentPassengers = 0;
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
        try {
            double fuelConsumed = consumeFuel(distance);
            addMileage(distance);
            System.out.println("Airplane flying at " + getMaxAltitude() + " m... " +
                    distance + " km, Fuel consumed: " + String.format("%.2f", fuelConsumed) + " liters");
        } catch (InsufficientFuelException e) {
            throw new InvalidOperationException("Cannot move: " + e.getMessage());
        }
    }

    @Override
    public double calculateFuelEfficiency() {
        return 5.0;
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
            throw new InsufficientFuelException("Need " + fuelNeeded + " L but only have " + fuelLevel + " L");
        }
        fuelLevel -= fuelNeeded;
        return fuelNeeded;
    }

    // ... (All other methods: boardPassengers, loadCargo, toCSVString, etc. remain unchanged) ...
    @Override
    public void boardPassengers(int count) throws OverloadException {
        if (count <= 0) throw new OverloadException("Passenger count must be positive");
        if (currentPassengers + count > passengerCapacity) {
            throw new OverloadException("Cannot board " + count + " passengers. Capacity: " +
                    passengerCapacity + ", Current: " + currentPassengers);
        }
        currentPassengers += count;
    }

    @Override
    public void disembarkPassengers(int count) throws InvalidOperationException {
        if (count <= 0) throw new InvalidOperationException("Passenger count must be positive");
        if (count > currentPassengers) {
            throw new InvalidOperationException("Cannot disembark " + count +
                    " passengers. Only " + currentPassengers + " onboard");
        }
        currentPassengers -= count;
    }

    @Override
    public int getPassengerCapacity() { return passengerCapacity; }
    @Override
    public int getCurrentPassengers() { return currentPassengers; }

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
        System.out.println("Airplane maintenance done: Engine overhaul, avionics, safety checks.");
    }

    @Override
    public String toCSVString() {
        return String.format("Airplane,%s,%s,%.1f,%.1f,%.1f,%d,%.1f,%.1f,%b",
                getId(), getModel(), getMaxSpeed(), getMaxAltitude(),
                fuelLevel, currentPassengers, currentCargo,
                getCurrentMileage(), maintenanceNeeded);
    }

    public static Airplane fromCSV(String[] data) throws InvalidOperationException {
        try {
            Airplane plane = new Airplane(data[1], data[2],
                    Double.parseDouble(data[3]), Double.parseDouble(data[4]));
            plane.fuelLevel = Double.parseDouble(data[5]);
            plane.currentPassengers = Integer.parseInt(data[6]);
            plane.currentCargo = Double.parseDouble(data[7]);
            plane.addMileage(Double.parseDouble(data[8]));
            plane.maintenanceNeeded = Boolean.parseBoolean(data[9]);
            return plane;
        } catch (Exception e) {
            throw new InvalidOperationException("Invalid CSV data for Airplane: " + String.join(",", data));
        }
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Type: Airplane");
        System.out.println("Passengers: " + currentPassengers + "/" + passengerCapacity);
        System.out.println("Cargo: " + currentCargo + "/" + cargoCapacity + " kg");
        System.out.println("Fuel Level: " + fuelLevel + " L");
        System.out.println("Maintenance Needed: " + (needsMaintenance() ? "Yes" : "No"));
        System.out.println("Efficiency: " + calculateFuelEfficiency() + " km/l");
    }

    @Override
    public String getDetails() {
        return String.format("Airplane: %s (ID: %s) - %.1f km/h, %.1f m altitude, %.1f km mileage, Passengers: %d/%d, Cargo: %.1f/%.1f kg",
                getModel(), getId(), getMaxSpeed(), getMaxAltitude(),
                getCurrentMileage(), currentPassengers, passengerCapacity,
                currentCargo, cargoCapacity);
    }
}