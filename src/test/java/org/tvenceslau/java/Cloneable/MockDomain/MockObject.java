package org.tvenceslau.java.Cloneable.MockDomain;
import org.tvenceslau.java.Cloneable.Cloneable;

public class MockObject implements Cloneable<MockObject> {

    protected String name;

    public MockObject(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MockObject)) return false;

        MockObject that = (MockObject) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
