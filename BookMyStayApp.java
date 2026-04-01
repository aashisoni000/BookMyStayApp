import java.util.*;

// --- UC2 & UC7: Domain & Service Models ---
class Service {
    private String name;
    private double cost;
    public Service(String name, double cost) { this.name = name; this.cost = cost; }
    public String getName() { return name; }
    public double getCost() { return cost; }
}

// --- UC5 & UC8: Reservation Model with Persistence Mindset ---
class Reservation {
    private String reservationId;
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
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

// --- UC7: Add-On Service Manager ---
class ServiceManager {
    private Map<String, List<Service>> selectedServices = new HashMap<>();

    public void addService(String resId, Service service) {
        selectedServices.computeIfAbsent(resId, k -> new ArrayList<>()).add(service);
    }

    public double getTotalServiceCost(String resId) {
        return selectedServices.getOrDefault(resId, Collections.emptyList())
                .stream().mapToDouble(Service::getCost).sum();
    }
}

// --- UC8: Booking History (Persistence Layer) ---
class BookingHistory {
    // List preserves chronological insertion order for auditing
    private List<Reservation> history = new ArrayList<>();

    public void recordConfirmation(Reservation res) {
        history.add(res);
        System.out.println("HISTORY: Recorded " + res.getReservationId());
    }

    public List<Reservation> getAllRecords() {
        return new ArrayList<>(history); // Return copy to protect internal state
    }
}

// --- UC8: Booking Report Service (Operational Visibility) ---
class ReportingService {
    public void generateSummary(BookingHistory history, ServiceManager sm) {
        System.out.println("\n--- ADMINISTRATIVE BOOKING REPORT ---");
        List<Reservation> records = history.getAllRecords();

        if (records.isEmpty()) {
            System.out.println("No confirmed bookings found.");
            return;
        }

        double totalRevenue = 0;
        for (Reservation res : records) {
            double serviceCost = sm.getTotalServiceCost(res.getReservationId());
            totalRevenue += serviceCost; // Simplification: tracking add-on revenue
            System.out.println(res + " | Add-ons: $" + serviceCost);
        }
        System.out.println("-------------------------------------");
        System.out.println("Total Bookings: " + records.size());
        System.out.println("Total Add-on Revenue: $" + totalRevenue);
    }
}

// --- UC3 & UC6: Room Inventory & Allocation ---
class RoomInventory {
    private Map<String, Integer> availability = new HashMap<>();
    private Map<String, Set<String>> allocatedRooms = new HashMap<>();

    public void addRoomType(String type, int count) {
        availability.put(type, count);
        allocatedRooms.put(type, new HashSet<>());
    }

    public boolean allocateRoom(Reservation res, BookingHistory history) {
        String type = res.getRoomType();
        int count = availability.getOrDefault(type, 0);

        if (count > 0) {
            String roomId = type.substring(0, 3).toUpperCase() + "-" + (allocatedRooms.get(type).size() + 1);
            allocatedRooms.get(type).add(roomId);
            availability.put(type, count - 1);

            // UC8: Transition from "Active Process" to "Historical Record"
            history.recordConfirmation(res);
            return true;
        }
        return false;
    }
}

// --- Main Application ---
public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("Book My Stay v1.8 [Reporting & History Mode]");

        // Initialize Components
        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("Single", 2);

        ServiceManager serviceManager = new ServiceManager();
        BookingHistory history = new BookingHistory();
        ReportingService reporter = new ReportingService();

        // 1. Setup Reservations & Services
        Reservation res1 = new Reservation("Alice", "Single");
        serviceManager.addService(res1.getReservationId(), new Service("WiFi", 10));

        Reservation res2 = new Reservation("Bob", "Single");
        serviceManager.addService(res2.getReservationId(), new Service("Breakfast", 20));

        // 2. Process Allocations
        System.out.println("\nProcessing Bookings...");
        inventory.allocateRoom(res1, history);
        inventory.allocateRoom(res2, history);

        // 3. Generate Report (UC8)
        reporter.generateSummary(history, serviceManager);
    }
}
