package com.example.a1project;

public class Feedback {
    private String id;
    private String feature;
    private String suggestion;

    public Feedback() {
        // Default constructor required for Firebase
    }

    public Feedback(String id, String feature, String suggestion) {
        this.id = id;
        this.feature = feature;
        this.suggestion = suggestion;
    }

    public String getId() {
        return id;
    }

    public String getFeature() {
        return feature;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
