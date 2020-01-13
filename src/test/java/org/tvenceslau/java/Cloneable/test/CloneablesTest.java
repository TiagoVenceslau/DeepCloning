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
    public void testPlain(){
        final MockObject original = generatePlain();

        final MockObject clone = original.cloneSelf();

        Assert.assertNotSame(original, clone);
        Assert.assertEquals(original, clone);


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
