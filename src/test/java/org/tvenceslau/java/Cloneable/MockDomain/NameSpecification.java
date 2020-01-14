package org.tvenceslau.java.Cloneable.MockDomain;

import org.tvenceslau.java.Cloneable.Cloneable.UpdateSpecification;

public class NameSpecification implements UpdateSpecification<String> {

    private static final String SEPARATOR = "_CLONE_";

    @Override
    public String update(String originalValue, int index) {
        return originalValue + SEPARATOR + index;
    }
}
