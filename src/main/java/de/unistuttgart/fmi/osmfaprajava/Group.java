package de.unistuttgart.fmi.osmfaprajava;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.UUID;

@Entity
public class Group {
    @Id
    @GeneratedValue
    private UUID id;

    private ArrayList<Member> members;


    public Group() {
    }

    public ArrayList<Member> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
