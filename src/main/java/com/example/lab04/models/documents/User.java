package com.example.lab04.models.documents;

import java.util.HashMap;
import java.util.Map;

public class User {

    public User() {
        this.transactions = new HashMap<>();
    }

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private Map<String,String> transactions;

    public Map<String, String> getTransactions() {
        return transactions;
    }

    public void setTransactions(Map<String, String> transactions) {
        this.transactions = transactions;
    }
}
