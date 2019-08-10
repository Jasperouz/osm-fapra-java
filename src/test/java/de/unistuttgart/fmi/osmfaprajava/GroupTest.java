package de.unistuttgart.fmi.osmfaprajava;

import org.junit.Assert;
import org.junit.Test;

public class GroupTest {
    @Test
    public void testBoundingBox() {
        MyGroup group = new MyGroup();

        User user = new User();
        user.setLon(10);
        user.setLat(10);
        User user2 = new User();
        user2.setLat(0);
        user2.setLon(0);

        group.addUser(user);
        group.addUser(user2);
        MyGroup.BoundingBox bb = group.calcBoundingBox();
        Assert.assertEquals(2, group.getUsers().size());
        Assert.assertEquals(0,bb.getMinLon(),  0);
        Assert.assertEquals(0,bb.getMinLat(), 0);
        Assert.assertEquals(10,bb.getMaxLon(), 0);
        Assert.assertEquals(10,bb.getMaxLat(),  0);


    }
}
