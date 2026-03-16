import java.util.*;

// --- UC2: Domain Model ---
abstract class Room {
    private String type;
    private double price;

    public Room(String type, double price) {
        this.type = type;
        this.price = price;
    }

    public String getType() { return type; }
    public void displayInfo() {
        System.out.print("Room Type: " + type + " | Price: $" + price);
    }
}

class SingleRoom extends Room { public SingleRoom() { super("Single", 100.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double", 180.0); } }

// --- UC5: Reservation Model ---
class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }

    @Override
    public String toString() {
        return "Reservation [Guest: " + guestName + ", Type: " + roomType + "]";
    }
}

// --- UC3 & UC6: Room Inventory & Allocation Service ---
class RoomInventory {
    private Map<String, Integer> availability = new HashMap<>();
    // UC6: Set ensures unique Room IDs (No double-booking)
    private Map<String, Set<String>> allocatedRooms = new HashMap<>();

    public void addRoomType(String type, int count) {
        availability.put(type, count);
        allocatedRooms.put(type, new HashSet<>());
    }

    public int getAvailability(String type) {
        return availability.getOrDefault(type, 0);
    }

    public boolean allocateRoom(String type, String guestName) {
        int count = getAvailability(type);
        if (count > 0) {
            String roomId = type.substring(0, 3).toUpperCase() + "-" + (allocatedRooms.get(type).size() + 1);

            // UC6: Uniqueness Enforcement
            allocatedRooms.get(type).add(roomId);
            availability.put(type, count - 1); // Immediate Synchronization

            System.out.println("SUCCESS: Room " + roomId + " allocated to " + guestName);
            return true;
        }
        System.out.println("FAILED: No availability for " + type);
        return false;
    }

    public void displayFinalState() {
        System.out.println("\n--- Final System State ---");
        allocatedRooms.forEach((type, rooms) -> {
            System.out.println(type + " Allocated IDs: " + rooms + " | Remaining: " + availability.get(type));
        });
    }
}

// --- UC5: Booking Request Queue (FIFO) ---
class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation res) {
        queue.add(res);
        System.out.println("Enqueued: " + res);
    }

    public Reservation nextRequest() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

// --- Main Application ---
public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("Welcome to Book My Stay v1.5 [Allocation Mode]");
        System.out.println("----------------------------------------------");

        // 1. Initialize Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("Single", 2); // Only 2 rooms available
        inventory.addRoomType("Double", 1);

        // 2. Queue Up Requests (UC5)
        BookingRequestQueue requestQueue = new BookingRequestQueue();
        requestQueue.addRequest(new Reservation("Alice", "Single"));
        requestQueue.addRequest(new Reservation("Bob", "Single"));
        requestQueue.addRequest(new Reservation("Charlie", "Single")); // This should fail (Inventory is 2)

        // 3. Process Allocation (UC6)
        System.out.println("\nProcessing Queued Requests (FIFO)...");
        while (!requestQueue.isEmpty()) {
            Reservation current = requestQueue.nextRequest();
            System.out.println("Processing: " + current.getGuestName() + "...");
            inventory.allocateRoom(current.getRoomType(), current.getGuestName());
        }

        inventory.displayFinalState();
    }
}
