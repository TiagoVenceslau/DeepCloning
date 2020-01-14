package org.tvenceslau.java.Cloneable.MockDomain;

import java.util.Objects;

public class SimpleCompostMockObject extends MockObject {

    @NotToUpdate
    private MockObject childObject;

    private String immutableString = "THIS STRING WON'T CHANGE";

    public SimpleCompostMockObject(){}

    public MockObject getChildObject() {
        return childObject;
    }

    public void setChildObject(MockObject childObject) {
        this.childObject = childObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleCompostMockObject)) return false;
        if (!super.equals(o)) return false;

        SimpleCompostMockObject that = (SimpleCompostMockObject) o;

        if (!Objects.equals(childObject, that.childObject)) return false;
        return Objects.equals(immutableString, that.immutableString);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (childObject != null ? childObject.hashCode() : 0);
        result = 31 * result + (immutableString != null ? immutableString.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SimpleCompostMockObject{" +
                "childObject=" + childObject +
                ", immutableString='" + immutableString + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
