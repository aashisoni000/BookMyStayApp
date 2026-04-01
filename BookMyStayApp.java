import java.io.*;
import java.util.*;

// --- UC12: Base Model must be Serializable ---
class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }
    @Override
    public String toString() { return "Guest: " + guestName + " [" + roomType + "]"; }
}

// --- UC12: Persistable System State ---
class SystemState implements Serializable {
    private static final long serialVersionUID = 1L;
    public Map<String, Integer> availability;
    public List<Reservation> history;

    public SystemState(Map<String, Integer> availability, List<Reservation> history) {
        this.availability = availability;
        this.history = history;
    }
}

// --- UC12: Persistence Service ---
class PersistenceService {
    private static final String FILE_NAME = "hotel_data.ser";

    public void save(Map<String, Integer> availability, List<Reservation> history) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            SystemState state = new SystemState(availability, history);
            oos.writeObject(state);
            System.out.println("SYSTEM: State persisted to " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("SAVE ERROR: " + e.getMessage());
        }
    }

    public SystemState load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("SYSTEM: No previous state found. Starting fresh.");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            System.out.println("SYSTEM: Recovering data from " + FILE_NAME + "...");
            return (SystemState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("LOAD ERROR: " + e.getMessage());
            return null;
        }
    }
}

// --- Main Application ---
public class BookMyStayApp {
    public static void main(String[] args) {
        System.out.println("Welcome to Book My Stay v1.12 [Persistence Mode]");
        System.out.println("------------------------------------------------");

        PersistenceService persistence = new PersistenceService();
        Map<String, Integer> currentAvailability = new HashMap<>();
        List<Reservation> currentHistory = new ArrayList<>();

        // 1. System Recovery (Startup)
        SystemState loadedState = persistence.load();
        if (loadedState != null) {
            currentAvailability = loadedState.availability;
            currentHistory = loadedState.history;
            System.out.println("RECOVERY SUCCESSFUL! Found " + currentHistory.size() + " past bookings.");
        } else {
            // Initial setup if no file exists
            currentAvailability.put("Single", 5);
        }

        // 2. Perform a new booking during this session
        System.out.println("\nProcessing new booking for this session...");
        if (currentAvailability.get("Single") > 0) {
            Reservation newRes = new Reservation("Alice_" + System.currentTimeMillis() % 1000, "Single");
            currentHistory.add(newRes);
            currentAvailability.put("Single", currentAvailability.get("Single") - 1);
            System.out.println("Booked: " + newRes);
        }

        // 3. System Shutdown (Save State)
        System.out.println("\nShutting down system...");
        persistence.save(currentAvailability, currentHistory);

        System.out.println("Check 'hotel_data.ser' in your folder. Run the app again to see the data persist!");
    }
}
