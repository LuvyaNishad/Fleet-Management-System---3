package fleetmanagement.interfaces;

import fleetmanagement.exceptions.InvalidOperationException;
import fleetmanagement.exceptions.OverloadException;

public interface CargoCarrier {
    void loadCargo(double weight) throws OverloadException;
    void unloadCargo(double weight) throws InvalidOperationException;
    double getCargoCapacity();
    double getCurrentCargo();
}