package org.tvenceslau.java.Cloneable.test;

import org.junit.Assert;
import org.junit.Test;
import org.tvenceslau.java.Cloneable.MockDomain.CompostMockObject;
import org.tvenceslau.java.Cloneable.MockDomain.MockEnum;
import org.tvenceslau.java.Cloneable.MockDomain.MockObject;
import org.tvenceslau.java.Cloneable.MockDomain.SimpleCompostMockObject;

import java.util.ArrayList;
import java.util.List;

public class CloneablesTest {

    @Test
    public void testCloneUpdate(){
        for (MockObject o: generateObjectList()){
            System.out.println("Cloning " + o);
            final MockObject clone = o.cloneSelf();
            Assert.assertNotSame(o, clone);
            Assert.assertEquals(o, clone);
            System.out.println("Upgrading " + clone);
            clone.updateSelf(1);
            System.out.println("Object after update: \n" + clone);
            Assert.assertNotSame(o, clone);
        };
    }

    @Test
    public void testMockObjectCloning(){
        generateObjectList().forEach(o -> {
            System.out.println("Cloning " + o);
            final MockObject clone = o.cloneSelf();
            Assert.assertNotSame(o, clone);
            Assert.assertEquals(o, clone);
        });
    }

    private List<MockObject> generateObjectList(){
        return new ArrayList<MockObject>(){{
            add(generatePlain());
            add(generateSimpleCompost());
            add(generateCompost());
        }};
    }

    private MockObject generateCompost(){
        final CompostMockObject mo = new CompostMockObject();
        mo.setName("CompostMockObject");
        final List<MockObject> list =new ArrayList<>();

        list.add(generateSimpleCompost());
        list.add(generatePlain());

        mo.setMockObjectList(list);
        mo.setMockEnum(MockEnum.ONE);
        return mo;
    }

    private MockObject generateSimpleCompost(){
        final SimpleCompostMockObject mo = new SimpleCompostMockObject();
        mo.setName("SimpleCompostMockObject");
        mo.setChildObject(generatePlain());
        return mo;
    }

    private MockObject generatePlain(){
        final MockObject mo = new MockObject();
        mo.setName("PlainMockObject");
        return mo;
    }
}
