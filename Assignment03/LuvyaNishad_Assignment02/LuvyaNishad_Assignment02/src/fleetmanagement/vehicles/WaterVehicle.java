package fleetmanagement.vehicles;

import fleetmanagement.exceptions.InvalidOperationException;

public abstract class WaterVehicle extends Vehicle {
    private boolean hasSail;

    public WaterVehicle(String id, String model, double maxSpeed, boolean hasSail)
            throws InvalidOperationException {
        super(id, model, maxSpeed);
        this.hasSail = hasSail;
    }

    public boolean hasSail() {
        return hasSail;
    }

    @Override
    public double estimateJourneyTime(double distance) {
        double baseline = distance / getMaxSpeed();
        return baseline * 1.15;
    }
}