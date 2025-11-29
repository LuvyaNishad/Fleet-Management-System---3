package fleetmanagement.gui;

import fleetmanagement.vehicles.Vehicle;
import fleetmanagement.vehicles.Car;
import fleetmanagement.vehicles.Truck;
import fleetmanagement.vehicles.Airplane;
import fleetmanagement.exceptions.InvalidOperationException;
import fleetmanagement.interfaces.HighwayTracker;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/* * Entry point and GUI handler for the assignment.
 * This class acts as the main controller, managing vehicle threads, the shared highway counter,
 * and the Swing graphical interface.
 *
 * KEY CONCEPTS USED:
 * 1. Multithreading: Each vehicle runs in a separate Thread.
 * 2. Shared Resources: 'highwayDistance' is accessed by multiple threads.
 * 3. Synchronization: The 'synchronized' keyword prevents race conditions on the shared counter.
 * 4. Collections: 'ArrayList' is used to manage the fleet and thread objects.
 * 5. Thread-Safe GUI: Updates are pushed to the Event Dispatch Thread (EDT).
 */
public class HighwaySimulatorGUI implements HighwayTracker {

    // SHARED RESOURCE
    // This static variable acts as the shared memory resource.
    // It tracks the cumulative distance traveled by all vehicles combined.
    public static int highwayDistance = 0;

    // --- COLLECTIONS ---
    // ArrayLists used for dynamic storage of Vehicle objects and their corresponding Threads.
    private List<Vehicle> fleet = new ArrayList<>();
    private List<Thread> vehicleThreads = new ArrayList<>();

    // --- GUI COMPONENTS ---
    private JFrame frame;
    private JButton btnStart, btnPause, btnResume, btnStop;
    private JLabel lblCounter, lblStatus;
    private JLabel lblVehicle1, lblVehicle2, lblVehicle3;
    private JButton btnRefuel1, btnRefuel2, btnRefuel3;

    // --- SYNCHRONIZATION / RACE CONDITION FIX ---
    // This method implements the HighwayTracker interface.
    // The 'synchronized' keyword ensures ATOMIC access. Only one thread can enter
    // this method at a time, preventing lost updates (Race Condition).
    @Override
    public synchronized void incrementHighwayCounter() {
        highwayDistance++;
    }

    // --- MAIN ENTRY POINT ---
    public static void main(String[] args) {
        // SwingUtilities.invokeLater ensures the GUI creation runs on the
        // Event Dispatch Thread (EDT) for thread safety.
        SwingUtilities.invokeLater(() -> {
            try {
                new HighwaySimulatorGUI().createAndShowGUI();
            } catch (InvalidOperationException e) {
                e.printStackTrace();
            }
        });
    }

    /*
     * Initializes the GUI components, layout, and starts the update timer.
     * Uses BorderLayout for main structure and BoxLayout/GridLayout for sub-panels.
     */
    public void createAndShowGUI() throws InvalidOperationException {
        frame = new JFrame("Fleet Highway Simulator (Professional Version)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 500);
        frame.setLayout(new BorderLayout());

        // 1. Top Control Panel (FlowLayout by default)
        // Contains main simulation controls (Start, Pause, Stop).
        JPanel controlPanel = new JPanel();
        btnStart = new JButton("Start");
        btnPause = new JButton("Pause");
        btnResume = new JButton("Resume");
        btnStop = new JButton("Stop");

        // Set initial button states
        btnPause.setEnabled(false);
        btnResume.setEnabled(false);
        btnStop.setEnabled(false);

        controlPanel.add(btnStart);
        controlPanel.add(btnPause);
        controlPanel.add(btnResume);
        controlPanel.add(btnStop);
        frame.add(controlPanel, BorderLayout.NORTH);

        // 2. Vehicle Status Panel (BoxLayout - Vertical Stack)
        // Displays individual stats for each vehicle in the fleet.
        JPanel vehicleListPanel = new JPanel();
        vehicleListPanel.setLayout(new BoxLayout(vehicleListPanel, BoxLayout.Y_AXIS));
        vehicleListPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Status"));

        // Setup Vehicle Rows using helper method for consistent styling
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

        // 3. Bottom Status Panel (GridLayout)
        // Displays the Shared Counter and general simulation status.
        JPanel statusPanel = new JPanel(new GridLayout(2, 1));
        lblCounter = new JLabel("Shared Highway Distance: 0");
        lblCounter.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblCounter.setHorizontalAlignment(SwingConstants.CENTER);

        lblStatus = new JLabel("Click Start to begin simulation.");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        statusPanel.add(lblCounter);
        statusPanel.add(lblStatus);
        frame.add(statusPanel, BorderLayout.SOUTH);

        // Initialize Logic
        setupFleet();
        addListeners();

        // --- GUI TIMER ---
        // Creates a javax.swing.Timer that fires every 100ms.
        // This polls the vehicle objects for changes and updates labels safely on the EDT.
        new Timer(100, e -> updateGUILabels()).start();

        frame.setVisible(true);
    }

    /*
     * Helper method to create a standardized UI row for a single vehicle.
     * Uses nested Border layouts for alignment.
     */
    private JPanel createVehicleRow(String labelText, JButton refuelBtn) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(labelText);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        refuelBtn.setPreferredSize(new Dimension(130, 30));
        refuelBtn.setEnabled(false);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(refuelBtn);

        panel.add(label, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.EAST);
        // Add a bottom line separator for cleaner look
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        return panel;
    }

    /*
     * Instantiates the specific vehicles (Car, Truck, Airplane) and adds them to the fleet list.
     * Passes 'this' (the GUI) to the vehicles so they can call incrementHighwayCounter().
     */
    private void setupFleet() throws InvalidOperationException {
        // 1. Car: 50.0 Liters
        Car car1 = new Car("C001", "Toyota Camry", 180.0, 4);
        car1.refuel(50.0);
        car1.setSimulator(this); // Passes 'this' as a HighwayTracker implementation

        // 2. Truck: 100.0 Liters
        Truck truck1 = new Truck("T001", "Ford F-150", 120.0, 6);
        truck1.refuel(100.0);
        truck1.setSimulator(this);

        // 3. Airplane: 500.0 Liters
        Airplane plane1 = new Airplane("A001", "Boeing 737", 850.0, 35000.0);
        plane1.refuel(500.0);
        plane1.setSimulator(this);

        fleet.add(car1);
        fleet.add(truck1);
        fleet.add(plane1);
    }

    /*
     * Defines ActionListeners (Lambdas) for all buttons.
     * Manages the lifecycle of threads (Start, Pause, Resume, Stop).
     */
    private void addListeners() {
        // START: Creates new Threads for each vehicle and starts them.
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

        // PAUSE: Sets a volatile flag in the vehicle objects to pause execution.
        btnPause.addActionListener(e -> {
            for (Vehicle v : fleet) v.pauseSimulation();
            lblStatus.setText("Simulation PAUSED.");
            btnPause.setEnabled(false);
            btnResume.setEnabled(true);
        });

        // RESUME: Unsets the pause flag, allowing threads to continue loop.
        btnResume.addActionListener(e -> {
            for (Vehicle v : fleet) v.resumeSimulation();
            lblStatus.setText("Simulation RUNNING...");
            btnPause.setEnabled(true);
            btnResume.setEnabled(false);
        });

        // STOP: Sets running flag to false, terminating the thread loop.
        btnStop.addActionListener(e -> {
            for (Vehicle v : fleet) v.stopSimulation();
            lblStatus.setText("Simulation STOPPED.");
            // Reset UI State
            btnStart.setEnabled(false);
            btnPause.setEnabled(false);
            btnResume.setEnabled(false);
            btnStop.setEnabled(false);
            btnRefuel1.setEnabled(false);
            btnRefuel2.setEnabled(false);
            btnRefuel3.setEnabled(false);
        });

        // REFUEL: Logic to refill specific vehicles
        btnRefuel1.addActionListener(e -> performRefuel(0, 50.0));
        btnRefuel2.addActionListener(e -> performRefuel(1, 100.0));
        btnRefuel3.addActionListener(e -> performRefuel(2, 500.0));
    }

    /*
     * Logic to handle refueling. Checks if vehicle implements FuelConsumable interface.
     */
    private void performRefuel(int vehicleIndex, double amount) {
        try {
            Vehicle v = fleet.get(vehicleIndex);
            if (v instanceof fleetmanagement.interfaces.FuelConsumable) {
                ((fleetmanagement.interfaces.FuelConsumable) v).refuel(amount);
            }
        } catch (InvalidOperationException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * Called by the Swing Timer. Calculates totals and updates labels.
     * This separates the simulation logic (Threads) from display logic (EDT).
     */
    private void updateGUILabels() {
        double realTotalMileage = 0;
        // Iterate through fleet to calculate actual total distance
        for(Vehicle v : fleet) {
            realTotalMileage += v.getCurrentMileage();
        }

        // Update the main counter label
        lblCounter.setText(String.format(
                "Shared Highway Distance: %d  |  Real Total: %.0f",
                highwayDistance, realTotalMileage
        ));

        // Update specific vehicle rows if fleet is populated
        if (fleet.size() >= 3) {
            updateVehicleLabel(lblVehicle1, btnRefuel1, (Car)fleet.get(0));
            updateVehicleLabel(lblVehicle2, btnRefuel2, (Truck)fleet.get(1));
            updateVehicleLabel(lblVehicle3, btnRefuel3, (Airplane)fleet.get(2));
        }
    }

    /*
     * Updates a single vehicle's status label with HTML formatting for colors.
     * Also controls the state of the Refuel button based on vehicle status.
     */
    private void updateVehicleLabel(JLabel label, JButton refuelBtn, Vehicle v) {
        double fuel = 0;
        if (v instanceof fleetmanagement.interfaces.FuelConsumable) {
            fuel = ((fleetmanagement.interfaces.FuelConsumable) v).getFuelLevel();
        }

        String status = v.getStatus();
        String color = "black";

        // Color coding based on status
        if (status.equals("Running")) color = "green";
        else if (status.equals("Out of Fuel")) color = "red";
        else if (status.equals("Paused")) color = "orange";

        // HTML usage in JLabel for rich text formatting
        label.setText(String.format("<html><b>%s</b> (%s): %.0f km travelled<br/>Fuel: %.1f L  |  Status: <font color='%s'>%s</font></html>",
                v.getClass().getSimpleName(), v.getId(), v.getCurrentMileage(), fuel, color, status));

        // Enable refuel button only if paused or out of fuel
        if (!status.equals("Running") && !status.equals("Idle")) {
            refuelBtn.setEnabled(true);
        } else {
            refuelBtn.setEnabled(false);
        }
    }
}