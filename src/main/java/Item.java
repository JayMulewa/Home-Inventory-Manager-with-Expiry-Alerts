import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Item {
    private String name;
    private String category;
    private double quantity; // numeric value
    private String unit;     // "kg", "g", etc.
    private LocalDate expiryDate;

    public Item(String name, String category, double quantity, String unit, LocalDate expiryDate) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    // ===== Getters & Setters =====
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    // ===== Display Quantity for Table =====
    public String getDisplayQuantity() {
        return quantity + " " + unit;
    }

    // ===== Expiry Checks =====
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean isExpiringSoon() {
        long days = daysToExpiry();
        return days > 0 && days <= 3;
    }

    public long daysToExpiry() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
}
