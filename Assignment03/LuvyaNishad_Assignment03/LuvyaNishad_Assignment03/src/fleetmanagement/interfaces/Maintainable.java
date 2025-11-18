package fleetmanagement.interfaces;

public interface Maintainable {
    boolean needsMaintenance();
    void performMaintenance();
    void scheduleMaintenance();
}