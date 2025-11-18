package fleetmanagement.vehicles;

import fleetmanagement.exceptions.InsufficientFuelException;
import fleetmanagement.exceptions.InvalidOperationException;
import fleetmanagement.exceptions.OverloadException;
import fleetmanagement.interfaces.FuelConsumable;
import fleetmanagement.interfaces.Maintainable;
import fleetmanagement.interfaces.PassengerCarrier;

public class Car extends LandVehicle implements FuelConsumable, PassengerCarrier, Maintainable {

    private double fuelLevel;
    private final int passengerCapacity = 5;
    private int currentPassengers;
    private boolean maintenanceNeeded;

    public Car(String id, String model, double maxSpeed, int numWheels) throws InvalidOperationException {
        super(id, model, maxSpeed, numWheels);
        this.fuelLevel = 0.0;
        this.currentPassengers = 0;
        this.maintenanceNeeded = false;
    }

    // --- NEW METHOD FOR ASSIGNMENT 3 ---
    @Override
    public boolean simulateTravel(double distance) {
        try {
            // consumeFuel will throw an exception if not enough fuel
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
            System.out.println("Driving... " + distance + " km, Fuel consumed: " +
                    String.format("%.2f", fuelConsumed) + " liters");
        } catch (InsufficientFuelException e) {
            throw new InvalidOperationException("Cannot move: " + e.getMessage());
        }
    }

    @Override
    public double calculateFuelEfficiency() {
        return 15.0;
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
    public void scheduleMaintenance() { maintenanceNeeded = true; }

    @Override
    public boolean needsMaintenance() {
        return maintenanceNeeded || getCurrentMileage() > 10000;
    }

    @Override
    public void performMaintenance() {
        maintenanceNeeded = false;
        resetMileage();
        System.out.println("Car maintenance done: Oil change, tire rotation, inspection.");
    }

    @Override
    public String toCSVString() {
        return String.format("Car,%s,%s,%.1f,%d,%.1f,%d,%.1f,%b",
                getId(), getModel(), getMaxSpeed(), getNumWheels(),
                fuelLevel, currentPassengers, getCurrentMileage(), maintenanceNeeded);
    }

    public static Car fromCSV(String[] data) throws InvalidOperationException {
        try {
            Car car = new Car(data[1], data[2],
                    Double.parseDouble(data[3]), Integer.parseInt(data[4]));
            car.fuelLevel = Double.parseDouble(data[5]);
            car.currentPassengers = Integer.parseInt(data[6]);
            car.addMileage(Double.parseDouble(data[7]));
            car.maintenanceNeeded = Boolean.parseBoolean(data[8]);
            return car;
        } catch (Exception e) {
            throw new InvalidOperationException("Invalid CSV data for Car: " + String.join(",", data));
        }
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Type: Car");
        System.out.println("Fuel Level: " + fuelLevel + " L");
        System.out.println("Passengers: " + currentPassengers + "/" + passengerCapacity);
        System.out.println("Maintenance Needed: " + (needsMaintenance() ? "Yes" : "No"));
        System.out.println("Efficiency: " + calculateFuelEfficiency() + " km/l");
    }

    @Override
    public String getDetails() {
        return String.format("Car: %s (ID: %s) - %.1f km/h, %d wheels, %.1f km mileage, Passengers: %d/%d",
                getModel(), getId(), getMaxSpeed(), getNumWheels(),
                getCurrentMileage(), currentPassengers, passengerCapacity);
    }
}