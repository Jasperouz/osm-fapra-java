package de.unistuttgart.fmi.osmfaprajava;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private String osmId;
    private String name;
    private String cuisineType;
    private List<User> voters = new ArrayList<>();

    public Restaurant() { }

    public String getOsmId() {
        return osmId;
    }

    public void setOsmId(String osmId) {
        this.osmId = osmId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public void addVoter(User user) {
        this.voters.add(user);
    }
    public List<User> getVoters() {
        return this.voters;
    }
}
