package org.tvenceslau.java.Cloneable.MockDomain;
import org.tvenceslau.java.Cloneable.Cloneable;

import java.util.Objects;

public class MockObject implements Cloneable<MockObject> {

    @ToUpdate(spec=NameSpecification.class)
    protected String name;

    @ToUpdate(spec=NumberSpecification.class)
    protected Integer value = 0;

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

        if (!Objects.equals(name, that.name)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MockObject{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
