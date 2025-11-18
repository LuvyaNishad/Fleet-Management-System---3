TRANSPORTATION FLEET MANAGEMENT SYSTEM - ASSIGNMENT 2
=====================================================

COMPILATION AND EXECUTION
-------------------------
1. Compile all Java files:
   javac -d . fleetmanagement/**/*.java fleetmanagement/cli/Main.java

2. Run the application:
   java fleetmanagement.cli.Main

3. The program will start with sample vehicles and display a menu-driven CLI.

HOW TO USE THE SYSTEM
---------------------
The CLI provides the following menu options:

1. Add Vehicle - Create new vehicles (Car, Truck, Bus, Airplane, Cargo Ship)
2. Remove Vehicle - Remove vehicle by ID
3. Start Journey - Simulate journey for all vehicles
4. Refuel All - Add fuel to all fuel-consuming vehicles
5. Perform Maintenance - Maintain vehicles needing service
6. Generate Report - Comprehensive fleet analytics
7. Save Fleet - Export fleet data to CSV file
8. Load Fleet - Import fleet data from CSV file
9. Search by Type - Find vehicles by type or interface
10. List Vehicles Needing Maintenance - Show vehicles requiring service
11. Sort Fleet - Sort by efficiency, speed, or model name
12. Show Analytics - Fastest/slowest vehicles and unique models
13. Display All Vehicles - Show current fleet order
14. Exit - Close the application

COLLECTIONS USED AND JUSTIFICATION
----------------------------------
- ArrayList<Vehicle>: Primary fleet storage allowing dynamic resizing and efficient iteration
- HashSet<String>: Maintains unique vehicle models with O(1) lookups
- TreeSet<String>: Automatically sorts model names alphabetically

FACTORY METHOD IMPLEMENTATION
-----------------------------
VehicleFactory.createVehicle() creates appropriate vehicle types from CSV data.
Used in FleetManager.loadFromFile() for persistence.

FILE I/O IMPLEMENTATION
-----------------------
- CSV format with type-specific columns
- Robust error handling for malformed files
- Graceful skipping of invalid data lines
- Consistent toCSVString() and fromCSV() methods across vehicles

DEMONSTRATION WALKTHROUGH
-------------------------
1. Run the program - 6 sample vehicles are created automatically
2. Use option 6 (Generate Report) to see Assignment 2 analytics:
   - Total vehicles and unique models count
   - Fastest and slowest vehicle details
   - Alphabetically sorted model list (TreeSet)
   - Vehicle counts by type
   - Average fuel efficiency and total mileage
3. Use option 11 (Sort Fleet) to test sorting:
   - Option 1: Sort by fuel efficiency (best first)
   - Option 2: Sort by maximum speed (fastest first)
   - Option 3: Sort by model name (alphabetical)
4. Use option 12 (Show Analytics) for collection features:
   - Option 1: Find fastest vehicle
   - Option 2: Find slowest vehicle
   - Option 3: Show unique models (HashSet/TreeSet)
5. Test persistence with options 7 and 8

SAMPLE CSV FILE
---------------
The system includes fleet_demo.csv with sample data:
Car,C001,Toyota Camry,180.0,4,50.0,3,1500.5,false
Truck,T001,Ford F-150,120.0,6,100.0,2000.0,800.0,false
Bus,B001,Volvo Bus,100.0,6,150.0,30,200.0,1200.0,false
Airplane,A001,Boeing 737,850.0,35000.0,5000.0,150,5000.0,2500.0,false
CargoShip,S001,Maersk Container,50.0,false,2000.0,15000.0,1800.0,false

OOP PRINCIPLES DEMONSTRATED
---------------------------
- Inheritance: Multi-level vehicle hierarchy
- Polymorphism: Interface implementations and abstract methods
- Abstraction: Abstract classes and interfaces
- Encapsulation: Private fields with controlled access

EXPECTED OUTPUT SAMPLES
-----------------------
Fastest Vehicle: Airplane: Boeing 737 (850.0 km/h)
Slowest Vehicle: CargoShip: Maersk Container (50.0 km/h)
Unique Models: 6 distinct models alphabetically sorted
Efficiency Sorting: Cars (15.0 km/l) first, CargoShips (4.0 km/l) last