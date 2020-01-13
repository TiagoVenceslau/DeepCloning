package org.tvenceslau.java.Cloneable.MockDomain;

/**
 * Because Enums are static in nature, they can't be cloned and are therefore
 * a good example on when to use custom cloning methods via {@link org.tvenceslau.java.Cloneable.Cloneable.ToClone}
 */
public enum MockEnum {
    ONE{
        @Override
        public MockEnum cloneSelf() {
            return MockEnum.ONE;
        }
    },
    TWO{
        @Override
        public MockEnum cloneSelf() {
            return MockEnum.TWO;
        }
    };

    public abstract MockEnum cloneSelf();
}
