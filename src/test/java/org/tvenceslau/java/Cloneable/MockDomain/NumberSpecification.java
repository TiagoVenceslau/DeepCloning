package org.tvenceslau.java.Cloneable.MockDomain;

import org.tvenceslau.java.Cloneable.Cloneable.UpdateSpecification;

public class NumberSpecification implements UpdateSpecification<Integer> {

    @Override
    public Integer update(Integer originalValue, int index) {
        return originalValue + index;
    }
}
