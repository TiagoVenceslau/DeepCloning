package org.tvenceslau.java.Cloneable.MockDomain;

import java.util.List;

public class CompostMockObject extends MockObject {

    protected List<MockObject> mockObjectList;

    @ToClone(method = "cloneSelf")
    private MockEnum mockEnum;

    public MockEnum getMockEnum() {
        return mockEnum;
    }

    public void setMockEnum(MockEnum mockEnum) {
        this.mockEnum = mockEnum;
    }

    public List<MockObject> getMockObjectList() {
        return mockObjectList;
    }

    public void setMockObjectList(List<MockObject> mockObjectList) {
        this.mockObjectList = mockObjectList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompostMockObject)) return false;
        if (!super.equals(o)) return false;

        CompostMockObject that = (CompostMockObject) o;

        if (!mockObjectList.equals(that.mockObjectList)) return false;
        return mockEnum == that.mockEnum;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mockObjectList.hashCode();
        result = 31 * result + mockEnum.hashCode();
        return result;
    }
}
