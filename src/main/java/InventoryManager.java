import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryManager {

    private List<Item> items;

    public InventoryManager() {
        items = new ArrayList<>();
    }

    // ===== Add Item =====
    public void addItem(Item item) {
        items.add(item);
    }

    // ===== Remove Item =====
    public void removeItem(Item item) {
        items.remove(item);
    }

    // ===== Get All Items =====
    public List<Item> getAllItems() {
        return new ArrayList<>(items);
    }

    // ===== Search Items by Name =====
    public List<Item> searchItems(String query) {
        String q = query.toLowerCase();
        return items.stream()
                .filter(i -> i.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    // ===== Filter by Category =====
    public List<Item> filterByCategory(String category) {
        return items.stream()
                .filter(i -> i.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    // ===== Get Expiring Items (within 3 days) =====
    public List<Item> getExpiringItems() {
        return items.stream()
                .filter(Item::isExpiringSoon)
                .collect(Collectors.toList());
    }

    // ===== Optional: Get Expired Items =====
    public List<Item> getExpiredItems() {
        return items.stream()
                .filter(Item::isExpired)
                .collect(Collectors.toList());
    }
}

