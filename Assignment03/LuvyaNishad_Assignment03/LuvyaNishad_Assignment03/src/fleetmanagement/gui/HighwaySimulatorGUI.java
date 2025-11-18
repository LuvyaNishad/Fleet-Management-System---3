package fleetmanagement.gui;

import fleetmanagement.vehicles.Vehicle;
import fleetmanagement.vehicles.Car;
import fleetmanagement.vehicles.Bus;
import fleetmanagement.vehicles.Truck;
import fleetmanagement.vehicles.Airplane;
import fleetmanagement.exceptions.InvalidOperationException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HighwaySimulatorGUI {

    // --- 1. THE SHARED RESOURCE ---
    public static int highwayDistance = 0;

    // --- Fleet and Thread Lists ---
    private List<Vehicle> fleet = new ArrayList<>();
    private List<Thread> vehicleThreads = new ArrayList<>();

    // --- GUI Components ---
    private JFrame frame;
    private JButton btnStart, btnPause, btnResume, btnStop;
    private JLabel lblCounter, lblStatus;
    private JLabel lblVehicle1, lblVehicle2, lblVehicle3;
    private JButton btnRefuel1, btnRefuel2, btnRefuel3;


    // --- 2. THE UNSYNCHRONIZED METHOD (THE BUG!) ---
    /**
     * This method is NOT thread-safe. It does not have the 'synchronized' keyword.
     * This will cause a race condition when multiple threads call it.
     */
    public void incrementHighwayCounter() {
        // The logic is the same, but now it's unprotected.
        int currentDistance = highwayDistance;
        try {
            // This sleep makes the race condition easy to see
            Thread.sleep(5);
        } catch (InterruptedException e) {}
        highwayDistance = currentDistance + 1;
    }


    // --- 3. Main Method (Entry Point) ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new HighwaySimulatorGUI().createAndShowGUI();
            } catch (InvalidOperationException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Creates and configures the GUI.
     * This version uses nested panels for a clean layout.
     */
    public void createAndShowGUI() throws InvalidOperationException {
        // Set the title to "UNCorrected"
        frame = new JFrame("Fleet Highway Simulator (UNCorrected Version)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 400);
        frame.setLayout(new BorderLayout());

        // --- 1. Top Panel (Control Buttons) ---
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

        // --- 2. Center Panel (Vehicle Status) ---
        JPanel vehicleListPanel = new JPanel();
        vehicleListPanel.setLayout(new BoxLayout(vehicleListPanel, BoxLayout.Y_AXIS));
        vehicleListPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Status"));


        // --- 2a. Vehicle 1 Panel (Car) ---
        JPanel panelV1 = new JPanel(new BorderLayout());
        lblVehicle1 = new JLabel("Vehicle 1 Status...");
        lblVehicle1.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

        btnRefuel1 = new JButton("Refuel Car");
        btnRefuel1.setEnabled(false);
        JPanel refuelPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        refuelPanel1.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        refuelPanel1.add(btnRefuel1);

        panelV1.add(lblVehicle1, BorderLayout.CENTER);
        panelV1.add(refuelPanel1, BorderLayout.EAST);

        // --- 2b. Vehicle 2 Panel (Truck) ---
        JPanel panelV2 = new JPanel(new BorderLayout());
        lblVehicle2 = new JLabel("Vehicle 2 Status...");
        lblVehicle2.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

        btnRefuel2 = new JButton("Refuel Truck");
        btnRefuel2.setEnabled(false);
        JPanel refuelPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        refuelPanel2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        refuelPanel2.add(btnRefuel2);

        panelV2.add(lblVehicle2, BorderLayout.CENTER);
        panelV2.add(refuelPanel2, BorderLayout.EAST);

        // --- 2c. Vehicle 3 Panel (Airplane) ---
        JPanel panelV3 = new JPanel(new BorderLayout());
        lblVehicle3 = new JLabel("Vehicle 3 Status...");
        lblVehicle3.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

        btnRefuel3 = new JButton("Refuel Airplane");
        btnRefuel3.setEnabled(false);
        JPanel refuelPanel3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        refuelPanel3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        refuelPanel3.add(btnRefuel3);

        panelV3.add(lblVehicle3, BorderLayout.CENTER);
        panelV3.add(refuelPanel3, BorderLayout.EAST);

        // Add individual vehicle panels to the main vehicle list
        vehicleListPanel.add(panelV1);
        vehicleListPanel.add(panelV2);
        vehicleListPanel.add(panelV3);

        frame.add(vehicleListPanel, BorderLayout.CENTER);

        // --- 3. Bottom Panel (Overall Status) ---
        JPanel statusPanel = new JPanel(new GridLayout(2, 1));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lblCounter = new JLabel("Shared Highway Distance: 0");
        lblCounter.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblStatus = new JLabel("Click Start to begin simulation.");
        lblStatus.setFont(new Font("Monospaced", Font.PLAIN, 14));
        statusPanel.add(lblCounter);
        statusPanel.add(lblStatus);
        frame.add(statusPanel, BorderLayout.SOUTH);

        // --- 4. Initialize Data and Add Listeners ---
        setupFleet();
        addListeners();

        // --- 5. GUI Update Timer ---
        javax.swing.Timer timer = new javax.swing.Timer(100, e -> updateGUILabels());
        timer.start();

        frame.setVisible(true);
    }


    // --- (The rest of the file is unchanged) ---

    /**
     * Creates the vehicles for the simulation (Car, Truck, Airplane).
     */
    private void setupFleet() throws InvalidOperationException {
        // 1. Create a Car
        Car car1 = new Car("C001", "Toyota Camry", 180.0, 4);
        car1.refuel(50.0);
        car1.setSimulator(this);

        // 2. Create a Truck
        Truck truck1 = new Truck("T001", "Ford F-150", 120.0, 6);
        truck1.refuel(100.0);
        truck1.setSimulator(this);

        // 3. Create an Airplane
        Airplane plane1 = new Airplane("A001", "Boeing 737", 850.0, 35000.0);
        plane1.refuel(500.0);
        plane1.setSimulator(this);

        fleet.add(car1);
        fleet.add(truck1);
        fleet.add(plane1);

        updateGUILabels();
    }

    /**
     * This method adds all the button listeners.
     */
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
            btnRefuel1.setEnabled(true);
            btnRefuel2.setEnabled(true);
            btnRefuel3.setEnabled(true);
        });

        btnPause.addActionListener(e -> {
            for (Vehicle v : fleet) {
                v.pauseSimulation();
            }
            lblStatus.setText("Simulation PAUSED.");
            btnPause.setEnabled(false);
            btnResume.setEnabled(true);
        });

        btnResume.addActionListener(e -> {
            for (Vehicle v : fleet) {
                v.resumeSimulation();
            }
            lblStatus.setText("Simulation RUNNING...");
            btnPause.setEnabled(true);
            btnResume.setEnabled(false);
        });

        btnStop.addActionListener(e -> {
            for (Vehicle v : fleet) {
                v.stopSimulation();
            }
            lblStatus.setText("Simulation STOPPED. (Restart app to run again)");
            btnStart.setEnabled(false);
            btnPause.setEnabled(false);
            btnResume.setEnabled(false);
            btnStop.setEnabled(false);
        });

        // Refuel Listeners
        btnRefuel1.addActionListener(e -> {
            try {
                ((Car)fleet.get(0)).refuel(50.0);
                fleet.get(0).resumeSimulation();
            } catch (InvalidOperationException ex) {
                ex.printStackTrace();
            }
        });
        btnRefuel2.addActionListener(e -> {
            try {
                ((Truck)fleet.get(1)).refuel(100.0);
                fleet.get(1).resumeSimulation();
            } catch (InvalidOperationException ex) {
                ex.printStackTrace();
            }
        });
        btnRefuel3.addActionListener(e -> {
            try {
                ((Airplane)fleet.get(2)).refuel(500.0);
                fleet.get(2).resumeSimulation();
            } catch (InvalidOperationException ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * This method updates all the labels safely from the Swing Timer.
     */
    private void updateGUILabels() {
        double realTotalMileage = 0;
        for(Vehicle v : fleet) {
            realTotalMileage += v.getCurrentMileage();
        }

        lblCounter.setText(String.format(
                "Shared Highway Distance: %d (Real Total: %.0f)",
                highwayDistance, realTotalMileage
        ));

        if (fleet.size() >= 3) {
            Vehicle v1 = fleet.get(0);
            Vehicle v2 = fleet.get(1);
            Vehicle v3 = fleet.get(2);

            Car car = (Car) v1;
            Truck truck = (Truck) v2;
            Airplane plane = (Airplane) v3;

            lblVehicle1.setText(String.format("  Car (%s): %.0f km, Fuel: %.0f L, Status: %s",
                    car.getId(), car.getCurrentMileage(), car.getFuelLevel(), car.getStatus()));

            lblVehicle2.setText(String.format("  Truck (%s): %.0f km, Fuel: %.0f L, Status: %s",
                    truck.getId(), truck.getCurrentMileage(), truck.getFuelLevel(), truck.getStatus()));

            lblVehicle3.setText(String.format("  Airplane (%s): %.0f km, Fuel: %.0f L, Status: %s",
                    plane.getId(), plane.getCurrentMileage(), plane.getFuelLevel(), plane.getStatus()));

            btnRefuel1.setEnabled(v1.getStatus().equals("Out of Fuel"));
            btnRefuel2.setEnabled(v2.getStatus().equals("Out of Fuel"));
            btnRefuel3.setEnabled(v3.getStatus().equals("Out of Fuel"));
        }
    }
}