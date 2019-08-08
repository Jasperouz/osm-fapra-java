package de.unistuttgart.fmi.osmfaprajava;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class Controllers {

    @CrossOrigin
    @MessageMapping("/update-location/{id}")
    @SendTo("topic/test")
    public String updateLocation(String message, @PathVariable String id) throws Exception {
        Thread.sleep(1000);
        return id;
    }
}
