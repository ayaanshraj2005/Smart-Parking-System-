class Vehicle {
    String vehicleNumber;
    String type;
    long entryTime;

    Vehicle(String vehicleNumber, String type) {
        this.vehicleNumber = vehicleNumber;
        this.type = type;
        this.entryTime = System.currentTimeMillis();
    }
}

