package mycompany.domain;

import org.garret.perst.Persistent;

/**
 * BenchmarkData - For benchmarking Perst performance.
 */
public class BenchmarkData extends Persistent {
    
    private String uuid;
    private String name;
    private long value;
    private String category;
    private int rating;
    private double amount;
    private long createdAt;
    private long updatedAt;
    
    public BenchmarkData() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
    
    public BenchmarkData(String name, long value, String category, int rating, double amount) {
        this();
        this.uuid = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.value = value;
        this.category = category;
        this.rating = rating;
        this.amount = amount;
    }
    
    public String getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; this.updatedAt = System.currentTimeMillis(); }
    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; this.updatedAt = System.currentTimeMillis(); }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; this.updatedAt = System.currentTimeMillis(); }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; this.updatedAt = System.currentTimeMillis(); }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; this.updatedAt = System.currentTimeMillis(); }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
}
