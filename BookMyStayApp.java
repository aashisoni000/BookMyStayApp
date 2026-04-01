import java.util.*;

// --- UC9: Custom Exceptions ---
class InvalidBookingException extends Exception { public InvalidBookingException(String m) { super(m); } }
class RoomNotAvailableException extends Exception { public RoomNotAvailableException(String m) { super(m); } }

// --- UC5, UC8 & UC10: Reservation with Cancellation State ---
class Reservation {
    private String reservationId;
    private String guestName;
    private String roomType;
    private String allocatedRoomId; // Track which specific ID was given

    public Reservation(String guestName, String roomType) throws InvalidBookingException {
        if (guestName == null || guestName.isEmpty()) throw new InvalidBookingException("Invalid Guest");
        this.reservationId = "RES-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getReservationId() { return reservationId; }
    public String getRoomType() { return roomType; }
    public void setAllocatedRoomId(String id) { this.allocatedRoomId = id; }
    public String getAllocatedRoomId() { return allocatedRoomId; }

    @Override
    public String toString() { return "[" + reservationId + "] " + guestName + " Room: " + allocatedRoomId; }
}

// --- UC3, UC6 & UC10: Inventory with Stack-based Rollback ---
class RoomInventory {
    private Map<String, Integer> availability = new HashMap<>();
    // UC10: Stack tracks released Room IDs for reuse (LIFO Rollback)
    private Map<String, Stack<String>> releasedRooms = new HashMap<>();
    private Map<String, Set<String>> activeAllocations = new HashMap<>();

    public void addRoomType(String type, int count) {
        availability.put(type, count);
        releasedRooms.put(type, new Stack<>());
        activeAllocations.put(type, new HashSet<>());
    }

    public String allocateRoom(Reservation res) throws RoomNotAvailableException {
        String type = res.getRoomType();
        if (availability.getOrDefault(type, 0) <= 0) throw new RoomNotAvailableException("No vacancy");

        String roomId;
        // UC10: Priority to reused IDs from the Stack
        if (!releasedRooms.get(type).isEmpty()) {
            roomId = releasedRooms.get(type).pop();
        } else {
            roomId = type.substring(0, 3).toUpperCase() + "-" + (activeAllocations.get(type).size() + 1);
        }

        activeAllocations.get(type).add(roomId);
        availability.put(type, availability.get(type) - 1);
        res.setAllocatedRoomId(roomId);
        return roomId;
    }

    public void rollbackRoom(Reservation res) {
        String type = res.getRoomType();
        String roomId = res.getAllocatedRoomId();

        activeAllocations.get(type).remove(roomId);
        releasedRooms.get(type).push(roomId); // UC10: Push back to Stack
        availability.put(type, availability.get(type) + 1); // Inventory Restoration

        System.out.println("ROLLBACK: Room " + roomId + " returned to inventory.");
    }
}

// --- UC10: Cancellation Service ---
class CancellationService {
    public void cancelBooking(String resId, List<Reservation> history, RoomInventory inventory) {
        Reservation toCancel = null;
        for (Reservation r : history) {
            if (r.getReservationId().equals(resId)) {
                toCancel = r;
                break;
            }
        }

        if (toCancel != null) {
            inventory.rollbackRoom(toCancel);
            history.remove(toCancel);
            System.out.println("SUCCESS: Cancellation complete for " + resId);
        } else {
            System.out.println("ERROR: Reservation " + resId + " not found.");
        }
    }
}

// --- Main Application ---
public class BookMyStayApp {
    public static void main(String[] args) throws Exception {
        System.out.println("Book My Stay v1.10 [Cancellation & Rollback Mode]");

        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("Single", 2);
        List<Reservation> history = new ArrayList<>();
        CancellationService cancelService = new CancellationService();

        // 1. Process a Booking
        Reservation res = new Reservation("Alice", "Single");
        inventory.allocateRoom(res);
        history.add(res);
        System.out.println("Confirmed: " + res);

        // 2. Perform Cancellation (UC10)
        System.out.println("\nInitiating Cancellation...");
        cancelService.cancelBooking(res.getReservationId(), history, inventory);

        // 3. Verify Inventory Restoration
        System.out.println("\nAttempting re-booking of same room type...");
        Reservation res2 = new Reservation("Bob", "Single");
        inventory.allocateRoom(res2);
        System.out.println("New Booking: " + res2);
    }
}