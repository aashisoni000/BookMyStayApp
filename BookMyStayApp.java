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
    public double getPrice() { return price; } // Added getter for cost calculation
}

class SingleRoom extends Room { public SingleRoom() { super("Single", 100.0); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double", 180.0); } }

// --- UC7: Add-On Service Model ---
class Service {
    private String name;
    private double cost;

    public Service(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }

    public String getName() { return name; }
    public double getCost() { return cost; }
}

// --- UC5: Reservation Model ---
class Reservation {
    private String reservationId; // UC7: Added unique ID for mapping services
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
        return "[" + reservationId + "] Guest: " + guestName + " | Type: " + roomType;
    }
}

// --- UC7: Add-On Service Manager ---
class ServiceManager {
    // Map<ReservationID, List<Services>> - One-to-Many Relationship
    private Map<String, List<Service>> selectedServices = new HashMap<>();

    public void addService(String reservationId, Service service) {
        selectedServices.computeIfAbsent(reservationId, k -> new ArrayList<>()).add(service);
        System.out.println("Service Added: " + service.getName() + " to Reservation " + reservationId);
    }

    public double calculateAdditionalCost(String reservationId) {
        List<Service> services = selectedServices.getOrDefault(reservationId, Collections.emptyList());
        return services.stream().mapToDouble(Service::getCost).sum();
    }

    public void displayServices(String reservationId) {
        List<Service> services = selectedServices.get(reservationId);
        if (services != null) {
            System.out.print(" Services: ");
            services.forEach(s -> System.out.print(s.getName() + " ($" + s.getCost() + ") "));
        }
    }
}

// --- UC3 & UC6: Room Inventory & Allocation Service ---
class RoomInventory {
    private Map<String, Integer> availability = new HashMap<>();
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
            allocatedRooms.get(type).add(roomId);
            availability.put(type, count - 1);
            System.out.println("SUCCESS: Room " + roomId + " allocated to " + guestName);
            return true;
        }
        System.out.println("FAILED: No availability for " + type);
        return false;
    }

    public void displayFinalState(ServiceManager serviceManager, List<Reservation> processed) {
        System.out.println("\n--- Final System State ---");
        processed.forEach(res -> {
            double extra = serviceManager.calculateAdditionalCost(res.getReservationId());
            System.out.print(res);
            serviceManager.displayServices(res.getReservationId());
            System.out.println(" | Extra Cost: $" + extra);
        });
    }
}

class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();
    public void addRequest(Reservation res) { queue.add(res); }
    public Reservation nextRequest() { return queue.poll(); }
    public boolean isEmpty() { return queue.isEmpty(); }
}

// --- Main Application ---
public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("Welcome to Book My Stay v1.7 [Add-On Service Mode]");
        System.out.println("----------------------------------------------");

        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("Single", 5);

        ServiceManager serviceManager = new ServiceManager();
        List<Reservation> processedReservations = new ArrayList<>();

        // 1. Create Reservations
        Reservation res1 = new Reservation("Alice", "Single");
        Reservation res2 = new Reservation("Bob", "Single");

        // 2. Add Services (UC7)
        serviceManager.addService(res1.getReservationId(), new Service("WiFi", 10.0));
        serviceManager.addService(res1.getReservationId(), new Service("Breakfast", 25.0));
        serviceManager.addService(res2.getReservationId(), new Service("Late Checkout", 15.0));

        // 3. Process Logic
        BookingRequestQueue requestQueue = new BookingRequestQueue();
        requestQueue.addRequest(res1);
        requestQueue.addRequest(res2);

        while (!requestQueue.isEmpty()) {
            Reservation current = requestQueue.nextRequest();
            if(inventory.allocateRoom(current.getRoomType(), current.getGuestName())) {
                processedReservations.add(current);
            }
        }

        // 4. Final Summary showing total costs
        inventory.displayFinalState(serviceManager, processedReservations);
    }
}
