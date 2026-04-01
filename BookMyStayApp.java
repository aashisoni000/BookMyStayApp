import java.util.*;

// --- UC9: Custom Exceptions ---
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) { super(message); }
}

class RoomNotAvailableException extends Exception {
    public RoomNotAvailableException(String message) { super(message); }
}

// --- UC2 & UC7: Models ---
class Service {
    private String name;
    private double cost;
    public Service(String name, double cost) { this.name = name; this.cost = cost; }
    public String getName() { return name; }
    public double getCost() { return cost; }
}

// --- UC5, UC8 & UC9: Reservation with Validation ---
class Reservation {
    private String reservationId;
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) throws InvalidBookingException {
        // UC9: Input Validation
        if (guestName == null || guestName.trim().isEmpty()) {
            throw new InvalidBookingException("Guest name cannot be empty.");
        }
        if (roomType == null || roomType.trim().isEmpty()) {
            throw new InvalidBookingException("Room type must be specified.");
        }
        this.reservationId = "RES-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }

    @Override
    public String toString() {
        return "[" + reservationId + "] Guest: " + guestName + " (" + roomType + ")";
    }
}

// --- UC3, UC6 & UC9: Room Inventory with Guard Clauses ---
class RoomInventory {
    private Map<String, Integer> availability = new HashMap<>();
    private Map<String, Set<String>> allocatedRooms = new HashMap<>();

    public void addRoomType(String type, int count) {
        availability.put(type, count);
        allocatedRooms.put(type, new HashSet<>());
    }

    // UC9: Fail-Fast Design
    public void validateAndAllocate(Reservation res, BookingHistory history) throws RoomNotAvailableException {
        String type = res.getRoomType();

        if (!availability.containsKey(type)) {
            throw new RoomNotAvailableException("Room type '" + type + "' does not exist in our system.");
        }

        int count = availability.get(type);
        if (count <= 0) {
            throw new RoomNotAvailableException("No " + type + " rooms left in inventory.");
        }

        // Logic only proceeds if validation passes
        String roomId = type.substring(0, 3).toUpperCase() + "-" + (allocatedRooms.get(type).size() + 1);
        allocatedRooms.get(type).add(roomId);
        availability.put(type, count - 1);

        history.recordConfirmation(res);
        System.out.println("SUCCESS: " + roomId + " assigned to " + res.getGuestName());
    }
}

// --- UC8: Booking History ---
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();
    public void recordConfirmation(Reservation res) { history.add(res); }
    public List<Reservation> getAllRecords() { return new ArrayList<>(history); }
}

// --- Main Application ---
public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("Welcome to Book My Stay v1.9 [Validation Mode]");
        System.out.println("----------------------------------------------");

        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("Single", 1); // Only ONE room available
        BookingHistory history = new BookingHistory();

        try {
            // SCENARIO 1: Valid Booking
            System.out.println("\nAttempting Valid Booking...");
            Reservation res1 = new Reservation("Alice", "Single");
            inventory.validateAndAllocate(res1, history);

            // SCENARIO 2: Inventory Exhaustion (UC9 Exception)
            System.out.println("\nAttempting Over-booking...");
            Reservation res2 = new Reservation("Bob", "Single");
            inventory.validateAndAllocate(res2, history);

        } catch (InvalidBookingException | RoomNotAvailableException e) {
            System.err.println("BOOKING ERROR: " + e.getMessage());
        }

        try {
            // SCENARIO 3: Invalid Room Type (UC9 Exception)
            System.out.println("\nAttempting Invalid Room Type...");
            Reservation res3 = new Reservation("Charlie", "Penthouse");
            inventory.validateAndAllocate(res3, history);

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: " + e.getMessage());
        }

        System.out.println("\nSystem remains stable. Total confirmed: " + history.getAllRecords().size());
    }
}
