package org.tvenceslau.java.Cloneable.MockDomain;

import java.util.List;
import java.util.Objects;

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

        if (!Objects.equals(mockObjectList, that.mockObjectList))
            return false;
        return mockEnum == that.mockEnum;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mockObjectList != null ? mockObjectList.hashCode() : 0);
        result = 31 * result + (mockEnum != null ? mockEnum.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompostMockObject{" +
                "mockObjectList=" + mockObjectList +
                ", mockEnum=" + mockEnum +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
