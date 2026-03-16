import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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
    public double getPrice() { return price; }

    public void displayInfo() {
        System.out.print("Room Type: " + type + " | Beds: " + beds + " | Price: $" + price);
    }
}

class SingleRoom extends Room { public SingleRoom() { super("Single", 1, 100.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double", 2, 180.0); } }
class SuiteRoom extends Room { public SuiteRoom() { super("Suite", 3, 350.0); } }

// --- UC3: Centralized Room Inventory Management ---
class RoomInventory {
    private Map<String, Integer> inventory = new HashMap<>();

    public void addRoomType(String type, int count) {
        inventory.put(type, count);
    }

    public void updateAvailability(String type, int count) {
        if (inventory.containsKey(type)) inventory.put(type, count);
    }

    public int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }
}

// --- UC4: Room Search & Availability Check (Read-Only Service) ---
class SearchService {
    private RoomInventory inventory;
    private List<Room> roomTemplates;

    public SearchService(RoomInventory inventory, List<Room> roomTemplates) {
        this.inventory = inventory;
        this.roomTemplates = roomTemplates;
    }

    public void searchAvailableRooms() {
        System.out.println("\n--- Search Results: Available Rooms ---");
        boolean found = false;

        for (Room room : roomTemplates) {
            int count = inventory.getAvailability(room.getType());

            if (count > 0) {
                room.displayInfo();
                System.out.println(" | Available: " + count);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Sorry, no rooms are currently available.");
        }
        System.out.println("---------------------------------------\n");
    }
}

// --- Main Application ---
public class BookMyStayApp {
    public static void main(String[] args) {
        // UC1: Welcome Message
        System.out.println("Welcome to Book My Stay v1.3");

        // UC3: Setup Inventory
        RoomInventory hotelInventory = new RoomInventory();
        hotelInventory.addRoomType("Single", 5);
        hotelInventory.addRoomType("Double", 3);
        hotelInventory.addRoomType("Suite", 0); // Setting Suite to 0 to test UC4 filter

        // UC2: Room Objects
        List<Room> roomTypes = new ArrayList<>();
        roomTypes.add(new SingleRoom());
        roomTypes.add(new DoubleRoom());
        roomTypes.add(new SuiteRoom());

        // UC4: Initialize Search Service (Read-Only)
        SearchService searchService = new SearchService(hotelInventory, roomTypes);

        System.out.println("Guest is searching for available rooms...");
        searchService.searchAvailableRooms();

        System.out.println("System Check: Search complete. No inventory was modified.");
    }
}
