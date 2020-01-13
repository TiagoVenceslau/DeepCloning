package org.tvenceslau.java.Cloneable.MockDomain;

import org.tvenceslau.java.Cloneable.Cloneable;

public class SimpleCompostMockObject extends MockObject {

    private MockObject childObject;

    @NotToUpdate
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

        if (!childObject.equals(that.childObject)) return false;
        return immutableString.equals(that.immutableString);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + childObject.hashCode();
        result = 31 * result + immutableString.hashCode();
        return result;
    }
}
