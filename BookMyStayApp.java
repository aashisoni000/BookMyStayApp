import java.util.HashMap;
import java.util.Map;

// --- UC2: Domain Model ---
abstract class Room {
    private String type;
    private int beds;
    private double price;

    public Room(String type, int beds, double price) {
        this.type = type;
        this.beds = beds;
        this.price = price;
    }

    public String getType() { return type; }

    public void displayInfo() {
        System.out.println("Room Type: " + type + " | Beds: " + beds + " | Price: $" + price);
    }
}

class SingleRoom extends Room { public SingleRoom() { super("Single", 1, 100.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double", 2, 180.0); } }
class SuiteRoom extends Room { public SuiteRoom() { super("Suite", 3, 350.0); } }

// --- UC3: Centralized Room Inventory Management ---
class RoomInventory {
    private Map<String, Integer> inventory;

    public RoomInventory() {
        this.inventory = new HashMap<>();
    }

    public void addRoomType(String type, int count) {
        inventory.put(type, count);
    }

    // Controlled update: logic for booking or cancelling can be added here later
    public void updateAvailability(String type, int count) {
        if (inventory.containsKey(type)) {
            inventory.put(type, count);
        }
    }

    public int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    public void displayInventory() {
        System.out.println("--- Current Room Inventory ---");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + " Rooms Available: " + entry.getValue());
        }
        System.out.println("------------------------------");
    }
}

public class BookMyStayApp {
    public static void main(String[] args) {
        // UC1: Welcome Message
        System.out.println("Welcome to Book My Stay v1.2");
        System.out.println("----------------------------");

        // UC3: Initialize Centralized Inventory
        RoomInventory hotelInventory = new RoomInventory();

        hotelInventory.addRoomType("Single", 5);
        hotelInventory.addRoomType("Double", 3);
        hotelInventory.addRoomType("Suite", 1);

        // UC2: Polymorphism with Room Objects
        Room single = new SingleRoom();
        Room dual = new DoubleRoom();
        Room suite = new SuiteRoom();
        single.displayInfo();
        System.out.println("Live Inventory: " + hotelInventory.getAvailability(single.getType()));
        System.out.println();

        dual.displayInfo();
        System.out.println("Live Inventory: " + hotelInventory.getAvailability(dual.getType()));
        System.out.println();

        suite.displayInfo();
        System.out.println("Live Inventory: " + hotelInventory.getAvailability(suite.getType()));
        System.out.println();

        System.out.println("Update: One Suite Room Booked...");
        hotelInventory.updateAvailability("Suite", 0);

        hotelInventory.displayInventory();
        System.out.println("End of Application Execution.");
    }
}
