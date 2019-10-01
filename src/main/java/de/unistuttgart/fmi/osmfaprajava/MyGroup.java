package de.unistuttgart.fmi.osmfaprajava;

import javax.persistence.*;
import java.util.*;

@Entity
public class MyGroup {

    public class BoundingBox {
        private double minLat;
        private double minLon;
        private double maxLat;
        private double maxLon;


        BoundingBox(){}
        BoundingBox(double minLat, double minLon, double maxLat, double maxLon) {
            this.minLat = minLat;
            this.minLon = minLon;
            this.maxLat = maxLat;
            this.maxLon = maxLon;
        }

        public double getMinLat() {
            return minLat;
        }

        public double getMinLon() {
            return minLon;
        }

        public double getMaxLat() {
            return maxLat;
        }

        public double getMaxLon() {
            return maxLon;
        }
    }

    @Id
    @GeneratedValue
    private UUID id;

    private UUID creatorId;
    @OneToMany
    private List<User> users = new ArrayList<>();
    @OneToMany
    private Set<Restaurant> restaurants = new HashSet<>();
    private boolean voteStarted = false;
    private boolean voteEnded = false;
    @OneToOne
    private Restaurant bestRestaurant;
    @Transient
    private int usersVoted = 0;
    @Transient
    private String creatorName;


    MyGroup() { }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public int getUsersVoted() {
        return usersVoted;
    }

    public void setUsersVoted(int usersVoted) {
        this.usersVoted = usersVoted;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    public void addRestaurant(Restaurant restaurant) {
        this.restaurants.add(restaurant);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public boolean isVoteStarted() {
        return voteStarted;
    }

    public void setVoteStarted(boolean voteStarted) {
        this.voteStarted = voteStarted;
    }

    public boolean isVoteEnded() {
        return voteEnded;
    }

    public void setVoteEnded(boolean voteEnded) {
        this.voteEnded = voteEnded;
    }

    public Restaurant getBestRestaurant() {
        return bestRestaurant;
    }

    public void setBestRestaurant(Restaurant bestRestaurant) {
        this.bestRestaurant = bestRestaurant;
    }

    public BoundingBox calcBoundingBox() {
        double minLat = Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;
        for (User user :
                users) {
            if(user.getLat() < minLat)
                minLat = user.getLat();
            if(user.getLon() < minLon)
                minLon = user.getLon();
            if(user.getLat() > maxLat)
                maxLat = user.getLat();
            if(user.getLon() > maxLon)
                maxLon = user.getLon();

        }
        return new BoundingBox(minLat, minLon, maxLat, maxLon);
    }
}
