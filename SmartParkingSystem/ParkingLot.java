import java.util.*;
import java.io.*;

class ParkingLot {

    ArrayList<ParkingSlot> slots;
    HashMap<String, ParkingSlot> parkedVehicles;
    Queue<Vehicle> waitingQueue;
    Stack<Vehicle> history;

    int totalSlots;
    double totalRevenue = 0;

    ParkingLot(int totalSlots) {
        this.totalSlots = totalSlots;
        slots = new ArrayList<>();
        parkedVehicles = new HashMap<>();
        waitingQueue = new LinkedList<>();
        history = new Stack<>();

        for (int i = 1; i <= totalSlots; i++) {
            slots.add(new ParkingSlot(i));
        }
    }

    void parkVehicle(String vehicleNumber, String type) {

    if (parkedVehicles.containsKey(vehicleNumber)) {
        System.out.println("Vehicle already parked!");
        return;
    }

    Vehicle v = new Vehicle(vehicleNumber, type);

    int start = -1;
    int end = -1;

    if (type.equalsIgnoreCase("Bike")) {
        start = 0;
        end = 2;
    } 
    else if (type.equalsIgnoreCase("Car")) {
        start = 2;
        end = 4;
    } 
    else {
        System.out.println("Invalid Vehicle Type!");
        return;
    }

    for (int i = start; i < end && i < slots.size(); i++) {
        ParkingSlot slot = slots.get(i);

        if (!slot.isOccupied) {
            slot.vehicle = v;
            slot.isOccupied = true;
            parkedVehicles.put(vehicleNumber, slot);

            System.out.println("Vehicle parked at Slot: " + slot.slotNumber);
            return;
        }
    }

    System.out.println("No available slot for " + type + "!");
}

    void removeVehicle(String vehicleNumber) {
        if (!parkedVehicles.containsKey(vehicleNumber)) {
            System.out.println("Vehicle not found!");
            return;
        }

        ParkingSlot slot = parkedVehicles.get(vehicleNumber);
        Vehicle v = slot.vehicle;

        long exitTime = System.currentTimeMillis();
        long duration = (exitTime - v.entryTime) / 1000;
        double bill = duration * 2; // ₹2 per second

        totalRevenue += bill;

        slot.vehicle = null;
        slot.isOccupied = false;
        parkedVehicles.remove(vehicleNumber);

        System.out.println("Vehicle removed from Slot: " + slot.slotNumber);
        System.out.println("Bill: " + bill);

        if (!waitingQueue.isEmpty()) {
            Vehicle next = waitingQueue.poll();
            parkVehicle(next.vehicleNumber, next.type);
        }
    }

    void showAvailableSlots() {
        for (ParkingSlot slot : slots) {
            if (!slot.isOccupied) {
                System.out.println("Slot " + slot.slotNumber + " is Available");
            }
        }
    }

    void generateReport() {
    try {
        FileWriter writer = new FileWriter("ParkingReport.txt");

        String report = "";
        report += "=====================================\n";
        report += "      SMART PARKING SYSTEM REPORT\n";
        report += "=====================================\n\n";

        report += "Date & Time: " + new Date() + "\n\n";

        report += "Total Parking Slots      : " + totalSlots + "\n";
        report += "Currently Parked Vehicles: " + parkedVehicles.size() + "\n";
        report += "Total Revenue Collected  : ₹" + totalRevenue + "\n\n";

        report += "----------- PARKED VEHICLES -----------\n";

        if (parkedVehicles.isEmpty()) {
            report += "No vehicles currently parked.\n";
        } else {
            for (String vehicleNum : parkedVehicles.keySet()) {
                ParkingSlot slot = parkedVehicles.get(vehicleNum);
                report += "Vehicle No: " + vehicleNum +
                          " | Slot No: " + slot.slotNumber + "\n";
            }
        }

        report += "\n=====================================\n";
        report += "        END OF REPORT\n";
        report += "=====================================\n";

        writer.write(report);
        writer.close();

        System.out.println("\nReport Generated Successfully!");
        System.out.println("Check ParkingReport.txt file.");

    } catch (IOException e) {
        System.out.println("Error Writing File");
    }
}

    void undoLastEntry() {
        if (!history.isEmpty()) {
            Vehicle last = history.pop();
            removeVehicle(last.vehicleNumber);
            System.out.println("Last entry undone.");
        } else {
            System.out.println("No history available.");
        }
    }
}
