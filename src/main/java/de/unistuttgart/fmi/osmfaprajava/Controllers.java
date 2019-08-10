package de.unistuttgart.fmi.osmfaprajava;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

@RestController
public class Controllers {

    private class UserID {
        public UUID userId;
    }

    private GroupRepository groupRepository;
    private UserRepository userRepository;
    @Autowired
    public Controllers(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @CrossOrigin
    @RequestMapping(value = "set-lat-lon", method = RequestMethod.PUT)
    public User setLatLon(@RequestParam double lat, @RequestParam double lon, @RequestParam UUID userId) {
        User user = userRepository.findById(userId).get();
        user.setLat(lat);
        user.setLon(lon);
        userRepository.save(user);
        return user;
    }

    @CrossOrigin
    @RequestMapping(value="create-group", method = RequestMethod.POST)
    public MyGroup createGroup(@RequestParam UUID userId) {
        MyGroup myGroup = new MyGroup();
        myGroup.setCreatorId(userId);
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
    public ArrayList<User> getGroup(@PathVariable UUID groupId) {
        ArrayList<User> users = new ArrayList<>();
        MyGroup group = groupRepository.findById(groupId).get();
        for (User user :
                group.getUsers()) {
            users.add(user);
        }
        return users;
    }

    @CrossOrigin
    @RequestMapping(value="create-user", method = RequestMethod.POST)
    public User createUser(@RequestBody String userName) {
        User user = new User();
        user.setName(userName);
        userRepository.save(user);
        return user;
    }


}
