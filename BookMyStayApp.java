import java.util.*;

// --- UC9 & UC11: Custom Exceptions & Thread-Safe Models ---
class RoomNotAvailableException extends Exception { public RoomNotAvailableException(String m) { super(m); } }

class Reservation {
    private String reservationId;
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.reservationId = "RES-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        this.guestName = guestName;
        this.roomType = roomType;
    }
    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    @Override
    public String toString() { return "[" + reservationId + "] " + guestName; }
}

// --- UC11: Thread-Safe Room Inventory ---
class RoomInventory {
    private Map<String, Integer> availability = new HashMap<>();
    private Map<String, Set<String>> allocatedRooms = new HashMap<>();

    public void addRoomType(String type, int count) {
        availability.put(type, count);
        allocatedRooms.put(type, new HashSet<>());
    }

    // UC11: Synchronized Method to prevent Race Conditions during allocation
    public synchronized boolean allocateRoom(Reservation res) {
        String type = res.getRoomType();
        int count = availability.getOrDefault(type, 0);

        if (count > 0) {
            // Simulate processing delay to expose potential race conditions if not synchronized
            try { Thread.sleep(10); } catch (InterruptedException e) {}

            String roomId = type.substring(0, 3).toUpperCase() + "-" + (allocatedRooms.get(type).size() + 1);
            allocatedRooms.get(type).add(roomId);
            availability.put(type, count - 1);

            System.out.println(Thread.currentThread().getName() + " SUCCESS: " + roomId + " for " + res.getGuestName());
            return true;
        }
        System.out.println(Thread.currentThread().getName() + " FAILED: No rooms for " + res.getGuestName());
        return false;
    }

    public void displayFinalState() {
        System.out.println("\n--- Final Inventory State ---");
        availability.forEach((type, count) -> System.out.println(type + " Remaining: " + count));
    }
}

// --- UC11: Concurrent Booking Processor ---
class BookingTask implements Runnable {
    private RoomInventory inventory;
    private Reservation reservation;

    public BookingTask(RoomInventory inventory, Reservation reservation) {
        this.inventory = inventory;
        this.reservation = reservation;
    }

    @Override
    public void run() {
        inventory.allocateRoom(reservation);
    }
}

// --- Main Application ---
public class BookMyStayApp {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Book My Stay v1.11 [Concurrent Mode]");
        System.out.println("-------------------------------------");

        // Initialize Inventory with limited rooms
        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("Single", 2);

        // Create multiple concurrent requests for the same 2 rooms
        List<Thread> threads = new ArrayList<>();
        String[] guests = {"Alice", "Bob", "Charlie", "Dave", "Eve"};

        System.out.println("Launching " + guests.length + " concurrent booking threads...");

        for (String name : guests) {
            Reservation res = new Reservation(name, "Single");
            Thread t = new Thread(new BookingTask(inventory, res), "Thread-" + name);
            threads.add(t);
            t.start(); // Start concurrent execution
        }

        // Wait for all threads to complete (Join)
        for (Thread t : threads) {
            t.join();
        }

        // Verify that exactly 0 rooms remain and no double-booking occurred
        inventory.displayFinalState();
    }
}
