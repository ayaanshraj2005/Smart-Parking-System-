import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        ParkingLot parkingLot = new ParkingLot(5);

        while (true) {
            System.out.println("\n1. Park Vehicle");
            System.out.println("2. Remove Vehicle");
            System.out.println("3. Show Available Slots");
            System.out.println("4. Undo Last Entry");
            System.out.println("5. Generate Report");
            System.out.println("6. Exit");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    System.out.print("Enter Vehicle Number: ");
                    String number = sc.next();
                    System.out.print("Enter Vehicle Type: ");
                    String type = sc.next();
                    parkingLot.parkVehicle(number, type);
                    break;

                case 2:
                    System.out.print("Enter Vehicle Number: ");
                    String removeNumber = sc.next();
                    parkingLot.removeVehicle(removeNumber);
                    break;

                case 3:
                    parkingLot.showAvailableSlots();
                    break;

                case 4:
                    parkingLot.undoLastEntry();
                    break;

                case 5:
                    parkingLot.generateReport();
                    break;

                case 6:
                    System.out.println("Exiting...");
                    return;

                default:
                    System.out.println("Invalid Choice!");
            }
        }
    }
}

