package de.unistuttgart.fmi.osmfaprajava;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static org.springframework.boot.autoconfigure.http.HttpProperties.Encoding.DEFAULT_CHARSET;

@RestController
@RequestMapping(value = "api/")
public class Controllers {

    private class UserID {
        public UUID userId;
    }
    private static final Logger log = LoggerFactory.getLogger(Controllers.class);

    private GroupRepository groupRepository;
    private UserRepository userRepository;
    private SimpMessagingTemplate template;
    private RestaurantRepository restaurantRepository;
    @Autowired
    public Controllers(GroupRepository groupRepository, UserRepository userRepository, SimpMessagingTemplate template,
                       RestaurantRepository restaurantRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.template = template;
        this.restaurantRepository = restaurantRepository;
    }

    @CrossOrigin
    @RequestMapping(value = "set-lat-lon", method = RequestMethod.PUT)
    public User setLatLon(@RequestParam double lat, @RequestParam double lon, @RequestParam UUID userId) {
        User user = userRepository.findById(userId).get();
        user.setLat(lat);
        user.setLon(lon);
        userRepository.save(user);
        template.convertAndSend("/topic/lat-lon-update/" + user.getId(), user);
        return user;
    }

    @CrossOrigin
    @RequestMapping(value="create-group", method = RequestMethod.POST)
    public MyGroup createGroup(@RequestParam UUID userId) {
        User creator = userRepository.findById(userId).get();
        MyGroup myGroup = new MyGroup();
        myGroup.setCreatorId(userId);
        myGroup.addUser(creator);
        groupRepository.save(myGroup);
        return myGroup;
    }

    @CrossOrigin
    @RequestMapping(value = "join-group", method = RequestMethod.POST)
    public ResponseEntity joinGroup(@RequestParam UUID userId, @RequestParam UUID groupId) {
        MyGroup group = groupRepository.findById(groupId).get();
        User user = userRepository.findById(userId).get();
        group.addUser(user);
        groupRepository.save(group);
        template.convertAndSend("/topic/user-added/" + groupId, user);
        return ResponseEntity.ok().build();

    }

    @CrossOrigin
    @RequestMapping(value = "groups/{groupId}", method = RequestMethod.GET)
    public MyGroup getGroup(@PathVariable UUID groupId, @RequestParam(required = false) Optional<UUID> userId) {
        MyGroup group = groupRepository.findById(groupId).get();
        User creator = userRepository.findById(group.getCreatorId()).get();
        group.setCreatorName(creator.getName());
        if(userId.isPresent() && group.isVoteStarted()) {
            int usersVoted = 0;
            for (User user : group.getUsers()) {
                if(user.hasVoted())
                    usersVoted++;
            }
            group.setUsersVoted(usersVoted);
            User user = userRepository.findById(userId.get()).get();
            if(user.hasVoted()) {
                Restaurant restaurant = restaurantRepository.findById(user.getVotedFor()).get();
                restaurant.setVotedFor(true);
                group.addRestaurant(restaurant);
            }
            group.getRestaurants().forEach(restaurant1 -> {
                restaurant1.setDistance(restaurant1.getDistanceMap().get(userId.get()));
                restaurant1.setVotes(restaurant1.getVoters().size());
            });
            return group;
        }
        if(!group.isVoteStarted())
            return group;
        else {
            group.getRestaurants().forEach(restaurant -> restaurant.setVotes(restaurant.getVoters().size()));
            return group;
        }
    }

    @CrossOrigin
    @RequestMapping(value="create-user", method = RequestMethod.POST)
    public User createUser(@RequestBody String userName) {
        User user = new User();
        user.setName(userName);
        userRepository.save(user);
        return user;
    }

    @CrossOrigin
    @RequestMapping(value = "distance", method = RequestMethod.GET)
    public long getDistance(@RequestParam float lat1, @RequestParam float lng1, @RequestParam float lat2, @RequestParam float lng2) throws IOException {
        Socket clientSocket = new Socket("127.0.0.1", 4301);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.print(lat1 + "," + lng1 + "," + lat2 + "," + lng2);
        out.flush();
        char[] buffer = new char[200];
        BufferedReader bufferedReader =
                new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream()));
        int anzahlZeichen = bufferedReader.read(buffer, 0, 200); // blockiert bis Nachricht empfangen
        String nachricht = new String(buffer, 0, anzahlZeichen);
        clientSocket.close();
        return Long.parseLong(nachricht);
    }

    @CrossOrigin
    @RequestMapping(value = "start-vote", method = RequestMethod.GET)
    public ResponseEntity startVote(@RequestParam UUID groupId) throws IOException {
        MyGroup group = this.groupRepository.findById(groupId).get();
        if(group.isVoteStarted()) {
            // return ResponseEntity.unprocessableEntity().body("voting already started");
        }
        RestTemplate restTemplate = new RestTemplate();
        MyGroup.BoundingBox bbox = group.calcBoundingBox();

        List<MediaType> types = Arrays.asList(
                new MediaType("text", "json", DEFAULT_CHARSET),
                new MediaType("application", "json", DEFAULT_CHARSET),
                new MediaType("application", "*+json", DEFAULT_CHARSET));
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setSupportedMediaTypes(types);
        List<HttpMessageConverter<?>> myMessageConverterList = new ArrayList<>();
        myMessageConverterList.add(gsonHttpMessageConverter);
        restTemplate.setMessageConverters(myMessageConverterList);
        //String queryUrl = "https://oscardev.fmi.uni-stuttgart.de/oscar/items/all?q=@amenity:restaurant+$geo:9.0,49.0,10.0,50.0";
        String queryUrl = "https://oscardev.fmi.uni-stuttgart.de/oscar/items/all?q=@amenity:restaurant+" +
                "$geo:" + (bbox.getMinLon() - 0.025) + "," + (bbox.getMinLat() - 0.025) + "," + (bbox.getMaxLon() + 0.025) + "," + (bbox.getMaxLat() + 0.025);
        log.info(queryUrl);
        OscarItem[] items = restTemplate.getForObject(queryUrl, OscarItem[].class);
        List<OscarItem> itemsList = new ArrayList<>(Arrays.asList(items));
        Collections.shuffle(itemsList);
        List<OscarItem> subItemList;
        if(itemsList.size() > 101) {
            subItemList = itemsList.subList(0, 100);
        } else {
            subItemList = itemsList;
        }
        log.info("got query response");
        for(OscarItem item : subItemList) {
            Restaurant restaurant = new Restaurant();
            restaurant.setOsmId(item.getOsmid());
            boolean hasName = false;
            for(int i = 0; i < item.getK().length; i++) {
                if(item.getK()[i].equals("name")) {
                    restaurant.setName(item.getV()[i]);
                    hasName = true;
                }
                if(item.getK()[i].equals("cuisine")) {
                    restaurant.setCuisineType(item.getV()[i]);
                }
            }
            if(hasName) {
                restaurant.setLat(item.getBbox()[0]);
                restaurant.setLon(item.getBbox()[2]);
                group.addRestaurant(restaurant);
            }
        }

        StringBuilder message = new StringBuilder(group.getUsers().size()*2 + ","  + group.getRestaurants().size()*2 + ",");
        ArrayList<User> users = new ArrayList<>();
        for (User user : group.getUsers()) {
            message.append(user.getLat()).append(",").append(user.getLon()).append(",");
            users.add(user);
        }
        ArrayList<Restaurant> restaurants = new ArrayList<>();
        for (Restaurant restaurant : group.getRestaurants()) {
            message.append(restaurant.getLat()).append(",").append(restaurant.getLon()).append(",");
            restaurants.add(restaurant);
        }
        String[] splitMessage = sendAndGetMessage(message.toString(), 1000000).split(",");
        int i = 0;
        for (User user : users) {
            for (Restaurant restaurant : restaurants) {
                restaurant.addDistance(user.getId(), Long.parseLong(splitMessage[i]));
                ++i;
            }
        }
        for (Restaurant restaurant :
                group.getRestaurants()) {
            double cumulativeDistance = 0;
            for (User user :
                    group.getUsers()) {
                cumulativeDistance += restaurant.getDistanceMap().get(user.getId());
            }
            restaurant.setAverageDistance(cumulativeDistance/group.getUsers().size());
        }
        this.restaurantRepository.saveAll(group.getRestaurants());
        template.convertAndSend("/topic/vote-started/" + group.getId(), "");
        group.setVoteStarted(true);
        groupRepository.save(group);
        return ResponseEntity.ok().build();
    }
    @CrossOrigin
    @RequestMapping(value = "vote", method = RequestMethod.PUT)
    public ResponseEntity vote(@RequestParam UUID restaurantId, @RequestParam UUID userId, @RequestParam UUID groupId) {
        User user = userRepository.findById(userId).get();
        MyGroup group = groupRepository.findById(groupId).get();
        if(user.hasVoted()) {
            Restaurant votedRestaurant = restaurantRepository.findById(user.getVotedFor()).get();
            votedRestaurant.removeVoter(user);
            restaurantRepository.save(votedRestaurant);
        }
        user.setHasVoted(true);
        Restaurant restaurant = restaurantRepository.findById(restaurantId).get();
        restaurant.addVoter(user);
        user.setVotedFor(restaurantId);
        userRepository.save(user);
        restaurantRepository.save(restaurant);
        int usersVoted = 0;
        for (User user1 : group.getUsers()) {
            if(user1.hasVoted())
                usersVoted++;
        }
        boolean allUsersHaveVoted = usersVoted == group.getUsers().size();
        template.convertAndSend("/topic/voting-changed/" + groupId,
                user.getName() + "," + restaurant.getName() + "," + allUsersHaveVoted);
        return ResponseEntity.ok().build();
    }
    @CrossOrigin
    @RequestMapping(value = "end-vote", method = RequestMethod.PUT)
    public ResponseEntity endVote(@RequestParam UUID groupId, @RequestParam UUID userId) {
        log.info("ending vote");
        MyGroup group = groupRepository.findById(groupId).get();
        Restaurant bestRestaurant = new Restaurant();
        double smallestDistance = Double.MAX_VALUE;
        int mostVotes = 0;
        for (Restaurant restaurant :
                group.getRestaurants()) {
            if(restaurant.getVoters().size() > mostVotes) {
                bestRestaurant = restaurant;
                mostVotes  = restaurant.getVoters().size();
            } else if (restaurant.getVoters().size() == mostVotes && restaurant.getAverageDistance() < smallestDistance) {
                bestRestaurant = restaurant;
                smallestDistance = restaurant.getAverageDistance();
            }
        }
        group.setBestRestaurant(bestRestaurant);
        group.setVoteEnded(true);
        groupRepository.save(group);
        template.convertAndSend("/topic/voting-ended/" + groupId, "");
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @RequestMapping(value = "get-path", method = RequestMethod.GET)
    public List<LatLng> getPath(@RequestParam double sourceLat, @RequestParam double sourceLng, @RequestParam double targetLat, @RequestParam double targetLng) throws IOException {
        String message = "p," + sourceLat + "," + sourceLng + "," + targetLat + "," + targetLng;
        String[] splitMessage = sendAndGetMessage(message, 1000000).split(",");
        ArrayList<LatLng> path = new ArrayList<>();
        for(int i = 0; i < splitMessage.length - 1; i+=2) {
            path.add(new LatLng(Double.parseDouble(splitMessage[i]), Double.parseDouble(splitMessage[i+1])));
        }
        return path;
    }

    private String sendAndGetMessage(String message, int bufferSize) throws IOException {
        Socket clientSocket = new Socket("127.0.0.1", 4301);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.print(message);
        out.flush();
        log.info("sent message: " + message);
        char[] buffer = new char[bufferSize];
        BufferedReader bufferedReader =
                new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream()));
        int anzahlZeichen = bufferedReader.read(buffer, 0, bufferSize); // blockiert bis Nachricht empfangen
        String responseMessage = new String(buffer, 0, anzahlZeichen);
        log.info("response message: " + responseMessage);
        clientSocket.close();
        return responseMessage;
    }
}
