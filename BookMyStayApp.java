// UC2
abstract class Room {
    private String type;
    private int beds;
    private double price;

    public Room(String type, int beds, double price) {
        this.type = type;
        this.beds = beds;
        this.price = price;
    }


    public void displayInfo() {
        System.out.println("Room Type: " + type + " | Beds: " + beds + " | Price: $" + price);
    }
}

class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room", 1, 100.0);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room", 2, 180.0);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite Room", 3, 350.0);
    }
}

public class BookMyStayApp {

    static int singleRoomAvailability = 5;
    static int doubleRoomAvailability = 3;
    static int suiteRoomAvailability = 1;

    //UC1
    public static void main(String[] args) {
        System.out.println("Welcome to Book My Stay v1.1");
        System.out.println("----------------------------");

        Room single = new SingleRoom();
        Room dual = new DoubleRoom();
        Room suite = new SuiteRoom();

        single.displayInfo();
        System.out.println("Available: " + singleRoomAvailability);
        System.out.println();

        dual.displayInfo();
        System.out.println("Available: " + doubleRoomAvailability);
        System.out.println();

        suite.displayInfo();
        System.out.println("Available: " + suiteRoomAvailability);
        System.out.println();

        System.out.println("End of Room Inventory.");
    }
}
