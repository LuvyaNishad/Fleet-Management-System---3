package fleetmanagement.interfaces;

import fleetmanagement.exceptions.InvalidOperationException;
import fleetmanagement.exceptions.OverloadException;

public interface PassengerCarrier {
    void boardPassengers(int count) throws OverloadException;
    void disembarkPassengers(int count) throws InvalidOperationException;
    int getPassengerCapacity();
    int getCurrentPassengers();
}