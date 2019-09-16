package de.unistuttgart.fmi.osmfaprajava;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

@Entity
public class Restaurant {
    @Id
    @GeneratedValue
    @NotNull
    private UUID id;
    private Long osmId;
    private String name;
    private String cuisineType;
    private float lat;
    private float lon;
    @JsonIgnore
    @ElementCollection
    @CollectionTable(name = "distance_to_user",
            joinColumns = { @JoinColumn(name = "user_id") })
    @MapKeyColumn(name = "user")
    @Column(name = "distance")
    private Map<UUID, Long> distanceMap = new HashMap<>();
    @Transient
    private boolean votedFor = false;
    @Transient
    private int votes = 0;
    @Transient
    private long distance = 0;
    @OneToMany
    @JsonIgnore
    private Set<User> voters = new HashSet<>();

    private double averageDistance;

    public Restaurant() { }

    public double getAverageDistance() {
        return averageDistance;
    }

    public void setAverageDistance(double averageDistance) {
        this.averageDistance = averageDistance;
    }

    public Long getOsmId() {
        return osmId;
    }

    public void setOsmId(Long osmId) {
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

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public void addVoter(User user) {
        this.voters.add(user);
    }
    public Set<User> getVoters() {
        return this.voters;
    }
    public boolean containsVoter(User user) {
        return voters.contains(user);
    }
    public void removeVoter(User user) {
        voters.remove(user);
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void addDistance(UUID userId, long distance) {
        this.distanceMap.put(userId, distance);
    }

    public float getDistance() {
        return distance;
    }

    public Map<UUID, Long> getDistanceMap() {
        return distanceMap;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public UUID getId() {
        return id;
    }

    public boolean isVotedFor() {
        return votedFor;
    }

    public void setVotedFor(boolean votedFor) {
        this.votedFor = votedFor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return osmId.equals(that.osmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(osmId);
    }
}
