package de.unistuttgart.fmi.osmfaprajava;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

@RestController
public class Controllers {

    private class UserID {
        public UUID userId;
    }

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
}
