package de.unistuttgart.fmi.osmfaprajava;

import net.bytebuddy.asm.Advice;

import javax.persistence.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private ArrayList<Restaurant> currentRestaurants = new ArrayList<>();

    MyGroup() { }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<Restaurant> getCurrentRestaurants() {
        return currentRestaurants;
    }

    public void setCurrentRestaurants(ArrayList<Restaurant> currentRestaurants) {
        this.currentRestaurants = currentRestaurants;
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
