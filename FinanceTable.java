package com.example.financemanager;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "finance")
public class FinanceTable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    // OLD: private String paymentType; <--- REMOVED
    private String title;       // <-- NEW: Transaction क
    private String category;    // <-- NEW: Category
    private long amount;
    private String description;
    private boolean income;
    private String date;        // <-- NEW: Date

    public FinanceTable() {}

    // Constructor
    public FinanceTable(int id, String title, String category, long amount, String description, boolean income, String date) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.income = income;
        this.date = date;
    }

    // --- Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isIncome() { return income; }
    public void setIncome(boolean income) { this.income = income; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}