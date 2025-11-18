package fleetmanagement.interfaces;

import fleetmanagement.exceptions.InsufficientFuelException;
import fleetmanagement.exceptions.InvalidOperationException;

public interface FuelConsumable {
    double consumeFuel(double distance) throws InsufficientFuelException;
    void refuel(double amount) throws InvalidOperationException;
    double getFuelLevel();
}