package de.unistuttgart.fmi.osmfaprajava;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.boot.autoconfigure.http.HttpProperties.Encoding.DEFAULT_CHARSET;

@RestController
public class Controllers {

    private class UserID {
        public UUID userId;
    }
    private static final Logger log = LoggerFactory.getLogger(Controllers.class);

    private GroupRepository groupRepository;
    private UserRepository userRepository;
    private SimpMessagingTemplate template;
    @Autowired
    public Controllers(GroupRepository groupRepository, UserRepository userRepository, SimpMessagingTemplate template) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.template = template;
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
        return ResponseEntity.ok().build();

    }

    @CrossOrigin
    @RequestMapping(value = "groups/{groupId}", method = RequestMethod.GET)
    public MyGroup getGroup(@PathVariable UUID groupId) {
        MyGroup group = groupRepository.findById(groupId).get();
        return group;
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
    public long getDistance(@RequestParam long source, @RequestParam long target) throws IOException {
        Socket clientSocket = new Socket("127.0.0.1", 4301);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.print(source + "," + target);
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
    public ResponseEntity startVote(@RequestParam UUID groupId) {
        MyGroup group = this.groupRepository.findById(groupId).get();
        if(group.isVoteStarted()) {
            return ResponseEntity.unprocessableEntity().body("voting already started");
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
        OscarItem[] items = restTemplate.getForObject("https://oscardev.fmi.uni-stuttgart.de/oscar/items/all?q=@amenity:restaurant+$geo:9.168434143066408,48.76976735954908,9.18663024902344,48.7794964290288", OscarItem[].class);
        for(OscarItem item : items) {
            Restaurant restaurant = new Restaurant();
            for(int i = 0; i < item.getK().length; i++) {
                if(item.getK()[i].equals("name")) {
                    restaurant.setName(item.getV()[i]);
                }
                if(item.getK()[i].equals("cuisine")) {
                    restaurant.setCuisineType(item.getV()[i]);
                }
            }
            group.addRestaurant(restaurant);
        }
        group.setVoteStarted(true);
        groupRepository.save(group);
        return ResponseEntity.ok().build();
    }
}
