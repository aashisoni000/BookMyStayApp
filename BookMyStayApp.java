import java.util.*;

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
        System.out.print("Room Type: " + type + " | Beds: " + beds + " | Price: $" + price);
    }
}

class SingleRoom extends Room { public SingleRoom() { super("Single", 1, 100.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double", 2, 180.0); } }
class SuiteRoom extends Room { public SuiteRoom() { super("Suite", 3, 350.0); } }

// --- UC5: Reservation Model ---
class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    @Override
    public String toString() {
        return "Reservation [Guest: " + guestName + ", Requested: " + roomType + "]";
    }
}

// --- UC3: Centralized Room Inventory Management ---
class RoomInventory {
    private Map<String, Integer> inventory = new HashMap<>();

    public void addRoomType(String type, int count) {
        inventory.put(type, count);
    }

    public int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }
}

// --- UC4: Search Service (Read-Only) ---
class SearchService {
    private RoomInventory inventory;
    private List<Room> roomTemplates;

    public SearchService(RoomInventory inventory, List<Room> roomTemplates) {
        this.inventory = inventory;
        this.roomTemplates = roomTemplates;
    }

    public void searchAvailableRooms() {
        System.out.println("\n--- Available Rooms ---");
        for (Room room : roomTemplates) {
            int count = inventory.getAvailability(room.getType());
            if (count > 0) {
                room.displayInfo();
                System.out.println(" | Available: " + count);
            }
        }
        System.out.println("------------------------\n");
    }
}

// --- UC5: Booking Request Queue (FIFO) ---
class BookingRequestQueue {
    private Queue<Reservation> requestQueue = new LinkedList<>();

    public void addRequest(Reservation res) {
        requestQueue.add(res);
        System.out.println("Enqueued: " + res);
    }

    public void showQueue() {
        System.out.println("\n--- Current Booking Queue (Waiting for Processing) ---");
        if (requestQueue.isEmpty()) {
            System.out.println("Queue is empty.");
        } else {
            for (Reservation res : requestQueue) {
                System.out.println(res);
            }
        }
        System.out.println("------------------------------------------------------\n");
    }
}

// --- Main Application ---
public class BookMyStayApp {
    public static void main(String[] args) {
        // UC1: Welcome Message
        System.out.println("Welcome to Book My Stay v1.4");

        RoomInventory hotelInventory = new RoomInventory();
        hotelInventory.addRoomType("Single", 5);
        hotelInventory.addRoomType("Double", 3);

        List<Room> roomTypes = Arrays.asList(new SingleRoom(), new DoubleRoom(), new SuiteRoom());

        // UC4: Search
        SearchService searchService = new SearchService(hotelInventory, roomTypes);
        searchService.searchAvailableRooms();

        // UC5: Booking Request Handling
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        System.out.println("Incoming Booking Requests...");
        bookingQueue.addRequest(new Reservation("Alice", "Single"));
        bookingQueue.addRequest(new Reservation("Bob", "Double"));
        bookingQueue.addRequest(new Reservation("Charlie", "Single"));

        bookingQueue.showQueue();

        System.out.println("System Note: Requests are queued. No inventory has been deducted yet.");
    }
}
