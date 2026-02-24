class ParkingSlot {
    int slotNumber;
    boolean isOccupied;
    Vehicle vehicle;

    ParkingSlot(int slotNumber) {
        this.slotNumber = slotNumber;
        this.isOccupied = false;
        this.vehicle = null;
    }
}
