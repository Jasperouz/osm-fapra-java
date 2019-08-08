package de.unistuttgart.fmi.osmfaprajava;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controllers {

    private GroupRepository groupRepository;
    @Autowired
    public Controllers(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }
    @CrossOrigin
    @RequestMapping("create-group")
    public MyGroup createGroup() {
        MyGroup myGroup = new MyGroup();
        groupRepository.save(myGroup);
        return myGroup;
    }
}
