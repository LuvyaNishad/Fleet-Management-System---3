package fleetmanagement.gui;

import fleetmanagement.vehicles.Vehicle;
import fleetmanagement.vehicles.Car;
import fleetmanagement.vehicles.Bus;
import fleetmanagement.vehicles.Truck;
import fleetmanagement.vehicles.Airplane;
import fleetmanagement.exceptions.InvalidOperationException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HighwaySimulatorGUI {

    // Shared Resource
    public static int highwayDistance = 0;

    private List<Vehicle> fleet = new ArrayList<>();
    private List<Thread> vehicleThreads = new ArrayList<>();

    private JFrame frame;
    private JButton btnStart, btnPause, btnResume, btnStop;
    private JLabel lblCounter, lblStatus;
    private JLabel lblVehicle1, lblVehicle2, lblVehicle3;
    private JButton btnRefuel1, btnRefuel2, btnRefuel3;

    // --- THE FIX IS HERE ---
    // Added 'synchronized' to prevent the race condition.
    // Remove this keyword to demonstrate the bug for your report.
    public synchronized void incrementHighwayCounter() {
        int currentDistance = highwayDistance;
        try {
            // Sleep to allow other threads to interfere (if unsynchronized)
            Thread.sleep(5);
        } catch (InterruptedException e) {}
        highwayDistance = currentDistance + 1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new HighwaySimulatorGUI().createAndShowGUI();
            } catch (InvalidOperationException e) {
                e.printStackTrace();
            }
        });
    }

    public void createAndShowGUI() throws InvalidOperationException {
        frame = new JFrame("Fleet Highway Simulator (Final Version)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 450); // Slightly taller to fit everything
        frame.setLayout(new BorderLayout());

        // 1. Top Control Panel
        JPanel controlPanel = new JPanel();
        btnStart = new JButton("Start");
        btnPause = new JButton("Pause");
        btnResume = new JButton("Resume");
        btnStop = new JButton("Stop");

        btnPause.setEnabled(false);
        btnResume.setEnabled(false);
        btnStop.setEnabled(false);

        controlPanel.add(btnStart);
        controlPanel.add(btnPause);
        controlPanel.add(btnResume);
        controlPanel.add(btnStop);
        frame.add(controlPanel, BorderLayout.NORTH);

        // 2. Vehicle Status Panel
        JPanel vehicleListPanel = new JPanel();
        vehicleListPanel.setLayout(new BoxLayout(vehicleListPanel, BoxLayout.Y_AXIS));
        vehicleListPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Status"));

        // Setup Rows for Vehicles
        JPanel panelV1 = createVehicleRow("Vehicle 1", btnRefuel1 = new JButton("Refuel Car"));
        lblVehicle1 = (JLabel) panelV1.getComponent(0);

        JPanel panelV2 = createVehicleRow("Vehicle 2", btnRefuel2 = new JButton("Refuel Truck"));
        lblVehicle2 = (JLabel) panelV2.getComponent(0);

        JPanel panelV3 = createVehicleRow("Vehicle 3", btnRefuel3 = new JButton("Refuel Airplane"));
        lblVehicle3 = (JLabel) panelV3.getComponent(0);

        vehicleListPanel.add(panelV1);
        vehicleListPanel.add(panelV2);
        vehicleListPanel.add(panelV3);
        frame.add(vehicleListPanel, BorderLayout.CENTER);

        // 3. Bottom Status Panel
        JPanel statusPanel = new JPanel(new GridLayout(2, 1));
        lblCounter = new JLabel("Shared Highway Distance: 0");
        lblCounter.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblCounter.setHorizontalAlignment(SwingConstants.CENTER);

        lblStatus = new JLabel("Click Start to begin simulation.");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        statusPanel.add(lblCounter);
        statusPanel.add(lblStatus);
        frame.add(statusPanel, BorderLayout.SOUTH);

        setupFleet();
        addListeners();

        // GUI Timer (Updates every 100ms)
        new Timer(100, e -> updateGUILabels()).start();

        frame.setVisible(true);
    }

    private JPanel createVehicleRow(String labelText, JButton refuelBtn) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(labelText);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        refuelBtn.setEnabled(false);

        JPanel btnPanel = new JPanel();
        btnPanel.add(refuelBtn);

        panel.add(label, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        return panel;
    }

    private void setupFleet() throws InvalidOperationException {
        // --- CONFIG FIX: Lower fuel for faster testing ---

        // 1. Car (Efficiency ~15 km/L). 5L = 75km = ~75 seconds runtime
        Car car1 = new Car("C001", "Toyota Camry", 180.0, 4);
        car1.refuel(5.0);
        car1.setSimulator(this);

        // 2. Truck (Efficiency ~8 km/L). 10L = 80km = ~80 seconds runtime
        Truck truck1 = new Truck("T001", "Ford F-150", 120.0, 6);
        truck1.refuel(10.0);
        truck1.setSimulator(this);

        // 3. Airplane (Efficiency ~5 km/L). 20L = 100km = ~100 seconds runtime
        Airplane plane1 = new Airplane("A001", "Boeing 737", 850.0, 35000.0);
        plane1.refuel(20.0);
        plane1.setSimulator(this);

        fleet.add(car1);
        fleet.add(truck1);
        fleet.add(plane1);
    }

    private void addListeners() {
        btnStart.addActionListener(e -> {
            for (Vehicle v : fleet) {
                Thread t = new Thread(v);
                vehicleThreads.add(t);
                t.start();
            }
            lblStatus.setText("Simulation RUNNING...");
            btnStart.setEnabled(false);
            btnPause.setEnabled(true);
            btnStop.setEnabled(true);
        });

        btnPause.addActionListener(e -> {
            for (Vehicle v : fleet) v.pauseSimulation();
            lblStatus.setText("Simulation PAUSED.");
            btnPause.setEnabled(false);
            btnResume.setEnabled(true);
        });

        btnResume.addActionListener(e -> {
            for (Vehicle v : fleet) v.resumeSimulation();
            lblStatus.setText("Simulation RUNNING...");
            btnPause.setEnabled(true);
            btnResume.setEnabled(false);
        });

        btnStop.addActionListener(e -> {
            for (Vehicle v : fleet) v.stopSimulation();
            lblStatus.setText("Simulation STOPPED.");
            btnStart.setEnabled(false);
            btnPause.setEnabled(false);
            btnResume.setEnabled(false);
            btnStop.setEnabled(false);
            btnRefuel1.setEnabled(false);
            btnRefuel2.setEnabled(false);
            btnRefuel3.setEnabled(false);
        });

        // Refuel Logic
        btnRefuel1.addActionListener(e -> performRefuel(0, 5.0));
        btnRefuel2.addActionListener(e -> performRefuel(1, 10.0));
        btnRefuel3.addActionListener(e -> performRefuel(2, 20.0));
    }

    private void performRefuel(int vehicleIndex, double amount) {
        try {
            Vehicle v = fleet.get(vehicleIndex);
            // Cast is safe because we know the order
            if (v instanceof fleetmanagement.interfaces.FuelConsumable) {
                ((fleetmanagement.interfaces.FuelConsumable) v).refuel(amount);
                v.resumeSimulation(); // Resume immediately after refuel
            }
        } catch (InvalidOperationException ex) {
            ex.printStackTrace();
        }
    }

    private void updateGUILabels() {
        double realTotalMileage = 0;
        for(Vehicle v : fleet) {
            realTotalMileage += v.getCurrentMileage();
        }

        lblCounter.setText(String.format(
                "Shared Highway Distance: %d  |  Real Total: %.0f",
                highwayDistance, realTotalMileage
        ));

        if (fleet.size() >= 3) {
            updateVehicleLabel(lblVehicle1, btnRefuel1, (Car)fleet.get(0));
            updateVehicleLabel(lblVehicle2, btnRefuel2, (Truck)fleet.get(1));
            updateVehicleLabel(lblVehicle3, btnRefuel3, (Airplane)fleet.get(2));
        }
    }

    private void updateVehicleLabel(JLabel label, JButton refuelBtn, Vehicle v) {
        // Safe cast to access fuel level (all these extend FuelConsumable in this setup)
        double fuel = 0;
        if (v instanceof fleetmanagement.interfaces.FuelConsumable) {
            fuel = ((fleetmanagement.interfaces.FuelConsumable) v).getFuelLevel();
        }

        label.setText(String.format("<html><b>%s</b> (%s): %.0f km travelled<br/>Fuel: %.1f L  |  Status: <font color='%s'>%s</font></html>",
                v.getClass().getSimpleName(), v.getId(), v.getCurrentMileage(), fuel,
                v.getStatus().equals("Running") ? "green" : "red", v.getStatus()));

        // Only enable refuel if status is explicitly "Out of Fuel"
        if (v.getStatus().equals("Out of Fuel")) {
            refuelBtn.setEnabled(true);
        } else {
            refuelBtn.setEnabled(false);
        }
    }
}